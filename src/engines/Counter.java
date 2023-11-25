package engines;

import game.Board;
import game.Move;
import game.Piece;
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

        List<Move> moves = MoveGenerator.generateMoves(board);
//        System.out.println("FOR THIS BOARD");
//        System.out.println(board);
//        System.out.println("THESE LEGAL MOVES: ");
//        for (Move move : moves) {
//            System.out.println(move);
//        }
//        System.out.println("WHITE PIECES: " + board.getPiecePositions(Piece.WHITE).toString());
//        System.out.println("BlACK PIECES: " + board.getPiecePositions(Piece.BLACK).toString());
//        int color = board.getColorToMove();
        int numPositions = 0;

        for (Move move : moves) {
//            System.out.println(move);
//            System.out.print(board.getPiecePositions(color).toString() + " -> ");
            int numBeforeLoop = numPositions;
            if (depth == this.depth) {
                System.out.print(move.toString() + ": ");
                results.append(move.toString()).append(": ");
            }
            board.makeMove(move);
//            System.out.print(board.getPiecePositions(color).toString() + " -> ");
            numPositions += countPositions(depth - 1, board);
            board.unmakeMove(move);
//            System.out.println(board.getPiecePositions(color).toString());
            if (depth == this.depth) {
                System.out.println(numPositions - numBeforeLoop);
                results.append(numPositions - numBeforeLoop).append("~");
            }
        }

        return numPositions;
    }
}
