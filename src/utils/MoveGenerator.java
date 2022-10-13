package utils;

import board.Board;
import board.Move;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MoveGenerator {

    private MoveGenerator() {
        throw new AssertionError();
    }

    public static List<Move> generateMoves(int x, int y, Board board) {
        List<Move> moves = new ArrayList<>();
        if (board.getTile(x, y) == null || !board.getTile(x, y).isColor(board.getColorToMove()) || board.getGameOver()) {
            return moves;
        }
        switch (board.getTile(x, y).getType()) {
            case "p":
                return generatePawnMoves(x, y, board);
            case "b":
                return generateBishopMoves(x, y, board);
            case "r":
                return generateRookMoves(x, y, board);
            case "q":
                return generateQueenMoves(x, y, board);
            case "k":
                String color = board.getTile(x, y).getColor();
                moves.addAll(generateKingMoves(x, y, board));
                moves.addAll(checkCastling(color, x, y, board));
                return moves;
            case "n":
                return generateKnightMoves(x, y, board);
            default:
                return moves;
        }
    }

    public static HashMap<int[], List<Move>> generateMovesForColorToMove(Board board) {
        HashMap<int[], List<Move>> moves = new HashMap<>();
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (board.getTile(x, y) == null || !board.getTile(x, y).isColor(board.getColorToMove()) || board.getGameOver()) {
                    continue;
                }
                int[] startPosition = {x, y};
                switch (board.getTile(x, y).getType()) {
                    case "p":
                        moves.put(startPosition, generatePawnMoves(x, y, board));
                        break;
                    case "b":
                        moves.put(startPosition, generateBishopMoves(x, y, board));
                        break;
                    case "r":
                        moves.put(startPosition, generateRookMoves(x, y, board));
                        break;
                    case "q":
                        moves.put(startPosition, generateQueenMoves(x, y, board));
                        break;
                    case "k":
                        List<Move> kingMoves = generateKingMoves(x, y, board);
                        kingMoves.addAll(checkCastling(board.getColorToMove(), x, y, board));
                        moves.put(startPosition, kingMoves);
                        break;
                    case "n":
                        moves.put(startPosition, generateKnightMoves(x, y, board));
                        break;
                    default:
                        return moves;
                }
            }
        }
        return moves;
    }

    public static List<Move> generatePawnMoves(int x, int y, Board board) {
        List<Move> moves = new ArrayList<>();
        int direction;
        String color = board.getTile(x, y).getColor();
        //set if pawn moves up or down the board
        if (color.equals("w")) {
            direction = -1;
        } else {
            direction = 1;
        }
        //if space in direction is free and move is legal, add move
        if (y + direction >= 0 && y + direction < 8 && board.getTile(x, y + direction) == null && board.isLegalMove(new Move(x, y, x, y + direction))) {
            moves.add(new Move(x, y, x, y + direction));
        }
        //if pawn has not yet moved, check if moving 2 tiles is possible and if the tile in front of you is empty
        if (!board.getTile(x, y).hasMoved() && y + (direction * 2) >= 0 && y + (direction * 2) < 8 && board.getTile(x, y + (direction * 2)) == null && board.getTile(x, y + direction) == null && board.isLegalMove(new Move(x, y, x, y + (direction * 2)))) {
            moves.add(new Move(x, y, x, y + (direction * 2)));
        }
        //check for available captures
        int[] leftAndRight = {-1, 1};
        for (int leftOrRight : leftAndRight) {
            if (!(x + leftOrRight >= 0 && x + leftOrRight < 8 && y + direction >= 0 && y + direction < 8)) {
                continue;
            }
            //check for regular capture
            if (board.getTile(x + leftOrRight, y + direction) != null && !board.getTile(x + leftOrRight, y + direction).isColor(color) && board.isLegalMove(new Move(x, y, x + leftOrRight, y + direction))) {
                moves.add(new Move(x, y, x + leftOrRight, y + direction));
            }
            //check for en passant
            if (board.getTile(x + leftOrRight, y) != null && board.getTile(x + leftOrRight, y).isEnPassantPossible() && !board.getTile(x + leftOrRight, y).isColor(color) && board.isLegalMove(new Move(x, y, x + leftOrRight, y + direction))) {
                moves.add(new Move(x, y, x + leftOrRight, y + direction));
            }
        }
        return moves;
    }

    public static List<Move> generateBishopMoves(int x, int y, Board board) {
        List<int[]> directions = generateSlidingPieceDirections("b");
        return scanDirectionUntilCollision(x, y, directions, board);
    }

    public static List<Move> generateKnightMoves(int x, int y, Board board) {
        List<int[]> directions = generateKnightDirections();
        return scanDirectionOnce(x, y, directions, board);
    }

    public static List<Move> generateRookMoves(int x, int y, Board board) {
        List<int[]> directions = generateSlidingPieceDirections("r");
        return scanDirectionUntilCollision(x, y, directions, board);
    }

    public static List<Move> generateQueenMoves(int x, int y, Board board) {
        List<Move> moves = new ArrayList<>();
        moves.addAll(generateRookMoves(x, y, board));
        moves.addAll(generateBishopMoves(x, y, board));
        return moves;
    }

    public static List<Move> generateKingMoves(int x, int y, Board board) {
        List<int[]> directions = generateSlidingPieceDirections("k");
        return scanDirectionOnce(x, y, directions, board);
    }

    public static List<Move> checkCastling(String color, int x, int y, Board board) {
        List<Move> castleMove = new ArrayList<>();
        if (board.getTile(x, y).hasMoved() || board.isCheck(color)) {
            return castleMove;
        }
        //if the tiles between king and rook are empty
        if (board.getTile(x + 1, y) == null && board.getTile(x + 2, y) == null) {
            //if there is a piece at the end, and it is your rook, which hasn't moved
            if (board.getTile(x + 3, y) != null && board.getTile(x + 3, y).isType("r") && board.getTile(x + 3, y).isColor(color) && !board.getTile(x + 3, y).hasMoved()) {
                //if move is legal
                if (board.isLegalMove(new Move(x, y, x + 1, y)) && board.isLegalMove(new Move(x, y, x + 2, y))) {
                    castleMove.add(new Move(x, y, x + 2, y));
                }
            }
        }
        //if the tiles between king and rook are empty
        if (board.getTile(x - 1, y) == null && board.getTile(x - 2, y) == null && board.getTile(x - 3, y) == null) {
            //if there is a piece at the end, and it is your rook, which hasn't moved
            if (board.getTile(x - 4, y) != null && board.getTile(x - 4, y).isType("r") && board.getTile(x - 4, y).isColor(color) && !board.getTile(x - 4, y).hasMoved()) {
                //if move is legal
                if (board.isLegalMove(new Move(x, y, x - 1, y)) && board.isLegalMove(new Move(x, y, x - 2, y))) {
                    castleMove.add(new Move(x, y, x - 2, y));
                }
            }
        }
        return castleMove;
    }

    public static String getCastleRights(int x, int y, Board board) {
        String castleRights = "";
        if (board.getTile(x, y).isType("k") && !board.getTile(x, y).hasMoved()) {
            if (board.getTile(x + 3, y) != null && board.getTile(x + 3, y).isType("r") && !board.getTile(x + 3, y).hasMoved()) {
                castleRights += "k";
            }
            if (board.getTile(x - 4, y) != null && board.getTile(x - 4, y).isType("r") && !board.getTile(x - 4, y).hasMoved()) {
                castleRights += "q";
            }
            return castleRights;
        }
        return "";
    }

    public static List<int[]> generateKnightDirections() {
        List<int[]> directions = new ArrayList<>();
        int[] direction1 = {-2, -1};
        directions.add(direction1);
        int[] direction2 = {-2, 1};
        directions.add(direction2);
        int[] direction3 = {-1, -2};
        directions.add(direction3);
        int[] direction4 = {-1, 2};
        directions.add(direction4);
        int[] direction5 = {1, -2};
        directions.add(direction5);
        int[] direction6 = {1, 2};
        directions.add(direction6);
        int[] direction7 = {2, -1};
        directions.add(direction7);
        int[] direction8 = {2, 1};
        directions.add(direction8);
        return directions;
    }

    public static List<int[]> generateSlidingPieceDirections(String piece) {
        List<int[]> directions = new ArrayList<>();
        int[] direction1 = {0, 1};
        int[] direction2 = {0, -1};
        int[] direction3 = {1, 0};
        int[] direction4 = {-1, 0};
        int[] direction5 = {-1, -1};
        int[] direction6 = {-1, 1};
        int[] direction7 = {1, -1};
        int[] direction8 = {1, 1};
        switch (piece) {
            case "k":
                directions.add(direction1);
                directions.add(direction2);
                directions.add(direction3);
                directions.add(direction4);
                directions.add(direction5);
                directions.add(direction6);
                directions.add(direction7);
                directions.add(direction8);
                break;
            case "b":
                directions.add(direction5);
                directions.add(direction6);
                directions.add(direction7);
                directions.add(direction8);
                break;
            case "r":
                directions.add(direction1);
                directions.add(direction2);
                directions.add(direction3);
                directions.add(direction4);
                break;
            default:
                return directions;
        }
        return directions;
    }

    public static List<Move> scanDirectionOnce(int x, int y, List<int[]> directions, Board board) {
        List<Move> moves = new ArrayList<>();
        String color = board.getTile(x, y).getColor();
        for (int[] direction : directions) {
            if (x + direction[0] >= 0 && x + direction[0] < 8 && y + direction[1] >= 0 && y + direction[1] < 8 && (board.getTile(x + direction[0], y + direction[1]) == null || !board.getTile(x + direction[0], y + direction[1]).isColor(color))) {
                if (board.isLegalMove(new Move(x, y, x + direction[0], y + direction[1]))) {
                    moves.add(new Move(x, y, x + direction[0], y + direction[1]));
                }
            }
        }
        return moves;
    }

    public static List<Move> scanDirectionUntilCollision(int x, int y, List<int[]> directions, Board board) {
        List<Move> moves = new ArrayList<>();
        String color = board.getTile(x, y).getColor();
        for (int[] direction : directions) {
            int currentX = x;
            int currentY = y;
            while (currentX + direction[0] >= 0 && currentX + direction[0] < 8 && currentY + direction[1] >= 0 && currentY + direction[1] < 8 && (board.getTile(currentX + direction[0], currentY + direction[1]) == null || !board.getTile(currentX + direction[0], currentY + direction[1]).isColor(color))) {
                currentX = currentX + direction[0];
                currentY = currentY + direction[1];
                if (board.isLegalMove(new Move(x, y, currentX, currentY))) {
                    moves.add(new Move(x, y, currentX, currentY));
                }
                if (board.getTile(currentX, currentY) != null && !board.getTile(currentX, currentY).isColor(color)) {
                    break;
                }
            }
        }
        return moves;
    }
}
