package sim;

import java.util.*;
import model.*;
import world.World;

public final class SimulationEngine {

    private final World world;
    private final Random rng;
    private final SimulationStats stats;

    private int stepNumber = 0;

    public SimulationEngine(World world, Random rng) {
        this.world = Objects.requireNonNull(world);
        this.rng = Objects.requireNonNull(rng);
        this.stats = new SimulationStats(world);
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public SimulationStats getStats() {
        return stats;
    }

    public void step() {
        stepNumber++;

        // 1) envelhecimento + energia (-1) + possíveis mortes (por idade/energia)
        for (Organism o : world.getOrganismsSnapshot()) {
            if (!o.isAlive()) continue;
            o.onStepStart();
        }

        // Remove já os mortos naturais, para não haver interações com “cadáveres”
        cleanupDeadNatural();

        // 2) movimento + alimentação
        Map<Position, List<Sheep>> sheepMeetings = moveSheepPhase();
        Map<Position, List<Wolf>> wolfMeetings = moveWolfPhase();

        // 3) reprodução
        reproducePlants();
        reproduceFromMeetingsSheep(sheepMeetings);
        reproduceFromMeetingsWolves(wolfMeetings);

        // segurança (normalmente não há mortos aqui, mas deixamos por robustez)
        cleanupDeadNatural();
    }

    // -------------------- MOVIMENTO: OVELHAS --------------------

    private Map<Position, List<Sheep>> moveSheepPhase() {
        Map<Position, List<Sheep>> intents = new HashMap<>();

        for (Organism o : world.getOrganismsSnapshot()) {
            if (!(o instanceof Sheep)) continue;
            if (!o.isAlive()) continue;

            Sheep s = (Sheep) o;
            Position target = s.chooseMoveTarget(world, rng);
            if (target == null || !world.isInside(target)) target = s.getPosition();

            intents.computeIfAbsent(target, k -> new ArrayList<>()).add(s);
        }

        // encontros (2+ tentaram o mesmo destino)
        Map<Position, List<Sheep>> meetings = new HashMap<>();
        for (var entry : intents.entrySet()) {
            if (entry.getValue().size() >= 2) meetings.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        // escolher 1 vencedor por célula
        Map<Position, Sheep> winners = new HashMap<>();
        for (var entry : intents.entrySet()) {
            List<Sheep> contenders = entry.getValue();
            winners.put(entry.getKey(), contenders.get(rng.nextInt(contenders.size())));
        }

        // aplicar movimentos
        for (var entry : winners.entrySet()) {
            Position dest = entry.getKey();
            Sheep s = entry.getValue();
            if (!s.isAlive()) continue;

            Position origin = s.getPosition();
            if (origin.equals(dest)) continue;

            Organism occupant = world.getAt(dest);

            if (occupant == null) {
                world.moveToEmpty(s, dest);
            } else if (occupant instanceof Plant) {
                Organism removed = world.moveInto(s, dest); // remove planta do mundo
                if (removed != null) {
                    removed.die();
                    stats.onPlantEatenBySheep();
                }
                s.eatPlant();
            }
        }

        return meetings;
    }

    // -------------------- MOVIMENTO: LOBOS --------------------

    private Map<Position, List<Wolf>> moveWolfPhase() {
        Map<Position, List<Wolf>> intents = new HashMap<>();

        for (Organism o : world.getOrganismsSnapshot()) {
            if (!(o instanceof Wolf)) continue;
            if (!o.isAlive()) continue;

            Wolf w = (Wolf) o;
            Position target = w.chooseMoveTarget(world, rng);
            if (target == null || !world.isInside(target)) target = w.getPosition();

            intents.computeIfAbsent(target, k -> new ArrayList<>()).add(w);
        }

        Map<Position, List<Wolf>> meetings = new HashMap<>();
        for (var entry : intents.entrySet()) {
            if (entry.getValue().size() >= 2) meetings.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        Map<Position, Wolf> winners = new HashMap<>();
        for (var entry : intents.entrySet()) {
            List<Wolf> contenders = entry.getValue();
            winners.put(entry.getKey(), contenders.get(rng.nextInt(contenders.size())));
        }

        for (var entry : winners.entrySet()) {
            Position dest = entry.getKey();
            Wolf w = entry.getValue();
            if (!w.isAlive()) continue;

            Position origin = w.getPosition();
            if (origin.equals(dest)) continue;

            Organism occupant = world.getAt(dest);

            if (occupant == null) {
                world.moveToEmpty(w, dest);

            } else if (occupant instanceof Sheep) {
                // só come se a ovelha estiver viva (por segurança)
                if (occupant.isAlive()) {
                    Organism removed = world.moveInto(w, dest);
                    if (removed != null) {
                        removed.die();
                        stats.onSheepEatenByWolf();
                    }
                    w.eatSheep();
                } else {
                    // se por algum motivo ainda existir uma ovelha morta ali, não conta como comida
                    world.moveInto(w, dest);
                }

            } else if (occupant instanceof Plant) {
                // lobo entra e planta desaparece (não ganha energia)
                Organism removed = world.moveInto(w, dest);
                if (removed != null) {
                    removed.die();
                    stats.onPlantRemovedByWolf();
                }
            }
        }

        return meetings;
    }

    // -------------------- REPRODUÇÃO: PLANTAS --------------------

    private void reproducePlants() {
        for (Organism o : world.getOrganismsSnapshot()) {
            if (!(o instanceof Plant)) continue;
            if (!o.isAlive()) continue;

            Plant p = (Plant) o;
            if (roll(p.reproductionProbability())) {
                Position target = p.chooseReproductionTarget(world, rng);
                if (target != null) {
                    world.place(new Plant(target), target);
                    stats.onPlantBorn();
                }
            }
        }
    }

    // -------------------- REPRODUÇÃO: OVELHAS (encontro) --------------------

    private void reproduceFromMeetingsSheep(Map<Position, List<Sheep>> meetings) {
        for (var entry : meetings.entrySet()) {
            Position meetingCell = entry.getKey();
            List<Sheep> contenders = entry.getValue();

            int adultCount = 0;
            for (Sheep s : contenders) {
                if (s != null && s.isAlive() && s.isAdultForReproduction()) adultCount++;
            }
            if (adultCount < 2) continue;

            if (roll(Sheep.REPRO_PROBABILITY)) {
                Position babyPos = chooseRandomEmptyAdjacent(meetingCell);
                if (babyPos != null) {
                    world.place(new Sheep(babyPos), babyPos);
                    stats.onSheepBorn();
                }
            }
        }
    }

    // -------------------- REPRODUÇÃO: LOBOS (encontro) --------------------

    private void reproduceFromMeetingsWolves(Map<Position, List<Wolf>> meetings) {
        for (var entry : meetings.entrySet()) {
            Position meetingCell = entry.getKey();
            List<Wolf> contenders = entry.getValue();

            int adultCount = 0;
            for (Wolf w : contenders) {
                if (w != null && w.isAlive() && w.isAdultForReproduction()) adultCount++;
            }
            if (adultCount < 2) continue;

            if (roll(Wolf.REPRO_PROBABILITY)) {
                Position babyPos = chooseRandomEmptyAdjacent(meetingCell);
                if (babyPos != null) {
                    world.place(new Wolf(babyPos), babyPos);
                    stats.onWolfBorn();
                }
            }
        }
    }

    private Position chooseRandomEmptyAdjacent(Position center) {
        List<Position> adj = new ArrayList<>(world.getAdjacent4(center));
        Collections.shuffle(adj, rng);
        for (Position p : adj) {
            if (world.isEmpty(p)) return p;
        }
        return null;
    }

    // -------------------- LIMPEZA (mortes naturais) --------------------

    private void cleanupDeadNatural() {
        for (Organism o : world.getOrganismsSnapshot()) {
            if (o == null) continue;
            if (o.isAlive()) continue;

            recordNaturalDeath(o);
            world.remove(o);
        }
    }

    private void recordNaturalDeath(Organism o) {
        // causa por idade (idade > maxAge)
        boolean diedByAge = o.getAge() > o.getMaxAge();

        if (o instanceof Plant) {
            // planta só morre naturalmente por idade (no nosso modelo)
            stats.onPlantDiedOldAge();
            return;
        }

        if (o instanceof Sheep) {
            if (diedByAge) stats.onSheepDiedOldAge();
            else stats.onSheepDiedStarvation(); // energia <= 0
            return;
        }

        if (o instanceof Wolf) {
            if (diedByAge) stats.onWolfDiedOldAge();
            else stats.onWolfDiedStarvation();
        }
    }

    private boolean roll(double p) {
        if (p <= 0.0) return false;
        if (p >= 1.0) return true;
        return rng.nextDouble() < p;
    }
}
