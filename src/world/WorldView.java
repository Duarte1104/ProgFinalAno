package world;

import model.Organism;
import model.Position;

import java.util.List;

public interface WorldView {
    int getWidth();
    int getHeight();

    boolean isInside(Position p);

    Organism getAt(Position p);

    default boolean isEmpty(Position p) {
        return getAt(p) == null;
    }

    List<Position> getAdjacent4(Position p);
}
