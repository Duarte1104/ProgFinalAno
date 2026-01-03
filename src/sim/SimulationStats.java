package sim;

import model.Species;
import world.World;

/** Estatísticas simples: criados e mortos por espécie. */
public final class SimulationStats {

    private long createdPlants, createdSheep, createdWolves;
    private long diedPlants, diedSheep, diedWolves;

    /** Inicializa "criados" com os organismos iniciais já presentes no mundo. */
    public SimulationStats(World world) {
        this.createdPlants = world.countSpecies(Species.PLANT);
        this.createdSheep  = world.countSpecies(Species.SHEEP);
        this.createdWolves = world.countSpecies(Species.WOLF);
    }

    public void onCreated(Species s, long n) {
        if (n <= 0) return;
        switch (s) {
            case PLANT -> createdPlants += n;
            case SHEEP -> createdSheep += n;
            case WOLF  -> createdWolves += n;
        }
    }

    public void onDied(Species s, long n) {
        if (n <= 0) return;
        switch (s) {
            case PLANT -> diedPlants += n;
            case SHEEP -> diedSheep += n;
            case WOLF  -> diedWolves += n;
        }
    }

    public long getCreatedPlants() { return createdPlants; }
    public long getCreatedSheep()  { return createdSheep; }
    public long getCreatedWolves() { return createdWolves; }

    public long getDiedPlants() { return diedPlants; }
    public long getDiedSheep()  { return diedSheep; }
    public long getDiedWolves() { return diedWolves; }
}
