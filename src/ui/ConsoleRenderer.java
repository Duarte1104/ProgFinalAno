package ui;

import model.Species;
import world.World;

import java.util.List;

public final class ConsoleRenderer {

    public void render(World world, int stepNumber) {
        System.out.println();
        System.out.println("Passo " + stepNumber);

        List<String> lines = world.toTextLines();
        for (String line : lines) {
            System.out.println(line);
        }

        int plants = world.countSpecies(Species.PLANT);
        int sheep  = world.countSpecies(Species.SHEEP);
        int wolves = world.countSpecies(Species.WOLF);

        System.out.println("Plantas(*): " + plants + " | Ovelhas(O): " + sheep + " | Lobos(W): " + wolves);
        System.out.println();
    }

    public void printExtinctionMessage(World world) {
        if (world.countSpecies(Species.PLANT) == 0) System.out.println("Extinção: Plantas (*) desapareceram.");
        if (world.countSpecies(Species.SHEEP) == 0) System.out.println("Extinção: Ovelhas (O) desapareceram.");
        if (world.countSpecies(Species.WOLF) == 0) System.out.println("Extinção: Lobos (W) desapareceram.");
    }
}
