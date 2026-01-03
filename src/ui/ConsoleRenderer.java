package ui;

import java.util.List;
import model.Species;
import sim.SimulationStats;
import world.World;

public final class ConsoleRenderer {

    public void render(World world, int stepNumber, SimulationStats stats) {
        System.out.println();
        System.out.println("Passo " + stepNumber);

        List<String> lines = world.toTextLines();
        for (String line : lines) {
            System.out.println(line);
        }

        int plants = world.countSpecies(Species.PLANT);
        int sheep  = world.countSpecies(Species.SHEEP);
        int wolves = world.countSpecies(Species.WOLF);

        System.out.println("Vivos  -> Plantas(*): " + plants + " | Ovelhas(O): " + sheep + " | Lobos(W): " + wolves);

        if (stats != null) {
            System.out.println("Criados-> Plantas(*): " + stats.getCreatedPlants()
                    + " | Ovelhas(O): " + stats.getCreatedSheep()
                    + " | Lobos(W): " + stats.getCreatedWolves());

            System.out.println("Mortos -> Plantas(*): " + stats.getDiedPlants()
                    + " | Ovelhas(O): " + stats.getDiedSheep()
                    + " | Lobos(W): " + stats.getDiedWolves());
        }

        System.out.println();
    }

    public void printExtinctionMessage(World world) {
        if (world.countSpecies(Species.PLANT) == 0) System.out.println("Extinção: Plantas (*) desapareceram.");
        if (world.countSpecies(Species.SHEEP) == 0) System.out.println("Extinção: Ovelhas (O) desapareceram.");
        if (world.countSpecies(Species.WOLF) == 0) System.out.println("Extinção: Lobos (W) desapareceram.");
    }
}
