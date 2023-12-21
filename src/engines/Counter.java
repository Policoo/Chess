package engines;

import game.Board;
import game.Move;
import utils.MoveGenerator;

import java.util.List;

public class Counter {
    private StringBuilder results;
    private int depth;

    public Counter() {

    }

    public String goPerft(Board board, int depth) {
        this.depth = depth;
        this.results = new StringBuilder();
        int numNodes = countPositions(depth, board);
        results.append("Nodes searched: ").append(numNodes).append("~");
        return results.toString();
    }

    private int countPositions(int depth, Board board) {
        if (depth == 0) {
            return 1;
        }

        List<Move> moves = MoveGenerator.generateMoves(board);
        int numPositions = 0;

        for (Move move : moves) {
            board.makeMove(move);
            int movePositions = countPositions(depth - 1, board);
            board.undoMove(move);
            numPositions += movePositions;
            if (depth == this.depth) {
                System.out.println(move.toString() + ": " + movePositions);
                results.append(move.toString()).append(": ").append(movePositions).append("~");
            }
        }

        return numPositions;
    }
}
