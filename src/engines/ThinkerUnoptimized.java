package engines;

import board.Board;
import board.Move;
import utils.MoveGenerator;
import utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThinkerUnoptimized implements Engine {
    private final String name;
    private final String color;
    private final int depth;

    private HashMap<String, Integer> pieceValues;

    public ThinkerUnoptimized(String color) {
        this.color = color;
        this.name = "thinker";
        initializePieceValues();
        this.depth = 2;
    }

    @Override
    public Move determineMove(Board board) {
        Move bestMove = new Move();
        int bestMoveEval = Integer.MIN_VALUE;

        HashMap<int[], List<Move>> moves = MoveGenerator.generateMovesForColorToMove(board);
        if (moves.size() == 0) {
            return null;
        }

        for (int[] startPosition : moves.keySet()) {
            Board boardCopy = board.deepCopy();
            for (Move move : moves.get(startPosition)) {
                boardCopy.makeMove(move);
                int moveEval = miniMax(boardCopy, this.depth);
                boardCopy = board.deepCopy();
                if (moveEval > bestMoveEval) {
                    bestMove = move;
                    bestMoveEval = moveEval;
                }
            }
        }
        System.out.println(bestMoveEval + " " + bestMove.toString());
        return bestMove;
    }

    private int miniMax(Board board, int depth) {
        if (depth == 0) {
            return evaluate(board);
        }

        HashMap<int[], List<Move>> moves = MoveGenerator.generateMovesForColorToMove(board);
        int bestEval = Integer.MIN_VALUE;

        for (int[] startPosition : moves.keySet()) {
            Board boardCopy = board.deepCopy();
            for (Move move : moves.get(startPosition)) {
                boardCopy.makeMove(move);
                int moveEval = -miniMax(boardCopy, depth - 1);
                bestEval = Math.max(bestEval, moveEval);
                boardCopy = board.deepCopy();
            }
        }
        return bestEval;
    }

    private int evaluate(Board board) {
        int whiteEval = 0;
        int blackEval = 0;

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (board.getTile(x, y) == null || board.isKing(x, y)) {
                    continue;
                }
                int value = pieceValues.get(board.getTile(x, y).getType());

                if (board.getTile(x, y).isColor("w")) {
                    whiteEval += value;
                } else {
                    blackEval += value;
                }

            }
        }

        int evaluation = whiteEval - blackEval;
        int perspective = (board.getColorToMove().equals("w")) ? -1 : 1;
        String thing = (evaluation > 0) ? "White is better" : ((evaluation == 0) ? "Equal" : "Black is better");
        System.out.println(thing);
//        board.printBoard();
//        System.out.println(evaluation * perspective);
//        System.out.println();
        return evaluation * perspective;
    }

    private void initializePieceValues() {
        this.pieceValues = new HashMap<>();
        this.pieceValues.put("p", 100);
        this.pieceValues.put("b", 300);
        this.pieceValues.put("n", 300);
        this.pieceValues.put("r", 500);
        this.pieceValues.put("q", 900);
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
