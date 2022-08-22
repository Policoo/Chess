package engines;

import board.Board;
import utils.MoveGenerator;
import utils.Utils;

import java.util.HashMap;
import java.util.List;

public class Counter implements Engine{
    private final String name;
    private final String color;
    private final int depth;
    private StringBuilder results;

    public Counter(String color) {
        this.color = color;
        this.depth = 5;
        this.name = "counter";
    }

    @Override
    public String getResults() {
        return this.results.toString();
    }

    @Override
    public int[] determineMove(Board board) {
        this.results = new StringBuilder();
        System.out.println(moveGenerationTest(depth, board));
        return new int[0];
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
        HashMap<int[], List<int[]>> moves = MoveGenerator.generateMovesForColorToMove(board);
        int numPositions = 0;

        for (int[] startPosition : moves.keySet()) {
            Board boardCopy = board.deepCopy();
            for (int[] move : moves.get(startPosition)) {
                int numBeforeLoop = numPositions;
                if (depth == this.depth) {
                    System.out.print(Utils.getChessCoordinates(startPosition[0], startPosition[1]) + Utils.getChessCoordinates(move[0], move[1]) + ": ");
                    results.append(Utils.getChessCoordinates(startPosition[0], startPosition[1])).append(Utils.getChessCoordinates(move[0], move[1])).append(":");
                }
                boardCopy.makeMove(startPosition[0], startPosition[1], move[0], move[1]);
                numPositions += moveGenerationTest(depth - 1, boardCopy);
                boardCopy = board.deepCopy();
                if (depth == this.depth) {
                    System.out.println(numPositions - numBeforeLoop);
                    results.append(numPositions - numBeforeLoop).append("`");
                }
            }
        }
        return numPositions;
    }
}
