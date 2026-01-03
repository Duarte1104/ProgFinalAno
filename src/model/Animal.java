package model;

import world.WorldView;

import java.util.Random;

public abstract class Animal extends Organism {
    private int energy;
    private final int energyCostPerStep;

    protected Animal(Position position, int maxAge, int initialEnergy, int energyCostPerStep) {
        super(position, maxAge);
        this.energy = initialEnergy;
        this.energyCostPerStep = energyCostPerStep;
    }

    public final int getEnergy() { return energy; }

    protected final void addEnergy(int delta) {
        energy += delta;
    }

    @Override
    public void onStepStart() {
        super.onStepStart();
        if (!isAlive()) return;

        energy -= energyCostPerStep;
        if (energy <= 0) die();
    }

    @Override
    public boolean requiresMateForReproduction() {
        return true;
    }

    /** Decide para onde se quer mover (motor aplica depois). */
    public abstract Position chooseMoveTarget(WorldView world, Random rng);

    protected abstract boolean canEnterCell(Organism occupant);
}
