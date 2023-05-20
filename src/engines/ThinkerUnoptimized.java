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
        this.depth = 1;
    }

    @Override
    public Move determineMove(Board board) {
        Move bestMove = new Move();
        int bestMoveEval = Integer.MIN_VALUE;
        int perspective = board.getColorToMove().equals("w") ? 1 : -1;

        HashMap<int[], List<Move>> moves = MoveGenerator.generateMovesForColorToMove(board);
        if (moves.size() == 0) {
            return null;
        }

        for (int[] startPosition : moves.keySet()) {
            Board boardCopy = board.deepCopy();
            for (Move move : moves.get(startPosition)) {
                boardCopy.makeMove(move);
                int moveEval = miniMax(boardCopy, this.depth);
                System.out.println(move + ": " + moveEval * perspective);
                boardCopy = board.deepCopy();
                System.out.println("CURRENT BEST MOVE: " + bestMove + " - " + bestMoveEval);
                if (moveEval * perspective > bestMoveEval) {
                    System.out.println(moveEval * perspective + " > " + bestMoveEval + " => NEW BEST MOVE: " + move);
                    bestMove = move;
                    bestMoveEval = moveEval * perspective;
                }
                else {
                    System.out.println(moveEval * perspective + " < " + bestMoveEval);
                }
            }
        }
        System.out.println("");
        System.out.println("");
        System.out.println("");
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
        return whiteEval - blackEval;
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
