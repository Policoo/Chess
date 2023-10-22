package engines;

import game.Board;
import game.Move;

public interface Engine {

    Move determineMove(Board board);

    boolean isWhite();

    String getName();

    String getResults();
}
