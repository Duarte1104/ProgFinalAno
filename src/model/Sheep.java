package model;

import world.WorldView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class Sheep extends Animal {
    public static final int MAX_AGE = 30;

    public static final int INITIAL_ENERGY = 10;
    public static final int ENERGY_COST_PER_STEP = 1;
    public static final int ENERGY_GAIN_FROM_PLANT = 5;

    // Cenário 2
    public static final int MIN_REPRO_AGE = 5;
    public static final int MIN_REPRO_ENERGY = 20;
    public static final double REPRO_PROBABILITY = 0.30;

    public Sheep(Position pos) {
        super(pos, MAX_AGE, INITIAL_ENERGY, ENERGY_COST_PER_STEP);
    }

    @Override
    public Species getSpecies() {
        return Species.SHEEP;
    }

    @Override
    protected boolean canEnterCell(Organism occupant) {
        if (occupant == null) return true;
        return occupant.getSpecies() == Species.PLANT;
    }

    @Override
    public Position chooseMoveTarget(WorldView world, Random rng) {
        List<Position> candidates = new ArrayList<>(world.getAdjacent4(getPosition()));
        Collections.shuffle(candidates, rng);

        for (Position p : candidates) {
            Organism occ = world.getAt(p);
            if (canEnterCell(occ)) return p;
        }
        return getPosition();
    }

    public void eatPlant() {
        addEnergy(ENERGY_GAIN_FROM_PLANT);
    }

    @Override
    public boolean isAdultForReproduction() {
        return getAge() >= MIN_REPRO_AGE && getEnergy() >= MIN_REPRO_ENERGY;
    }

    @Override
    public double reproductionProbability() {
        return REPRO_PROBABILITY;
    }

    @Override
    public Organism createOffspring(Position pos) {
        return new Sheep(pos);
    }
}
