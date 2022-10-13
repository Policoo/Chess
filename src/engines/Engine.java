package engines;

import board.Board;
import board.Move;

public interface Engine {

    Move determineMove(Board board);

    boolean isWhite();

    String getName();

    String getResults();
}
