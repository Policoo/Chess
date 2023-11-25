package utils;

import game.Board;
import game.Move;
import game.Piece;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MoveGenerator {
    private static HashMap<Integer, int[]> edgeOfBoard;
    private static HashMap<Integer, int[]> pieceDirections;

    /**
     * Calculates and stores the distance to the edge of the board for each direction
     * for later use. Also initializes all directions that the pieces can go in.
     */
    public static void initialize() {
        edgeOfBoard = new HashMap<>();
        edgeOfBoard.put(-17, new int[64]);
        edgeOfBoard.put(-15, new int[64]);
        edgeOfBoard.put(-10, new int[64]);
        edgeOfBoard.put(-9, new int[64]);
        edgeOfBoard.put(-8, new int[64]);
        edgeOfBoard.put(-7, new int[64]);
        edgeOfBoard.put(-6, new int[64]);
        edgeOfBoard.put(-1, new int[64]);
        edgeOfBoard.put(1, new int[64]);
        edgeOfBoard.put(6, new int[64]);
        edgeOfBoard.put(7, new int[64]);
        edgeOfBoard.put(8, new int[64]);
        edgeOfBoard.put(9, new int[64]);
        edgeOfBoard.put(10, new int[64]);
        edgeOfBoard.put(15, new int[64]);
        edgeOfBoard.put(17, new int[64]);

        for (int index = 0; index < 64; index++) {

            //up and left
            int i = index;
            int count = 0;
            while (i - 9 >= 0 && (i / 8) - 1 == (i - 9) / 8) {
                i -= 9;
                count++;
            }
            edgeOfBoard.get(-9)[index] = count;

            //up
            i = index;
            count = 0;
            while (i - 8 >= 0) {
                i -= 8;
                count++;
            }
            edgeOfBoard.get(-8)[index] = count;

            //up and right
            i = index;
            count = 0;
            while (i - 7 >= 0 && i / 8 != (i - 7) / 8) {
                i -= 7;
                count++;
            }
            edgeOfBoard.get(-7)[index] = count;

            //left
            i = index;
            count = 0;
            while (i - 1 >= 0 && i / 8 == (i - 1) / 8) {
                i -= 1;
                count++;
            }
            edgeOfBoard.get(-1)[index] = count;

            //right
            i = index;
            count = 0;
            while (i + 1 < 64 && i / 8 == (i + 1) / 8) {
                i += 1;
                count++;
            }
            edgeOfBoard.get(1)[index] = count;

            //down and left
            i = index;
            count = 0;
            while (i + 7 < 64 && i / 8 != (i + 7) / 8) {
                i += 7;
                count++;
            }
            edgeOfBoard.get(7)[index] = count;

            //down
            i = index;
            count = 0;
            while (i + 8 < 64) {
                i += 8;
                count++;
            }
            edgeOfBoard.get(8)[index] = count;

            //down and right
            i = index;
            count = 0;
            while (i + 9 < 64 && (i / 8) + 1 == (i + 9) / 8) {
                i += 9;
                count++;
            }
            edgeOfBoard.get(9)[index] = count;

            //for knight
            if (index - 17 >= 0 && index % 8 != 0) {
                edgeOfBoard.get(-17)[index] = 1;
            }

            if (index - 15 > 0 && index % 8 != 7) {
                edgeOfBoard.get(-15)[index] = 1;
            }

            if (index - 10 >= 0 && index % 8 > 1) {
                edgeOfBoard.get(-10)[index] = 1;
            }

            if (index - 6 > 0 && index % 8 < 6) {
                edgeOfBoard.get(-6)[index] = 1;
            }

            if (index + 17 < 64 && index % 8 != 7) {
                edgeOfBoard.get(17)[index] = 1;
            }

            if (index + 15 < 64 && index % 8 != 0) {
                edgeOfBoard.get(15)[index] = 1;
            }

            if (index + 10 < 64 && index % 8 < 6) {
                edgeOfBoard.get(10)[index] = 1;
            }

            if (index + 6 < 64 && index % 8 > 1) {
                edgeOfBoard.get(6)[index] = 1;
            }
        }

        pieceDirections = new HashMap<>();
        pieceDirections.put(Piece.KING, new int[]{-9, -8, -7, -1, 1, 7, 8, 9});
        pieceDirections.put(Piece.QUEEN, new int[]{-9, -8, -7, -1, 1, 7, 8, 9});
        pieceDirections.put(Piece.BISHOP, new int[]{-9, -7, 7, 9});
        pieceDirections.put(Piece.ROOK, new int[]{-8, -1, 1, 8});
        pieceDirections.put(Piece.KNIGHT, new int[]{-17, -15, -10, -6, 6, 10, 15, 17});
        pieceDirections.put(Piece.PAWN, new int[]{-9, -7});
    }

    public static List<Move> generateMoves(Board board) {
        if (board.getGameOver()) {
            return new ArrayList<>();
        }

        List<Move> moves = new ArrayList<>();
        int color = board.getColorToMove();
        int otherColor = (color == Piece.WHITE) ? Piece.BLACK : Piece.WHITE;
        List<Integer> piecePositions = board.getPiecePositions(color);
        List<List<Integer>> pinLines = getPins(board, otherColor);
        List<List<Integer>> attackedSquares = getAttackedSquares(board, otherColor);
        int check = getCheckLineIndex(board, attackedSquares);

        for (int index : piecePositions) {
            switch (board.getPieceType(index)) {
                case Piece.PAWN:
                    List<Move> pawnMoves = generatePawnMoves(index, board, pinLines, attackedSquares, check);
                    if (pawnMoves.size() > 0) {
                        moves.addAll(pawnMoves);
                    }
                    break;
                case Piece.BISHOP:
                    List<Move> bishopMoves = generateBishopMoves(index, board, pinLines, attackedSquares, check);
                    if (bishopMoves.size() > 0) {
                        moves.addAll(bishopMoves);
                    }
                    break;
                case Piece.ROOK:
                    List<Move> rookMoves = generateRookMoves(index, board, pinLines, attackedSquares, check);
                    if (rookMoves.size() > 0) {
                        moves.addAll(rookMoves);
                    }
                    break;
                case Piece.QUEEN:
                    List<Move> queenMoves = generateQueenMoves(index, board, pinLines, attackedSquares, check);
                    if (queenMoves.size() > 0) {
                        moves.addAll(queenMoves);
                    }
                    break;
                case Piece.KING:
                    List<Move> kingMoves = generateKingMoves(index, board, pinLines, attackedSquares, check);
                    kingMoves.addAll(checkCastling(index, board, pinLines, attackedSquares, check));
                    if (kingMoves.size() > 0) {
                        moves.addAll(kingMoves);
                    }
                    break;
                case Piece.KNIGHT:
                    List<Move> knightMoves = generateKnightMoves(index, board, pinLines, attackedSquares, check);
                    if (knightMoves.size() > 0) {
                        moves.addAll(knightMoves);
                    }
                    break;
            }
        }
        return moves;
    }

    public static boolean legalMovesExist(Board board, List<List<Integer>> attackedSquares) {
        int color = board.getColorToMove();
        int otherColor = (color == Piece.WHITE) ? Piece.BLACK : Piece.WHITE;
        List<Integer> piecePositions = board.getPiecePositions(color);
        List<List<Integer>> pinLines = getPins(board, otherColor);
        int check = getCheckLineIndex(board, attackedSquares);

        for (int index : piecePositions) {
            switch (board.getPieceType(index)) {
                case Piece.PAWN:
                    List<Move> pawnMoves = generatePawnMoves(index, board, pinLines, attackedSquares, check);
                    if (pawnMoves.size() > 0) {
                        return true;
                    }
                    break;
                case Piece.BISHOP:
                    List<Move> bishopMoves = generateBishopMoves(index, board, pinLines, attackedSquares, check);
                    if (bishopMoves.size() > 0) {
                        return true;
                    }
                    break;
                case Piece.ROOK:
                    List<Move> rookMoves = generateRookMoves(index, board, pinLines, attackedSquares, check);
                    if (rookMoves.size() > 0) {
                        return true;
                    }
                    break;
                case Piece.QUEEN:
                    List<Move> queenMoves = generateQueenMoves(index, board, pinLines, attackedSquares, check);
                    if (queenMoves.size() > 0) {
                        return true;
                    }
                    break;
                case Piece.KING:
                    List<Move> kingMoves = generateKingMoves(index, board, pinLines, attackedSquares, check);
                    kingMoves.addAll(checkCastling(index, board, pinLines, attackedSquares, check));
                    if (kingMoves.size() > 0) {
                        return true;
                    }
                    break;
                case Piece.KNIGHT:
                    List<Move> knightMoves = generateKnightMoves(index, board, pinLines, attackedSquares, check);
                    if (knightMoves.size() > 0) {
                        return true;
                    }
                    break;
            }
        }

        return false;
    }

    private static List<Move> generatePawnMoves(int index, Board board, List<List<Integer>> pinLines, List<List<Integer>> attackedSquares, int check) {
        List<Move> moves = new ArrayList<>();
        int color = board.getPieceColor(index);
        //set if pawn moves up or down the board
        int direction = (color == Piece.WHITE) ? -8 : 8;

        //if space in direction is free and move is legal, add move
        if (edgeOfBoard.get(direction)[index] > 0 && board.isEmptyTile(index + direction) && isLegalMove(board, index, index + direction, pinLines, attackedSquares, check)) {

            //if a pawn can only move once before hitting the edge, he is going to promote
            if (edgeOfBoard.get(direction)[index] == 1) {
                moves.add(new Move(index, index + direction, 3, Piece.QUEEN));
                moves.add(new Move(index, index + direction, 3, Piece.ROOK));
                moves.add(new Move(index, index + direction, 3, Piece.BISHOP));
                moves.add(new Move(index, index + direction, 3, Piece.KNIGHT));
            } else {
                moves.add(new Move(index, index + direction, 0, 0));
            }
        }

        //if pawn can go forward 6 times it has not moved yet, check if moving 2 tiles is possible and if the tile in front of you is empty
        if (edgeOfBoard.get(direction)[index] == 6 && board.isEmptyTile(index + direction) && board.isEmptyTile(index + (direction * 2)) && isLegalMove(board, index, index + (direction * 2), pinLines, attackedSquares, check)) {
            moves.add(new Move(index, index + (direction * 2), 0, 0));
        }

        //check for available captures. We multiply by this so that we look in the correct diagonals
        int[] leftAndRight = {9 * (direction / 8), 7 * (direction / 8)};
        for (int side : leftAndRight) {
            if (edgeOfBoard.get(side)[index] == 0) {
                continue;
            }

            //check for regular capture
            if (!board.isEmptyTile(index + side) && !board.isColor(index + side, color) && isLegalMove(board, index, index + side, pinLines, attackedSquares, check)) {
                //if a pawn can only move once before hitting the edge, he is going to promote
                if (edgeOfBoard.get(direction)[index] == 1) {
                    moves.add(new Move(index, index + side, 3, Piece.QUEEN));
                    moves.add(new Move(index, index + side, 3, Piece.ROOK));
                    moves.add(new Move(index, index + side, 3, Piece.BISHOP));
                    moves.add(new Move(index, index + side, 3, Piece.KNIGHT));
                } else {
                    moves.add(new Move(index, index + side, 0, 0));
                }
            }

            //check for en passant, make sure that pawn doesn't go off the board if he takes in this direction
            int sideways = (side == -9 || side == 7) ? -1 : (side == -7 || side == 9) ? 1 : side;
            if (edgeOfBoard.get(sideways)[index] == 0) {
                continue;
            }

            if (!board.isEmptyTile(index + sideways) && board.getEnPassant() == index + sideways && !board.isColor(index + sideways, color) && isLegalMove(board, index, index + side, pinLines, attackedSquares, check)) {
                moves.add(new Move(index, index + side, 2, 0));
            }
        }

        return moves;
    }

    private static List<Move> generateBishopMoves(int index, Board board, List<List<Integer>> pinLines, List<List<Integer>> attackedSquares, int check) {
        return scanDirectionUntilCollision(index, pieceDirections.get(Piece.BISHOP), board, pinLines, attackedSquares, check);
    }

    private static List<Move> generateKnightMoves(int index, Board board, List<List<Integer>> pinLines, List<List<Integer>> attackedSquares, int check) {
        return scanDirectionOnce(index, pieceDirections.get(Piece.KNIGHT), board, pinLines, attackedSquares, check);
    }

    private static List<Move> generateRookMoves(int index, Board board, List<List<Integer>> pinLines, List<List<Integer>> attackedSquares, int check) {
        return scanDirectionUntilCollision(index, pieceDirections.get(Piece.ROOK), board, pinLines, attackedSquares, check);
    }

    private static List<Move> generateQueenMoves(int index, Board board, List<List<Integer>> pinLines, List<List<Integer>> attackedSquares, int check) {
        List<Move> moves = new ArrayList<>();
        moves.addAll(generateRookMoves(index, board, pinLines, attackedSquares, check));
        moves.addAll(generateBishopMoves(index, board, pinLines, attackedSquares, check));
        return moves;
    }

    private static List<Move> generateKingMoves(int index, Board board, List<List<Integer>> pinLines, List<List<Integer>> attackedSquares, int check) {
        return scanDirectionOnce(index, pieceDirections.get(Piece.KING), board, pinLines, attackedSquares, check);
    }

    private static List<Move> checkCastling(int index, Board board, List<List<Integer>> pinLines, List<List<Integer>> attackedSquares, int check) {
        List<Move> castleMove = new ArrayList<>();
        int color = board.getPieceColor(index);

        if ((!board.canCastleKingSide(color) && !board.canCastleQueenSide(color)) || board.isCheck(color, attackedSquares)) {
            return castleMove;
        }

        //if the tiles between king and rook are empty
        if (board.isEmptyTile(index + 1) && board.isEmptyTile(index + 2) && board.canCastleKingSide(color)) {
            //if there is a piece at the end, and it is your rook, which hasn't moved
            if (!board.isEmptyTile(index + 3) && board.isRook(index + 3) && board.isColor(index + 3, color)) {
                //if move is legal
                if (isLegalMove(board, index, index + 1, pinLines, attackedSquares, check) && isLegalMove(board, index, index + 2, pinLines, attackedSquares, check)) {
                    castleMove.add(new Move(index, index + 2, 1, 0));
                }
            }
        }

        //if the tiles between king and rook are empty
        if (board.isEmptyTile(index - 1) && board.isEmptyTile(index - 2) && board.isEmptyTile(index - 3) && board.canCastleQueenSide(color)) {
            //if there is a piece at the end, and it is your rook, which hasn't moved
            if (!board.isEmptyTile(index - 4) && board.isRook(index - 4) && board.isColor(index - 4, color)) {
                //if move is legal
                if (isLegalMove(board, index, index - 1, pinLines, attackedSquares, check) && isLegalMove(board, index, index - 2, pinLines, attackedSquares, check)) {
                    castleMove.add(new Move(index, index - 2, 1, 0));
                }
            }
        }

        return castleMove;
    }

    public static String getCastleRights(int index, Board board) {
        String castleRights = "";
        if (board.isKing(index)) {
            if (board.canCastleKingSide(board.getPieceColor(index))) {
                castleRights += "k";
            }
            if (board.canCastleQueenSide(board.getPieceColor(index))) {
                castleRights += "q";
            }
            return castleRights;
        }
        return "";
    }

    /**
     * Goes through the board to find pieces of the given color and calculates what
     * squares those pieces attack.
     *
     * @param board board the game is on.
     * @param color color of the pieces you want to calculate the attacking squares for
     * @return a list of list, each containing a line of sight in a direction for a piece.
     */
    public static List<List<Integer>> getAttackedSquares(Board board, int color) {
        List<List<Integer>> attackedSquares = new ArrayList<>();
        List<Integer> piecePositions = board.getPiecePositions(color);

        for (int index : piecePositions) {
            int pieceType = board.getPieceType(index);

            //generate directions that this piece can go in
            int[] directions = pieceDirections.get(pieceType);

            //if this is a pawn that needs to go down, multiply its directions by -1
            int scalar = (board.isPawn(index) && board.isColor(index, Piece.BLACK)) ? -1 : 1;

            for (int direction : directions) {
                //for the pawn again, this will leave every other case unchanged
                direction = direction * scalar;

                List<Integer> lineOfSight = new ArrayList<>();
                int curIndex = index;
                int count = edgeOfBoard.get(direction)[index];

                //add the piece itself to the line of sight, but indent it by 64, so we know later that this is the piece
                lineOfSight.add(index + 64);

                for (int step = 0; step < count; step++) {
                    curIndex = curIndex + direction;

                    //add empty tiles to the line of sight, if we see a piece, stop
                    if (board.isEmptyTile(curIndex)) {
                        lineOfSight.add(curIndex);

                        //pawns and kings can only go in a direction once
                        if (board.isPawn(index) || board.isKing(index)) {
                            break;
                        }
                    } else {
                        //if it is our color, or a king (if it gets to the king condition it means it's not our color)
                        if (board.isColor(curIndex, color) || board.isKing(curIndex)) {
                            lineOfSight.add(curIndex);
                        }
                        break;
                    }
                }

                //if piece is attacking at least a square, add it
                if (lineOfSight.size() > 1) {
                    attackedSquares.add(lineOfSight);
                }
            }

        }

        return attackedSquares;
    }

    /**
     * Goes through the board to find bishops, rooks or the queen of the given
     * color. Once it finds one it checks if the piece is pinning something to
     * the king. If so, it adds the line of sight of that piece to a list which
     * it later returns.
     *
     * @param board board the game is on.
     * @param color color of the pieces that you want to check are pinning the
     *              given color king.
     * @return a list of lists containing coordinates of tiles that are in the line
     * of sight of a pin.
     */
    private static List<List<Integer>> getPins(Board board, int color) {
        List<List<Integer>> pinLines = new ArrayList<>();
        List<Integer> piecePositions = board.getPiecePositions(color);

        for (int index : piecePositions) {
            //kings, knights and pawns can't pin
            if (board.isKing(index) || board.isKnight(index) || board.isPawn(index)) {
                continue;
            }

            //generate directions that this piece can go in
            int[] directions = pieceDirections.get(board.getPieceType(index));

            //add the pin line, if one is found
            List<Integer> pinLine = getPinLine(board, index, directions);
            if (pinLine.size() > 0) {
                pinLines.add(pinLine);
            }
        }

        return pinLines;
    }

    /**
     * Goes in each direction that the piece can go in and checks for pins.
     *
     * @param index      index of tile the piece is on.
     * @param directions directions that the piece can go in.
     * @return a list of coordinates in the pin
     */
    private static List<Integer> getPinLine(Board board, int index, int[] directions) {
        List<Integer> pinLine = new ArrayList<>();
        int color = board.getPieceColor(index);

        for (int direction : directions) {
            int curIndex = index;
            boolean canPin = false;
            int count = edgeOfBoard.get(direction)[index];

            //we add the square that the piece is on, so that captures can break the pin
            pinLine.add(index);

            for (int step = 0; step < count; step++) {
                curIndex = curIndex + direction;

                //if tile is empty, add it to the list
                if (board.isEmptyTile(curIndex)) {
                    pinLine.add(curIndex);
                    continue;
                }

                //if you see your own piece, that means it's not a pin in that direction
                if (board.isColor(curIndex, color)) {
                    pinLine.clear();
                    break;
                }

                //if we find a king of the opposite color, and it's a pin, so return the pin line
                if (board.isKing(curIndex)) {
                    if (canPin) {
                        return pinLine;
                    } else {
                        pinLine.clear();
                        break;
                    }
                }

                /*if we get here, the tile contains a piece that is of the other color. If canPin is true, this is
                the second opponents piece we encountered, so a pin is not possible*/
                if (canPin) {
                    pinLine.clear();
                    break;
                }

                //if we get here that means we found an opponents piece that is not a king, so a pin is possible
                pinLine.add(curIndex);
                canPin = true;
            }

            pinLine.clear();
        }

        return pinLine;
    }

    /**
     * Checks if a potential move found by the MoveGenerator is legal by seeing if a pinned piece moved or
     * if the king is in check.
     *
     * @param board           board the game is on.
     * @param start           start position of move that you want to check if legal.
     * @param end             end position of move that you want to check if legal.
     * @param pinLines        List of lists containing all pins on the king.
     * @param attackedSquares List of lists containing line of sights of enemy pieces.
     * @param check           int that is -1 if the king is not in check, (indexCheck1 + 1) * 100 + indexCheck2
     *                        if it is in double check or an index of attackedSquares if it is a normal check
     * @return true if move is legal, false otherwise.
     */
    private static boolean isLegalMove(Board board, int start, int end, List<List<Integer>> pinLines, List<List<Integer>> attackedSquares, int check) {
        //if we are moving the king, make sure he is not in a square attacked by the opponent
        if (board.isKing(start)) {
            for (List<Integer> lineOfSight : attackedSquares) {
                //if the king is moving to a square that is in the line of sight, it's in check so illegal
                if (lineOfSight.contains(end)) {
                    return false;
                }
            }

            //the king was not in check and moved to a square that is not attacked by an enemy piece
            if (check == -1) {
                return true;
            }

            /*line of sight stops at king, so the square behind him, that is technically still under attack
              by the enemy piece, is not in attacked squares. Make sure he doesn't move there*/
            if (check < attackedSquares.size()) {
                //get index the piece is on
                int index = attackedSquares.get(check).get(0) - 64;

                //pawns only check for the square in front of them
                if (board.isPawn(index)) {
                    return true;
                }

                int direction = attackedSquares.get(check).get(1) - index;
                return start + direction != end;
            }

            //same thing but for double checks
            int[] checks = {check % 100, (check / 100) - 1};

            for (int line : checks) {
                //get index the piece is on
                int index = attackedSquares.get(line).get(0) - 64;

                //pawns only check for the square in front of them
                if (board.isPawn(index)) {
                    continue;
                }

                int direction = attackedSquares.get(line).get(1) - index;
                if (start + direction == end) {
                    return false;
                }
            }

            return true;
        }

        //if we get here that means we are not moving the king, and it's a double check, so move is not legal
        if (check > attackedSquares.size()) {
            return false;
        }

        //if we get here, the king is not in check, so make sure the piece isn't pinned
        int pinned = -1;
        for (int index = 0; index < pinLines.size(); index++) {
            //if this piece is not pinned, continue
            if (!pinLines.get(index).contains(start)) {
                continue;
            }

            //if the piece is moving out of the pin, it would result in a check, save pinLine index to make sure is legal
            pinned = index;
            break;
        }

        //if we are in check, make sure to either block it or capture the piece checking the king
        if (check != -1) {
            //if we are either capturing the piece checking us or moving in front of the check, it's legal (except if the piece is pinned)
            return (attackedSquares.get(check).contains(end + 64) || attackedSquares.get(check).contains(end)) && pinned == -1;
        }

        //make sure the piece is still in the pin line
        if (pinned != -1) {
            return pinLines.get(pinned).contains(end);
        }

        //if we get here, the piece isn't pinned, and it's not a check, so it's free to move
        return true;
    }

    /**
     * Goes through the attacked squares to find if the king is in check.
     *
     * @param board           board that the game is being played on.
     * @param attackedSquares List of lists, each containing the squares in a direction that a piece is attacking.
     * @return 0 if there is no check, the index of the list in attackedSquares containing the line of sight
     * that has the king in check, or (indexCheck1 + 1) * 100 + indexCheck2 if it's a double check and the king has to move.
     */
    private static int getCheckLineIndex(Board board, List<List<Integer>> attackedSquares) {
        int kingIndex = board.getKingIndex(board.getColorToMove());
        int check = -1;
        for (int index = 0; index < attackedSquares.size(); index++) {
            //if the king is not in this line of sight, continue, because he can't be in check
            if (!attackedSquares.get(index).contains(kingIndex)) {
                continue;
            }

            /*if we already found a check, then this is a double check. The only way to get out of a
              double check is by moving the king, if you are here, you are not moving the king, it's not legal*/
            if (check != -1) {
                return (check + 1) * 100 + index;
            }

            //if we get here we found a check
            check = index;
        }

        return check;
    }

    private static List<Move> scanDirectionOnce(int index, int[] directions, Board board, List<List<Integer>> pinLines, List<List<Integer>> attackedSquares, int check) {
        List<Move> moves = new ArrayList<>();
        int color = board.getPieceColor(index);

        for (int direction : directions) {
            int count = edgeOfBoard.get(direction)[index];

            if (count > 0 && (board.isEmptyTile(index + direction) || !board.isColor(index + direction, color))) {
                if (isLegalMove(board, index, index + direction, pinLines, attackedSquares, check)) {
                    moves.add(new Move(index, index + direction, 0, 0));
                }
            }
        }

        return moves;
    }

    private static List<Move> scanDirectionUntilCollision(int index, int[] directions, Board board, List<List<Integer>> pinLines, List<List<Integer>> attackedSquares, int check) {
        List<Move> moves = new ArrayList<>();
        int color = board.getPieceColor(index);

        for (int direction : directions) {
            int count = edgeOfBoard.get(direction)[index];
            int curTile = index;

            while (count > 0 && (board.isEmptyTile(curTile + direction) || !board.isColor(curTile + direction, color))) {
                curTile += direction;

                if (isLegalMove(board, index, curTile, pinLines, attackedSquares, check)) {
                    moves.add(new Move(index, curTile, 0, 0));
                }

                if (!board.isEmptyTile(curTile) && !board.isColor(curTile, color)) {
                    break;
                }
                count--;
            }
        }

        return moves;
    }
}
