package engines;

import board.Board;
import board.Move;
import utils.MoveGenerator;
import utils.Utils;

import java.util.List;

public class Random implements Engine{
    private final String name;
    private final String color;

    public Random(String color) {
        this.color = color;
        this.name = "random";
    }

    @Override
    public Move determineMove(Board board) {
        int[] finalMove = new int[4];
        int x = (int) (Math.random() * 7);
        int y = (int) (Math.random() * 7);
        List<Move> moves = MoveGenerator.generateMoves(x, y, board);
        while (board.getTile(x, y) == null || !board.getTile(x, y).isColor(color) || moves.size() == 0) {
            x = (int) (Math.random() * 7);
            y = (int) (Math.random() * 7);
            moves = MoveGenerator.generateMoves(x, y, board);
        }
        int move = (int) (Math.random() * (moves.size() - 1));
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
}
