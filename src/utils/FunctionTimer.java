package utils;

import engines.Counter;
import game.Board;
import game.Move;

import java.util.ArrayList;
import java.util.List;

public class FunctionTimer {


    //            boolean test = MoveGenerator.isLegalMove(board, 48, 40, yes, yesYes, -1);
//            List<Move> test = MoveGenerator.generatePawnMoves(48, board, yes, yesYes, -1);
//            List<List<Integer>> test = MoveGenerator.getAttackedSquares(board, 0);
//            List<Move> moves = MoveGenerator.generateMoves(board);

    public static void main(String[] args) {
        Zobrist.initialize();
        Counter counter = new Counter();
        Board board = new Board();
        MoveGenerator.initialize();

        long startTime = System.nanoTime();

        counter.goPerft(board.positionToFen(), 3);

        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        System.out.println("Execution time: " + duration + " nanoseconds");
    }
}
