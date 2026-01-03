package model;

public enum Species {
    PLANT('*'),
    SHEEP('O'),
    WOLF('W');

    private final char symbol;

    Species(char symbol) {
        this.symbol = symbol;
    }

    public char symbol() {
        return symbol;
    }
}
