package model;

import world.WorldView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public abstract class Organism {
    private Position position;
    private int age;
    private final int maxAge;
    private boolean alive = true;

    protected Organism(Position position, int maxAge) {
        this.position = position;
        this.maxAge = maxAge;
        this.age = 0;
    }

    public final Position getPosition() { return position; }
    public final void setPosition(Position position) { this.position = position; }

    public final int getAge() { return age; }
    public final int getMaxAge() { return maxAge; }

    public final boolean isAlive() { return alive; }
    public final void die() { alive = false; }

    public abstract Species getSpecies();
    public final char getSymbol() { return getSpecies().symbol(); }

    /** +1 idade por passo; morre se passar maxAge. */
    public void onStepStart() {
        age++;
        if (age > maxAge) die();
    }

    /** Para Cenário 2: plantas não precisam de par; animais normalmente sim. */
    public boolean requiresMateForReproduction() { return false; }

    /** Para Cenário 2: animais sobrescrevem (idade/energia). */
    public boolean isAdultForReproduction() { return true; }

    public abstract double reproductionProbability();
    public abstract Organism createOffspring(Position pos);

    /** Escolhe vizinho vazio aleatório (N/S/E/W); se não houver, devolve null. */
    protected final Position chooseRandomEmptyAdjacent(WorldView world, Random rng) {
        List<Position> adj = new ArrayList<>(world.getAdjacent4(getPosition()));
        Collections.shuffle(adj, rng);
        for (Position p : adj) {
            if (world.isEmpty(p)) return p;
        }
        return null;
    }
}
