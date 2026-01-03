package world;

import model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class World implements WorldView {

    private final int width;
    private final int height;
    private final Organism[][] grid; // [y][x]
    private final List<Organism> organisms;
    private final Random rng;

    public World(int width, int height, Random rng) {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Dimensões inválidas.");
        this.width = width;
        this.height = height;
        this.grid = new Organism[height][width];
        this.organisms = new ArrayList<>();
        this.rng = Objects.requireNonNull(rng);
    }

    public static World fromConfig(WorldConfig config, Random rng) {
        World w = new World(config.getWidth(), config.getHeight(), rng);
        w.initializeRandom(config);
        return w;
    }

    @Override
    public int getWidth() { return width; }

    @Override
    public int getHeight() { return height; }

    @Override
    public boolean isInside(Position p) {
        return p != null && p.x() >= 0 && p.x() < width && p.y() >= 0 && p.y() < height;
    }

    @Override
    public Organism getAt(Position p) {
        if (!isInside(p)) return null;
        return grid[p.y()][p.x()];
    }

    @Override
    public List<Position> getAdjacent4(Position p) {
        if (!isInside(p)) return List.of();

        List<Position> res = new ArrayList<>(4);

        Position n = p.translate(0, -1);
        Position s = p.translate(0, 1);
        Position e = p.translate(1, 0);
        Position w = p.translate(-1, 0);

        if (isInside(n)) res.add(n);
        if (isInside(s)) res.add(s);
        if (isInside(e)) res.add(e);
        if (isInside(w)) res.add(w);

        return res;
    }

    public void place(Organism o, Position pos) {
        Objects.requireNonNull(o);
        Objects.requireNonNull(pos);

        if (!isInside(pos)) throw new IllegalArgumentException("Fora da grelha: " + pos);
        if (grid[pos.y()][pos.x()] != null) throw new IllegalStateException("Célula ocupada: " + pos);

        o.setPosition(pos);
        grid[pos.y()][pos.x()] = o;
        organisms.add(o);
    }

    public void moveToEmpty(Organism o, Position newPos) {
        Objects.requireNonNull(o);
        Objects.requireNonNull(newPos);

        if (!isInside(newPos)) throw new IllegalArgumentException("Destino fora: " + newPos);
        if (grid[newPos.y()][newPos.x()] != null) throw new IllegalStateException("Destino não vazio: " + newPos);

        Position old = o.getPosition();
        grid[old.y()][old.x()] = null;

        o.setPosition(newPos);
        grid[newPos.y()][newPos.x()] = o;
    }

    /** Move para célula vazia ou ocupada; se ocupada remove e devolve o ocupante. */
    public Organism moveInto(Organism mover, Position newPos) {
        Objects.requireNonNull(mover);
        Objects.requireNonNull(newPos);

        if (!isInside(newPos)) throw new IllegalArgumentException("Destino fora: " + newPos);

        Position old = mover.getPosition();
        if (old.equals(newPos)) return null;

        Organism occupant = grid[newPos.y()][newPos.x()];
        if (occupant != null) {
            grid[newPos.y()][newPos.x()] = null;
            organisms.remove(occupant);
        }

        if (grid[old.y()][old.x()] != mover) {
            throw new IllegalStateException("Inconsistência: mover não está na origem.");
        }

        grid[old.y()][old.x()] = null;
        mover.setPosition(newPos);
        grid[newPos.y()][newPos.x()] = mover;

        return occupant;
    }

    public void remove(Organism o) {
        if (o == null) return;
        Position p = o.getPosition();
        if (isInside(p) && grid[p.y()][p.x()] == o) {
            grid[p.y()][p.x()] = null;
        }
        organisms.remove(o);
    }

    public List<Organism> getOrganismsSnapshot() {
        return new ArrayList<>(organisms);
    }

    public int countSpecies(Species s) {
        int c = 0;
        for (Organism o : organisms) {
            if (o.isAlive() && o.getSpecies() == s) c++;
        }
        return c;
    }

    public void initializeRandom(WorldConfig cfg) {
        clearAll();

        double pW = cfg.getWolfProb();
        double pO = cfg.getSheepProb();
        double pP = cfg.getPlantProb();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double r = rng.nextDouble();
                Position pos = new Position(x, y);

                if (r < pW) place(new Wolf(pos), pos);
                else if (r < pW + pO) place(new Sheep(pos), pos);
                else if (r < pW + pO + pP) place(new Plant(pos), pos);
            }
        }
    }

    public void clearAll() {
        organisms.clear();
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                grid[y][x] = null;
    }

    public char[][] toCharMatrix() {
        char[][] m = new char[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Organism o = grid[y][x];
                m[y][x] = (o == null) ? '.' : o.getSymbol();
            }
        }
        return m;
    }

    public List<String> toTextLines() {
        char[][] m = toCharMatrix();
        List<String> lines = new ArrayList<>();

        String border = "+" + "-".repeat(width * 2 + 1) + "+";
        lines.add(border);

        for (int y = 0; y < height; y++) {
            StringBuilder sb = new StringBuilder();
            sb.append("|");
            for (int x = 0; x < width; x++) {
                sb.append(m[y][x]);
                if (x < width - 1) sb.append(' ');
            }
            sb.append("|");
            lines.add(sb.toString());
        }

        lines.add(border);
        return Collections.unmodifiableList(lines);
    }
}
