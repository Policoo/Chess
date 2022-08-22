package engines;

import board.Board;

public interface Engine {

    int[] determineMove(Board board);

    boolean isWhite();

    String getName();

    String getResults();
}
