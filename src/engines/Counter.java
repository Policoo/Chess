package engines;

import game.Board;
import game.Move;
import utils.MoveGenerator;
import utils.Utils;

import java.util.HashMap;
import java.util.List;

public class Counter implements Engine {
    private final String name = "Counter";
    private final String color;
    private final int depth;
    private StringBuilder results;

    public Counter(String color) {
        this.color = color;
        this.depth = 5;
    }

    @Override
    public String getResults() {
        return this.results.toString();
    }

    @Override
    public Move determineMove(Board board) {
        this.results = new StringBuilder();
        System.out.println(moveGenerationTest(depth, board));
        return new Move();
    }

    @Override
    public boolean isWhite() {
        return color.equals("w");
    }

    @Override
    public String getName() {
        return name;
    }

    private int moveGenerationTest(int depth, Board board) {
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
                results.append(move.toString()).append(":");
            }
            boardCopy.makeMove(move);
            numPositions += moveGenerationTest(depth - 1, boardCopy);
            boardCopy = board.deepCopy();
            if (depth == this.depth) {
                System.out.println(numPositions - numBeforeLoop);
                results.append(numPositions - numBeforeLoop).append("`");
            }
        }

        return numPositions;
    }
}
