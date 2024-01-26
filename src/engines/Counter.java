package engines;

import game.Board;
import game.Move;
import utils.MoveGenerator;

import java.util.LinkedHashMap;
import java.util.List;

public class Counter {
    private LinkedHashMap<String, Integer> results;
    private int depth;

    public Counter() {

    }

    public LinkedHashMap<String, Integer> goPerft(String fenString, int depth) {
        this.depth = depth;
        this.results = new LinkedHashMap<>();

        Board board = new Board(fenString);
        int numNodes = countPositions(board, depth);

        results.put("Nodes searched", numNodes);
        return results;
    }

    private int countPositions(Board board, int depth) {
        if (depth == 0) {
            return 1;
        }

        List<Move> moves = MoveGenerator.generateMoves(board);
        int numPositions = 0;

        for (Move move : moves) {
            board.makeMove(move);
            int movePositions = countPositions(board, depth - 1);
            board.undoMove(move);

            numPositions += movePositions;
            if (depth == this.depth) {
                System.out.println(move + ": " + movePositions);
                results.put(move.toString(), movePositions);
            }
        }

        return numPositions;
    }
}
