package engines;

import game.Board;
import game.Move;
import utils.MoveGenerator;

import java.util.HashMap;
import java.util.List;

public class Random implements Engine{
    private final String name = "Random";
    private final String color;

    public Random(String color) {
        this.color = color;
    }

    @Override
    public Move determineMove(Board board) {
        List<Move> moves = MoveGenerator.generateMoves(board);
        int move = (int) (Math.random() * moves.size());
        return moves.get(move);
    }

    @Override
    public boolean isWhite() {
        return color.equals("w");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getResults() {
        return null;
    }

    @Override
    public String toString() {
        return name;
    }
}
