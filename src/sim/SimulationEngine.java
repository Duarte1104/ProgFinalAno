package sim;

import model.*;
import world.World;

import java.util.*;

public final class SimulationEngine {

    private final World world;
    private final Random rng;
    private int stepNumber = 0;

    public SimulationEngine(World world, Random rng) {
        this.world = Objects.requireNonNull(world);
        this.rng = Objects.requireNonNull(rng);
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public void step() {
        stepNumber++;

        // 1) envelhecimento + energia (-1) + mortes
        List<Organism> snap1 = world.getOrganismsSnapshot();
        for (Organism o : snap1) {
            if (!o.isAlive()) continue;
            o.onStepStart();
        }

        // 2) movimento + alimentação
        Map<Position, List<Sheep>> sheepMeetings = moveSheepPhase();
        Map<Position, List<Wolf>> wolfMeetings = moveWolfPhase();

        // 3) reprodução
        reproducePlants();
        reproduceFromMeetingsSheep(sheepMeetings);
        reproduceFromMeetingsWolves(wolfMeetings);

        // 4) remover mortos
        cleanupDead();
    }

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

        Map<Position, List<Sheep>> meetings = new HashMap<>();
        for (var e : intents.entrySet()) {
            if (e.getValue().size() >= 2) meetings.put(e.getKey(), new ArrayList<>(e.getValue()));
        }

        Map<Position, Sheep> winners = new HashMap<>();
        for (var e : intents.entrySet()) {
            List<Sheep> contenders = e.getValue();
            winners.put(e.getKey(), contenders.get(rng.nextInt(contenders.size())));
        }

        for (var e : winners.entrySet()) {
            Position dest = e.getKey();
            Sheep s = e.getValue();
            if (!s.isAlive()) continue;

            Position origin = s.getPosition();
            if (origin.equals(dest)) continue;

            Organism occupant = world.getAt(dest);
            if (occupant == null) {
                world.moveToEmpty(s, dest);
            } else if (occupant instanceof Plant) {
                Organism removed = world.moveInto(s, dest);
                if (removed != null) removed.die();
                s.eatPlant();
            }
        }

        return meetings;
    }

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
        for (var e : intents.entrySet()) {
            if (e.getValue().size() >= 2) meetings.put(e.getKey(), new ArrayList<>(e.getValue()));
        }

        Map<Position, Wolf> winners = new HashMap<>();
        for (var e : intents.entrySet()) {
            List<Wolf> contenders = e.getValue();
            winners.put(e.getKey(), contenders.get(rng.nextInt(contenders.size())));
        }

        for (var e : winners.entrySet()) {
            Position dest = e.getKey();
            Wolf w = e.getValue();
            if (!w.isAlive()) continue;

            Position origin = w.getPosition();
            if (origin.equals(dest)) continue;

            Organism occupant = world.getAt(dest);

            if (occupant == null) {
                world.moveToEmpty(w, dest);
            } else if (occupant instanceof Sheep) {
                Organism removed = world.moveInto(w, dest);
                if (removed != null) removed.die();
                w.eatSheep();
            } else if (occupant instanceof Plant) {
                // para manter 1 por célula no fim do passo, o lobo entra e a planta sai (sem energia)
                Organism removed = world.moveInto(w, dest);
                if (removed != null) removed.die();
            }
        }

        return meetings;
    }

    private void reproducePlants() {
        for (Organism o : world.getOrganismsSnapshot()) {
            if (!(o instanceof Plant)) continue;
            if (!o.isAlive()) continue;

            Plant p = (Plant) o;
            if (roll(p.reproductionProbability())) {
                Position target = p.chooseReproductionTarget(world, rng);
                if (target != null) world.place(new Plant(target), target);
            }
        }
    }

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
                if (babyPos != null) world.place(new Sheep(babyPos), babyPos);
            }
        }
    }

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
                if (babyPos != null) world.place(new Wolf(babyPos), babyPos);
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

    private void cleanupDead() {
        for (Organism o : world.getOrganismsSnapshot()) {
            if (o != null && !o.isAlive()) {
                world.remove(o);
            }
        }
    }

    private boolean roll(double p) {
        if (p <= 0.0) return false;
        if (p >= 1.0) return true;
        return rng.nextDouble() < p;
    }
}
