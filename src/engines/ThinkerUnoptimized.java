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

    private final int alpha = Integer.MIN_VALUE;
    private final int beta = Integer.MAX_VALUE;

    private HashMap<String, Integer> pieceValues;

    public ThinkerUnoptimized(String color) {
        this.color = color;
        this.name = "thinker";
        initializePieceValues();
        this.depth = 4;
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
        int count = 0;
        for (int[] startPosition : moves.keySet()) {
            Board boardCopy = board.deepCopy();
            for (Move move : moves.get(startPosition)) {
                boardCopy.makeMove(move);
                boolean maximizingPlayer = boardCopy.getColorToMove().equals("w");
                //int[] moveEval = miniMax(boardCopy, this.depth, maximizingPlayer);
                int[] moveEval = miniMaxPruning(boardCopy, this.depth, maximizingPlayer, alpha, beta);
                count += moveEval[1];
                boardCopy = board.deepCopy();
                if (moveEval[0] * perspective > bestMoveEval) {
                    bestMove = move;
                    bestMoveEval = moveEval[0] * perspective;
                }
            }
        }
        System.out.println(count);
        return bestMove;
    }

    private int[] miniMax(Board board, int depth, boolean maximizingPlayer) {
        if (depth == 0) {
            return new int[]{evaluate(board), 1};
        }

        HashMap<int[], List<Move>> moves = MoveGenerator.generateMovesForColorToMove(board);
        int bestEval = (maximizingPlayer) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int count = 0;

        for (int[] startPosition : moves.keySet()) {
            for (Move move : moves.get(startPosition)) {
                Board boardCopy = board.deepCopy();
                boardCopy.makeMove(move);
                int[] moveEval = miniMax(boardCopy, depth - 1, !maximizingPlayer);
                count += moveEval[1];
                bestEval = (maximizingPlayer) ? Math.max(bestEval, moveEval[0]) : Math.min(bestEval, moveEval[0]);
            }
        }
        return new int[]{bestEval, count};
    }

    private int[] miniMaxPruning(Board board, int depth, boolean maximizingPlayer, int alpha, int beta) {
        if (depth == 0) {
            return new int[]{evaluate(board), 1};
        }

        HashMap<int[], List<Move>> moves = MoveGenerator.generateMovesForColorToMove(board);
        int bestEval = (maximizingPlayer) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int count = 0;

        for (int[] startPosition : moves.keySet()) {
            for (Move move : moves.get(startPosition)) {
                Board boardCopy = board.deepCopy();
                boardCopy.makeMove(move);
                int[] moveEval = miniMaxPruning(boardCopy, depth - 1, !maximizingPlayer, alpha, beta);
                count += moveEval[1];
                bestEval = (maximizingPlayer) ? Math.max(bestEval, moveEval[0]) : Math.min(bestEval, moveEval[0]);
                if (maximizingPlayer) {
                    alpha = Math.max(alpha, moveEval[0]);
                }
                else {
                    beta = Math.min(beta, moveEval[0]);
                }
                if (beta <= alpha) {
                    break;
                }
            }
        }
        return new int[]{bestEval, count};
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
