package model;

import world.WorldView;

import java.util.Random;

public final class Plant extends Organism {
    public static final int MAX_AGE = 20;
    public static final double REPRO_PROBABILITY = 0.10;

    public Plant(Position pos) {
        super(pos, MAX_AGE);
    }

    @Override
    public Species getSpecies() {
        return Species.PLANT;
    }

    @Override
    public double reproductionProbability() {
        return REPRO_PROBABILITY;
    }

    @Override
    public Organism createOffspring(Position pos) {
        return new Plant(pos);
    }

    public Position chooseReproductionTarget(WorldView world, Random rng) {
        return chooseRandomEmptyAdjacent(world, rng);
    }
}
