package engines;

import game.Board;
import game.Move;
import game.Piece;
import utils.MoveGenerator;

import java.util.HashMap;
import java.util.List;

public class Greedy implements Engine {
    private final String name = "Greedy";
    private final String color;
    private final int depth;

    private final int alpha = Integer.MIN_VALUE;
    private final int beta = Integer.MAX_VALUE;

    private HashMap<Integer, Integer> pieceValues;

    public Greedy(String color) {
        this.color = color;
        initializePieceValues();
        this.depth = 4;
    }

    @Override
    public Move determineMove(Board board) {
        Move bestMove = new Move();
        int bestMoveEval = Integer.MIN_VALUE;
        int perspective = color.equals("w") ? 1 : -1;

        List<Move> moves = MoveGenerator.generateMoves(board);
        if (moves.size() == 0) {
            return null;
        }

        int count = 0;
        for (Move move : moves) {
            Board boardCopy = board.deepCopy();
            boardCopy.makeMove(move);
            boolean maximizingPlayer = boardCopy.getTurn() == Piece.WHITE;
            int[] moveEval = miniMax(boardCopy, this.depth, maximizingPlayer);
            count += moveEval[1];
            boardCopy = board.deepCopy();

            if (moveEval[0] * perspective > bestMoveEval) {
                bestMove = move;
                bestMoveEval = moveEval[0] * perspective;
            }
        }
        System.out.println(count);
        return bestMove;
    }

    private int[] miniMax(Board board, int depth, boolean maximizingPlayer) {
        if (depth == 0) {
            return new int[]{evaluate(board), 1};
        }

        List<Move> moves = MoveGenerator.generateMoves(board);
        int bestEval = (maximizingPlayer) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int count = 0;

        for (Move move : moves) {
                Board boardCopy = board.deepCopy();
                boardCopy.makeMove(move);
                int[] moveEval = miniMax(boardCopy, depth - 1, !maximizingPlayer);
                count += moveEval[1];
                bestEval = (maximizingPlayer) ? Math.max(bestEval, moveEval[0]) : Math.min(bestEval, moveEval[0]);
        }
        return new int[]{bestEval, count};
    }

    private int evaluate(Board board) {
        int whiteEval = 0;
        int blackEval = 0;

        for (int index = 0; index < 64; index++) {
            if (board.isEmpty(index) || board.isKing(index)) {
                continue;
            }
            int value = pieceValues.get(board.getPieceType(index));

            if (board.isColor(index, Piece.WHITE)) {
                whiteEval += value;
            } else {
                blackEval += value;
            }

        }
        return whiteEval - blackEval;
    }

    private void initializePieceValues() {
        this.pieceValues = new HashMap<>();
        this.pieceValues.put(Piece.PAWN, 100);
        this.pieceValues.put(Piece.BISHOP, 300);
        this.pieceValues.put(Piece.KNIGHT, 300);
        this.pieceValues.put(Piece.ROOK, 500);
        this.pieceValues.put(Piece.QUEEN, 900);
    }

    @Override
    public String toString() {
        return "Greedy";
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
