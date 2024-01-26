package utils;

import game.Board;
import game.Move;
import game.Piece;
import game.PrecomputedGameData;

import java.io.PipedInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MoveGenerator {
    public static List<Move> generateMoves(Board board) {
        if (board.isGameOver()) {
            return new ArrayList<>();
        }

        List<Move> moves = new ArrayList<>();
        int color = board.getTurn();
        List<Integer> piecePositions = board.getPiecePositions(color);

        for (int index : piecePositions) {
            if (index == -1) {
                continue;
            }

            switch (board.getPieceType(index)) {
                case Piece.PAWN:
                    List<Move> pawnMoves = generatePawnMoves(board, index);
                    if (pawnMoves.size() > 0) {
                        moves.addAll(pawnMoves);
                    }
                    break;
                case Piece.BISHOP:
                    List<Move> bishopMoves = generateBishopMoves(board, index);
                    if (bishopMoves.size() > 0) {
                        moves.addAll(bishopMoves);
                    }
                    break;
                case Piece.ROOK:
                    List<Move> rookMoves = generateRookMoves(board, index);
                    if (rookMoves.size() > 0) {
                        moves.addAll(rookMoves);
                    }
                    break;
                case Piece.QUEEN:
                    List<Move> queenMoves = generateQueenMoves(board, index);
                    if (queenMoves.size() > 0) {
                        moves.addAll(queenMoves);
                    }
                    break;
                case Piece.KING:
                    List<Move> kingMoves = generateKingMoves(board, index);
                    kingMoves.addAll(checkCastling(board, index));
                    if (kingMoves.size() > 0) {
                        moves.addAll(kingMoves);
                    }
                    break;
                case Piece.KNIGHT:
                    List<Move> knightMoves = generateKnightMoves(board, index);
                    if (knightMoves.size() > 0) {
                        moves.addAll(knightMoves);
                    }
                    break;
            }
        }
        return moves;
    }

    public static boolean legalMovesExist(Board board) {
        int color = board.getTurn();
        List<Integer> piecePositions = board.getPiecePositions(color);

        for (int index : piecePositions) {
            if (index == -1) {
                continue;
            }

            switch (board.getPieceType(index)) {
                case Piece.PAWN:
                    List<Move> pawnMoves = generatePawnMoves(board, index);
                    if (pawnMoves.size() > 0) {
                        return true;
                    }
                    break;
                case Piece.BISHOP:
                    List<Move> bishopMoves = generateBishopMoves(board, index);
                    if (bishopMoves.size() > 0) {
                        return true;
                    }
                    break;
                case Piece.ROOK:
                    List<Move> rookMoves = generateRookMoves(board, index);
                    if (rookMoves.size() > 0) {
                        return true;
                    }
                    break;
                case Piece.QUEEN:
                    List<Move> queenMoves = generateQueenMoves(board, index);
                    if (queenMoves.size() > 0) {
                        return true;
                    }
                    break;
                case Piece.KING:
                    List<Move> kingMoves = generateKingMoves(board, index);
                    kingMoves.addAll(checkCastling(board, index));
                    if (kingMoves.size() > 0) {
                        return true;
                    }
                    break;
                case Piece.KNIGHT:
                    List<Move> knightMoves = generateKnightMoves(board, index);
                    if (knightMoves.size() > 0) {
                        return true;
                    }
                    break;
            }
        }

        return false;
    }

    private static List<Move> generatePawnMoves(Board board, int index) {
        List<Move> moves = new ArrayList<>();
        int color = board.getPieceColor(index);
        int[] dir = PrecomputedGameData.pieceDirections.get(Piece.create(Piece.PAWN, color));

        //if space in direction is free and move is legal, add move
        if (PrecomputedGameData.edgeOfBoard.get(dir[0])[index] > 0 && board.isEmpty(index + dir[0]) && board.isLegalMove(index, index + dir[0])) {

            //if a pawn can only move once before hitting the edge, he is going to promote
            if (PrecomputedGameData.edgeOfBoard.get(dir[0])[index] == 1) {
                moves.add(new Move(index, index + dir[0], 3, Piece.QUEEN));
                moves.add(new Move(index, index + dir[0], 3, Piece.ROOK));
                moves.add(new Move(index, index + dir[0], 3, Piece.BISHOP));
                moves.add(new Move(index, index + dir[0], 3, Piece.KNIGHT));
            } else {
                moves.add(new Move(index, index + dir[0], 0, 0));
            }
        }

        //if pawn can go forward 6 times it has not moved yet, check if moving 2 tiles is possible and if the tile in front of you is empty
        if (PrecomputedGameData.edgeOfBoard.get(dir[0])[index] == 6 && board.isEmpty(index + dir[0]) && board.isEmpty(index + (dir[0] * 2)) && board.isLegalMove(index, index + (dir[0] * 2))) {
            moves.add(new Move(index, index + (dir[0] * 2), 0, 0));
        }

        //look sideways for captures
        for (int side = 1; side < dir.length; side++) {
            if (PrecomputedGameData.edgeOfBoard.get(dir[side])[index] == 0) {
                continue;
            }

            //check for regular capture
            if (!board.isEmpty(index + dir[side]) && !board.isColor(index + dir[side], color) && board.isLegalMove(index, index + dir[side])) {
                //if a pawn can only move forward once before hitting the edge, he is going to promote
                if (PrecomputedGameData.edgeOfBoard.get(dir[0])[index] == 1) {
                    moves.add(new Move(index, index + dir[side], 3, Piece.QUEEN));
                    moves.add(new Move(index, index + dir[side], 3, Piece.ROOK));
                    moves.add(new Move(index, index + dir[side], 3, Piece.BISHOP));
                    moves.add(new Move(index, index + dir[side], 3, Piece.KNIGHT));
                } else {
                    moves.add(new Move(index, index + dir[side], 0, 0));
                }
            }

            //from this point on we check for en passant, don't waste time if it's not possible
            if (board.getEnPassant() == 0) {
                continue;
            }

            //if we get here, en passant is possible on the board, so check if this pawn can take it
            int enPassantCapt = board.getEnPassant() + dir[0];
            if (enPassantCapt == index + dir[side] && board.isLegalMove(index, index + dir[side])) {
                moves.add(new Move(index, index + dir[side], 2, 0));
            }
        }

        return moves;
    }

    private static List<Move> generateBishopMoves(Board board, int index) {
        return scanDirectionUntilCollision(board, index, PrecomputedGameData.pieceDirections.get(Piece.BISHOP));
    }

    private static List<Move> generateKnightMoves(Board board, int index) {
        return scanDirectionOnce(board, index, PrecomputedGameData.pieceDirections.get(Piece.KNIGHT));
    }

    private static List<Move> generateRookMoves(Board board, int index) {
        return scanDirectionUntilCollision(board, index, PrecomputedGameData.pieceDirections.get(Piece.ROOK));
    }

    private static List<Move> generateQueenMoves(Board board, int index) {
        List<Move> moves = new ArrayList<>();
        moves.addAll(generateRookMoves(board, index));
        moves.addAll(generateBishopMoves(board, index));
        return moves;
    }

    private static List<Move> generateKingMoves(Board board, int index) {
        return scanDirectionOnce(board, index, PrecomputedGameData.pieceDirections.get(Piece.KING));
    }

    private static List<Move> checkCastling(Board board, int index) {
        List<Move> castleMove = new ArrayList<>();
        int color = board.getPieceColor(index);

        if ((!board.canCastleKingSide(color) && !board.canCastleQueenSide(color)) || board.isCheck()) {
            return castleMove;
        }

        //if the tiles between king and rook are empty
        if (board.isEmpty(index + 1) && board.isEmpty(index + 2) && board.canCastleKingSide(color)) {
            //if there is a piece at the end, and it is your rook, which hasn't moved
            if (!board.isEmpty(index + 3) && board.isRook(index + 3) && board.isColor(index + 3, color)) {
                //if move is legal
                if (board.isLegalMove(index, index + 1) && board.isLegalMove(index, index + 2)) {
                    castleMove.add(new Move(index, index + 2, 1, 0));
                }
            }
        }

        //if the tiles between king and rook are empty
        if (board.isEmpty(index - 1) && board.isEmpty(index - 2) && board.isEmpty(index - 3) && board.canCastleQueenSide(color)) {
            //if there is a piece at the end, and it is your rook, which hasn't moved
            if (!board.isEmpty(index - 4) && board.isRook(index - 4) && board.isColor(index - 4, color)) {
                //if move is legal
                if (board.isLegalMove(index, index - 1) && board.isLegalMove(index, index - 2)) {
                    castleMove.add(new Move(index, index - 2, 1, 0));
                }
            }
        }

        return castleMove;
    }

    private static List<Move> scanDirectionOnce(Board board, int index, int[] directions) {
        List<Move> moves = new ArrayList<>(8);
        int color = board.getPieceColor(index);

        for (int direction : directions) {
            int count = PrecomputedGameData.edgeOfBoard.get(direction)[index];

            if (count > 0 && (board.isEmpty(index + direction) || !board.isColor(index + direction, color))) {
                if (board.isLegalMove(index, index + direction)) {
                    moves.add(new Move(index, index + direction, 0, 0));
                }
            }
        }

        return moves;
    }

    private static List<Move> scanDirectionUntilCollision(Board board, int index, int[] directions) {
        List<Move> moves = new ArrayList<>(8);
        int color = board.getPieceColor(index);

        for (int direction : directions) {
            int count = PrecomputedGameData.edgeOfBoard.get(direction)[index];
            int curTile = index;

            while (count > 0 && (board.isEmpty(curTile + direction) || !board.isColor(curTile + direction, color))) {
                curTile += direction;

                if (board.isLegalMove(index, curTile)) {
                    moves.add(new Move(index, curTile, 0, 0));
                }

                if (!board.isEmpty(curTile) && !board.isColor(curTile, color)) {
                    break;
                }
                count--;
            }
        }

        return moves;
    }
}
