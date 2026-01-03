package sim;

import model.Species;
import ui.ConsoleRenderer;
import world.World;
import world.WorldConfig;

import java.util.Random;
import java.util.Scanner;

public final class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        Random rng = new Random();

        WorldConfig cfg = WorldConfig.defaultConfig();
        World world = World.fromConfig(cfg, rng);
        SimulationEngine engine = new SimulationEngine(world, rng);
        ConsoleRenderer renderer = new ConsoleRenderer();

        renderer.render(world, engine.getStepNumber());

        while (true) {
            printMenu();
            String opt = sc.nextLine().trim();

            switch (opt) {
                case "1" -> {
                    System.out.println("ENTER para avançar 1 passo (ou escreve 'q' para voltar ao menu).");
                    while (true) {
                        String in = sc.nextLine();
                        if (in.trim().equalsIgnoreCase("q")) break;

                        if (isAnySpeciesExtinct(world)) {
                            renderer.printExtinctionMessage(world);
                            break;
                        }

                        engine.step();
                        renderer.render(world, engine.getStepNumber());

                        if (isAnySpeciesExtinct(world)) {
                            renderer.printExtinctionMessage(world);
                            break;
                        }
                    }
                }

                case "2" -> {
                    int n = readInt(sc, "Quantos passos queres correr? ");
                    if (n <= 0) {
                        System.out.println("N tem de ser > 0.");
                        break;
                    }

                    for (int i = 0; i < n; i++) {
                        if (isAnySpeciesExtinct(world)) break;
                        engine.step();
                    }

                    renderer.render(world, engine.getStepNumber());
                    if (isAnySpeciesExtinct(world)) renderer.printExtinctionMessage(world);
                }

                case "3" -> {
                    int maxSteps = readInt(sc, "Máximo de passos (ex: 100000): ");
                    if (maxSteps <= 0) maxSteps = 100000;

                    int ran = 0;
                    while (!isAnySpeciesExtinct(world) && ran < maxSteps) {
                        engine.step();
                        ran++;
                    }

                    renderer.render(world, engine.getStepNumber());
                    if (isAnySpeciesExtinct(world)) renderer.printExtinctionMessage(world);
                    else System.out.println("Parou por atingir maxSteps=" + maxSteps + " sem extinção.");
                }

                case "4" -> {
                    world.initializeRandom(cfg);
                    engine = new SimulationEngine(world, rng);
                    renderer.render(world, engine.getStepNumber());
                    System.out.println("Mundo reinicializado.");
                }

                case "0" -> {
                    System.out.println("A sair...");
                    return;
                }

                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("Menu:");
        System.out.println("1 - Passo-a-passo");
        System.out.println("2 - Correr N passos");
        System.out.println("3 - Correr até desaparecer uma espécie");
        System.out.println("4 - Reset (reinicializar mundo)");
        System.out.println("0 - Sair");
        System.out.print("> ");
    }

    private static boolean isAnySpeciesExtinct(World world) {
        return world.countSpecies(Species.PLANT) == 0
            || world.countSpecies(Species.SHEEP) == 0
            || world.countSpecies(Species.WOLF) == 0;
    }

    private static int readInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido. Tenta outra vez.");
            }
        }
    }
}
