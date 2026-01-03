package world;

public final class WorldConfig {
    private final int width;
    private final int height;

    private final double wolfProb;
    private final double sheepProb;
    private final double plantProb;

    public WorldConfig(int width, int height, double wolfProb, double sheepProb, double plantProb) {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Dimensões inválidas.");
        if (wolfProb < 0 || sheepProb < 0 || plantProb < 0) throw new IllegalArgumentException("Probabilidades inválidas.");
        if (wolfProb + sheepProb + plantProb > 1.0 + 1e-12) {
            throw new IllegalArgumentException("Soma das probabilidades excede 1.0");
        }
        this.width = width;
        this.height = height;
        this.wolfProb = wolfProb;
        this.sheepProb = sheepProb;
        this.plantProb = plantProb;
    }

    public static WorldConfig defaultConfig() {
        return new WorldConfig(20, 20, 0.04, 0.12, 0.75);
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public double getWolfProb() { return wolfProb; }
    public double getSheepProb() { return sheepProb; }
    public double getPlantProb() { return plantProb; }
}
