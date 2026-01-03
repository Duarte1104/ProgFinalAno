package sim;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import model.Species;
import ui.ConsoleRenderer;
import world.World;
import world.WorldConfig;

public final class Main {

    // Guarda uma “fotografia” das contagens num certo passo
    private static final class EvolutionPoint {
        final int step;
        final int plants;
        final int sheep;
        final int wolves;

        EvolutionPoint(int step, int plants, int sheep, int wolves) {
            this.step = step;
            this.plants = plants;
            this.sheep = sheep;
            this.wolves = wolves;
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Random rng = new Random();

        WorldConfig cfg = WorldConfig.defaultConfig();
        World world = World.fromConfig(cfg, rng);
        SimulationEngine engine = new SimulationEngine(world, rng);
        ConsoleRenderer renderer = new ConsoleRenderer();

        renderer.render(world, engine.getStepNumber(), engine.getStats());

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
                        renderer.render(world, engine.getStepNumber(), engine.getStats());

                        if (isAnySpeciesExtinct(world)) {
                            renderer.printExtinctionMessage(world);
                            break;
                        }
                    }
                }

                case "2" -> { // Correr N passos (no fim mostrar evolução)
                    int n = readInt(sc, "Quantos passos queres correr? ");
                    if (n <= 0) {
                        System.out.println("N tem de ser > 0.");
                        break;
                    }

                    List<EvolutionPoint> evo = new ArrayList<>();
                    evo.add(snapshot(world, engine.getStepNumber()));

                    for (int i = 0; i < n; i++) {
                        if (isAnySpeciesExtinct(world)) break;
                        engine.step();
                        evo.add(snapshot(world, engine.getStepNumber()));
                    }

                    renderer.render(world, engine.getStepNumber(), engine.getStats());
                    if (isAnySpeciesExtinct(world)) renderer.printExtinctionMessage(world);

                    // ✅ Mostrar evolução automaticamente
                    printEvolution(evo);
                }

                case "3" -> { // Correr até extinção (no fim mostrar evolução)
                    int maxSteps = readInt(sc, "Máximo de passos (ex: 100000): ");
                    if (maxSteps <= 0) maxSteps = 100000;

                    List<EvolutionPoint> evo = new ArrayList<>();
                    evo.add(snapshot(world, engine.getStepNumber()));

                    int ran = 0;
                    while (!isAnySpeciesExtinct(world) && ran < maxSteps) {
                        engine.step();
                        ran++;
                        evo.add(snapshot(world, engine.getStepNumber()));
                    }

                    renderer.render(world, engine.getStepNumber(), engine.getStats());

                    if (isAnySpeciesExtinct(world)) {
                        renderer.printExtinctionMessage(world);
                    } else {
                        System.out.println("Parou por atingir maxSteps=" + maxSteps + " sem extinção.");
                    }

                    // ✅ Mostrar evolução automaticamente
                    printEvolution(evo);
                }

                case "4" -> { // Reset
                    world.initializeRandom(cfg);
                    engine = new SimulationEngine(world, rng);
                    renderer.render(world, engine.getStepNumber(), engine.getStats());
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

    private static EvolutionPoint snapshot(World world, int step) {
        int plants = world.countSpecies(Species.PLANT);
        int sheep  = world.countSpecies(Species.SHEEP);
        int wolves = world.countSpecies(Species.WOLF);
        return new EvolutionPoint(step, plants, sheep, wolves);
    }

    /**
     * Mostra a evolução das contagens ao longo do tempo.
     * Se houver muitos passos, faz amostragem para não encher o ecrã.
     */
    private static void printEvolution(List<EvolutionPoint> evo) {
        if (evo == null || evo.isEmpty()) return;

        System.out.println();
        System.out.println("Evolução dos números ao longo do tempo (passos):");
        System.out.println("Passo | Plantas(*) | Ovelhas(O) | Lobos(W)");
        System.out.println("-----------------------------------------");

        int n = evo.size();

        // Se for pequeno, mostra tudo
        if (n <= 50) {
            for (EvolutionPoint p : evo) {
                System.out.printf("%5d | %9d | %10d | %7d%n", p.step, p.plants, p.sheep, p.wolves);
            }
            System.out.println();
            return;
        }

        // Se for grande, mostra: 5 primeiros, depois amostra, depois 5 últimos
        int first = 5;
        int last = 5;

        for (int i = 0; i < first; i++) {
            EvolutionPoint p = evo.get(i);
            System.out.printf("%5d | %9d | %10d | %7d%n", p.step, p.plants, p.sheep, p.wolves);
        }

        System.out.println("  ...  |    ...    |    ...     |   ...");

        int middleStart = first;
        int middleEnd = n - last;

        int desiredMiddleLines = 25;
        int middleCount = middleEnd - middleStart;
        int step = Math.max(1, middleCount / desiredMiddleLines);

        for (int i = middleStart; i < middleEnd; i += step) {
            EvolutionPoint p = evo.get(i);
            System.out.printf("%5d | %9d | %10d | %7d%n", p.step, p.plants, p.sheep, p.wolves);
        }

        System.out.println("  ...  |    ...    |    ...     |   ...");

        for (int i = n - last; i < n; i++) {
            EvolutionPoint p = evo.get(i);
            System.out.printf("%5d | %9d | %10d | %7d%n", p.step, p.plants, p.sheep, p.wolves);
        }

        System.out.println();
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
