package sim;

import model.Species;
import world.World;

/** Estatísticas com detalhe de eventos (nascimentos, predação, mortes naturais). */
public final class SimulationStats {

    // Iniciais
    private final long initialPlants;
    private final long initialSheep;
    private final long initialWolves;

    // Nascimentos por reprodução
    private long bornPlants;
    private long bornSheep;
    private long bornWolves;

    // Interações
    private long plantsEatenBySheep;
    private long sheepEatenByWolves;
    private long plantsRemovedByWolves; // se o lobo entra numa planta e a planta desaparece

    // Mortes naturais
    private long plantsDiedOldAge;
    private long sheepDiedOldAge;
    private long sheepDiedStarvation;
    private long wolvesDiedOldAge;
    private long wolvesDiedStarvation;

    public SimulationStats(World world) {
        this.initialPlants = world.countSpecies(Species.PLANT);
        this.initialSheep  = world.countSpecies(Species.SHEEP);
        this.initialWolves = world.countSpecies(Species.WOLF);
    }

    // ---------- eventos "nascimentos" ----------
    public void onPlantBorn() { bornPlants++; }
    public void onSheepBorn() { bornSheep++; }
    public void onWolfBorn()  { bornWolves++; }

    // ---------- eventos de interação ----------
    public void onPlantEatenBySheep() { plantsEatenBySheep++; }
    public void onSheepEatenByWolf()  { sheepEatenByWolves++; }
    public void onPlantRemovedByWolf() { plantsRemovedByWolves++; }

    // ---------- mortes naturais ----------
    public void onPlantDiedOldAge() { plantsDiedOldAge++; }

    public void onSheepDiedOldAge() { sheepDiedOldAge++; }
    public void onSheepDiedStarvation() { sheepDiedStarvation++; }

    public void onWolfDiedOldAge() { wolvesDiedOldAge++; }
    public void onWolfDiedStarvation() { wolvesDiedStarvation++; }

    // ---------- getters ----------
    public long getInitialPlants() { return initialPlants; }
    public long getInitialSheep()  { return initialSheep; }
    public long getInitialWolves() { return initialWolves; }

    public long getBornPlants() { return bornPlants; }
    public long getBornSheep()  { return bornSheep; }
    public long getBornWolves() { return bornWolves; }

    public long getPlantsEatenBySheep() { return plantsEatenBySheep; }
    public long getSheepEatenByWolves() { return sheepEatenByWolves; }
    public long getPlantsRemovedByWolves() { return plantsRemovedByWolves; }

    public long getPlantsDiedOldAge() { return plantsDiedOldAge; }
    public long getSheepDiedOldAge()  { return sheepDiedOldAge; }
    public long getSheepDiedStarvation() { return sheepDiedStarvation; }
    public long getWolvesDiedOldAge() { return wolvesDiedOldAge; }
    public long getWolvesDiedStarvation() { return wolvesDiedStarvation; }

    // Totais úteis (se quiseres mostrar)
    public long getTotalCreatedPlants() { return initialPlants + bornPlants; }
    public long getTotalCreatedSheep()  { return initialSheep + bornSheep; }
    public long getTotalCreatedWolves() { return initialWolves + bornWolves; }

    public long getTotalDiedPlants() {
        return plantsEatenBySheep + plantsRemovedByWolves + plantsDiedOldAge;
    }

    public long getTotalDiedSheep() {
        return sheepEatenByWolves + sheepDiedOldAge + sheepDiedStarvation;
    }

    public long getTotalDiedWolves() {
        return wolvesDiedOldAge + wolvesDiedStarvation;
    }
}
