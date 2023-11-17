package engines;

import game.Board;
import game.Move;
import utils.MoveGenerator;
import utils.Utils;

import java.util.HashMap;
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

//        System.out.println("FOR THIS BOARD");
//        System.out.println(board);
//        System.out.println("THESE LEGAL MOVES: ");
        List<Move> moves = MoveGenerator.generateMoves(board);
//        for (var entry : moves.entrySet()) {
//            System.out.println(entry.getKey() + ": " + entry.getValue().toString());
//        }
//        System.out.println("WHITE PIECES: " + board.getPiecePositions("w").toString());
//        System.out.println("BlACK PIECES: " + board.getPiecePositions("b").toString());
        int numPositions = 0;

        for (Move move : moves) {
            Board boardCopy = board.deepCopy();
            int numBeforeLoop = numPositions;
            if (depth == this.depth) {
                System.out.print(move.toString() + ": ");
                results.append(move.toString()).append(": ");
            }
            boardCopy.makeMove(move);
            numPositions += countPositions(depth - 1, boardCopy);
            boardCopy = board.deepCopy();
            if (depth == this.depth) {
                System.out.println(numPositions - numBeforeLoop);
                results.append(numPositions - numBeforeLoop).append("~");
            }
        }

        return numPositions;
    }
}
