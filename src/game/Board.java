package game;

import java.util.*;

import utils.MoveGenerator;
import utils.Utils;
import utils.Zobrist;

public class Board {
    private final int[] tile;
    private HashMap<Integer, List<Integer>> piecePositions;

    private int enPassant;
    private int castleRights;

    private int currentMove;
    private int lastCaptureOrPawnAdv;
    private int colorToMove;


    private HashMap<Long, Integer> positionHistory;
    private HashMap<Integer, Integer> remainingPieces;
    private boolean gameOver;
    private long hash;

    private final List<Integer> boardState;


    public Board() {
        positionHistory = new HashMap<>();
        remainingPieces = new HashMap<>();
        gameOver = false;
        currentMove = 2;
        lastCaptureOrPawnAdv = 1;
        enPassant = 0;
        tile = new int[64];
        colorToMove = Piece.WHITE;
        castleRights = 15;
        boardState = new ArrayList<>();

        reset();
        hashPosition();
    }

    public Board(String fenString) {
        positionHistory = new HashMap<>();
        remainingPieces = new HashMap<>();
        gameOver = false;
        currentMove = 0;
        lastCaptureOrPawnAdv = 0;
        enPassant = 0;
        tile = new int[64];
        colorToMove = Piece.WHITE;
        castleRights = 0;
        boardState = new ArrayList<>();

        makeBoardFromFen(fenString);
        hashPosition();
    }

    /**
     * Makes a deep copy of the board.
     *
     * @return a deep copy of the board.
     */
    public Board deepCopy() {
        Board boardCopy = new Board();
        System.arraycopy(this.tile, 0, boardCopy.tile, 0, 64);
        if (enPassant > 0) {
            boardCopy.setEnPassant(enPassant);
        }
        boardCopy.setColorToMove(colorToMove);
        boardCopy.setCurrentMove(currentMove);
        boardCopy.setLastCaptureOrPawnAdv(lastCaptureOrPawnAdv);
        boardCopy.castleRights = castleRights;

        boardCopy.setPositionHistory(new HashMap<>(positionHistory));
        boardCopy.setRemainingPieces(new HashMap<>(remainingPieces));
        boardCopy.setPiecePositions(new HashMap<>(piecePositions.size()));
        for (var entry : piecePositions.entrySet()) {
            boardCopy.piecePositions.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        boardCopy.setGameOver(gameOver);
        return boardCopy;
    }

    /**
     * Resets the board to its initial state.
     */
    public void reset() {
        piecePositions = new HashMap<>();
        piecePositions.put(Piece.BLACK, new ArrayList<>());
        piecePositions.put(Piece.WHITE, new ArrayList<>());

        piecePositions.get(Piece.BLACK).add(0);
        piecePositions.get(Piece.BLACK).add(1);
        piecePositions.get(Piece.BLACK).add(2);
        piecePositions.get(Piece.BLACK).add(3);
        piecePositions.get(Piece.BLACK).add(4);
        piecePositions.get(Piece.BLACK).add(5);
        piecePositions.get(Piece.BLACK).add(6);
        piecePositions.get(Piece.BLACK).add(7);

        tile[0] = Piece.createPiece(Piece.ROOK, Piece.BLACK);
        tile[1] = Piece.createPiece(Piece.KNIGHT, Piece.BLACK);
        tile[2] = Piece.createPiece(Piece.BISHOP, Piece.BLACK);
        tile[3] = Piece.createPiece(Piece.QUEEN, Piece.BLACK);
        tile[4] = Piece.createPiece(Piece.KING, Piece.BLACK);
        tile[5] = Piece.createPiece(Piece.BISHOP, Piece.BLACK);
        tile[6] = Piece.createPiece(Piece.KNIGHT, Piece.BLACK);
        tile[7] = Piece.createPiece(Piece.ROOK, Piece.BLACK);
        for (int index = 8; index < 16; index++) {
            piecePositions.get(Piece.BLACK).add(index);
            tile[index] = Piece.createPiece(Piece.PAWN, Piece.BLACK);
        }

        for (int index = 48; index < 56; index++) {
            piecePositions.get(Piece.WHITE).add(index);
            tile[index] = Piece.createPiece(Piece.PAWN, Piece.WHITE);
        }
        tile[56] = Piece.createPiece(Piece.ROOK, Piece.WHITE);
        tile[57] = Piece.createPiece(Piece.KNIGHT, Piece.WHITE);
        tile[58] = Piece.createPiece(Piece.BISHOP, Piece.WHITE);
        tile[59] = Piece.createPiece(Piece.QUEEN, Piece.WHITE);
        tile[60] = Piece.createPiece(Piece.KING, Piece.WHITE);
        tile[61] = Piece.createPiece(Piece.BISHOP, Piece.WHITE);
        tile[62] = Piece.createPiece(Piece.KNIGHT, Piece.WHITE);
        tile[63] = Piece.createPiece(Piece.ROOK, Piece.WHITE);

        piecePositions.get(Piece.WHITE).add(56);
        piecePositions.get(Piece.WHITE).add(57);
        piecePositions.get(Piece.WHITE).add(58);
        piecePositions.get(Piece.WHITE).add(59);
        piecePositions.get(Piece.WHITE).add(60);
        piecePositions.get(Piece.WHITE).add(61);
        piecePositions.get(Piece.WHITE).add(62);
        piecePositions.get(Piece.WHITE).add(63);

        remainingPieces.put(Piece.createPiece(Piece.ROOK, Piece.WHITE), 2);
        remainingPieces.put(Piece.createPiece(Piece.QUEEN, Piece.WHITE), 1);
        remainingPieces.put(Piece.createPiece(Piece.BISHOP, Piece.WHITE), 2);
        remainingPieces.put(Piece.createPiece(Piece.KNIGHT, Piece.WHITE), 2);
        remainingPieces.put(Piece.createPiece(Piece.PAWN, Piece.WHITE), 8);
        remainingPieces.put(Piece.createPiece(Piece.ROOK, Piece.BLACK), 2);
        remainingPieces.put(Piece.createPiece(Piece.QUEEN, Piece.BLACK), 1);
        remainingPieces.put(Piece.createPiece(Piece.BISHOP, Piece.BLACK), 2);
        remainingPieces.put(Piece.createPiece(Piece.KNIGHT, Piece.BLACK), 2);
        remainingPieces.put(Piece.createPiece(Piece.PAWN, Piece.BLACK), 8);
    }

    /**
     * Updates the gameOver variable according to the current game state
     */
    private void checkGameOver() {
        int color = (colorToMove == Piece.WHITE) ? Piece.BLACK : Piece.WHITE;
        List<List<Integer>> attackedSquares = MoveGenerator.getAttackedSquares(this, color);

        //if no legal moves exist, it's either checkmate or stalemate
        if (!MoveGenerator.legalMovesExist(this, attackedSquares)) {
            gameOver = true;
            return;
        }

        if (isRepetition() || fiftyMoveRule() || insufficientMaterial()) {
            gameOver = true;
        }
    }

    /**
     * Determines if the king is in check and has no legal moves.
     *
     * @return true if its checkmate, false otherwise.
     */
    private boolean isCheckMate(List<List<Integer>> attackedSquares) {
        int color = (colorToMove == Piece.WHITE) ? Piece.BLACK : Piece.WHITE;
        return isCheck(color, attackedSquares);
    }

    /**
     * Determines if there is a stalemate.
     *
     * @return true if it's stalemate, false otherwise.
     */
    private boolean isStalemate(List<List<Integer>> attackedSquares) {
        int color = (colorToMove == Piece.WHITE) ? Piece.BLACK : Piece.WHITE;
        return !isCheck(color, attackedSquares);
    }

    /**
     * Determines if the same position has been reached 3 times.
     *
     * @return true if it's the 3 repetition rule, false otherwise.
     */
    private boolean isRepetition() {
        return positionHistory.containsKey(hash) && positionHistory.get(hash) > 2;
    }

    /**
     * Checks if there has been no capture or pawn advancement in the last 50 moves.
     *
     * @return true if there are no captures or pawn advancements in the last 50 moves,
     * false otherwise.
     */
    private boolean fiftyMoveRule() {
        return currentMove - lastCaptureOrPawnAdv >= 100;
    }

    /**
     * Checks if there is sufficient material on the board for a checkmate.
     *
     * @return true if there is insufficient material, false otherwise.
     */
    private boolean insufficientMaterial() {
        boolean insufficientMaterialWhite = false;
        boolean insufficientMaterialBlack = false;

        //if there are pawns on the board there is sufficient material
        if (remainingPieces.containsKey(Piece.createPiece(Piece.PAWN, Piece.WHITE))
                || remainingPieces.containsKey(Piece.createPiece(Piece.PAWN, Piece.BLACK))) {
            return false;
        }

        //if white or black has rooks or queens there is sufficient material
        if (remainingPieces.containsKey(Piece.createPiece(Piece.QUEEN, Piece.WHITE))
                || remainingPieces.containsKey(Piece.createPiece(Piece.ROOK, Piece.WHITE)) ||
                remainingPieces.containsKey(Piece.createPiece(Piece.QUEEN, Piece.BLACK)) ||
                remainingPieces.containsKey(Piece.createPiece(Piece.ROOK, Piece.BLACK))) {
            return false;
        }

        //if white only has a king and one/two knight(s)
        if (remainingPieces.containsKey(Piece.createPiece(Piece.KNIGHT, Piece.WHITE)) && !remainingPieces.containsKey(Piece.createPiece(Piece.BISHOP, Piece.WHITE))) {
            insufficientMaterialWhite = true;
        }

        //if white only has a king and a bishop
        if (remainingPieces.containsKey(Piece.createPiece(Piece.BISHOP, Piece.WHITE)) &&
                !remainingPieces.containsKey(Piece.createPiece(Piece.KNIGHT, Piece.WHITE)) &&
                remainingPieces.get(Piece.createPiece(Piece.BISHOP, Piece.WHITE)) == 1) {
            insufficientMaterialWhite = true;
        }

        //if black only has a king and one/two knight(s)
        if (remainingPieces.containsKey(Piece.createPiece(Piece.KNIGHT, Piece.BLACK)) && !remainingPieces.containsKey(Piece.createPiece(Piece.BISHOP, Piece.BISHOP))) {
            insufficientMaterialBlack = true;
        }

        //if black only has a king and a bishop
        if (remainingPieces.containsKey(Piece.createPiece(Piece.BISHOP, Piece.BLACK)) &&
                !remainingPieces.containsKey(Piece.createPiece(Piece.KNIGHT, Piece.BLACK)) &&
                remainingPieces.get(Piece.createPiece(Piece.BISHOP, Piece.BLACK)) == 1) {
            insufficientMaterialBlack = true;
        }

        return insufficientMaterialWhite && insufficientMaterialBlack;
    }

    public long getHash() {
        return hash;
    }

    /**
     * Makes a move on the board.
     *
     * @param move Move you want to make
     */
    public void makeMove(Move move) {
        currentMove++;
        int flag = move.flag();
        int start = move.start();
        int end = move.end();

        //save current board state
        boardState.add(BoardState.encodeState(enPassant, castleRights, tile[end], lastCaptureOrPawnAdv));

        //update hash for moved/captured pieces
        hash ^= Zobrist.getKey(start, tile[start]);
        if (tile[end] != 0) {
            hash ^= Zobrist.getKey(end, tile[end]);
        }

        if (flag == Move.NONE) {
            //if it's a capture or a pawn move, reset lastCaptureOrPawnAdv
            if (tile[end] != 0 || isPawn(start)) {
                lastCaptureOrPawnAdv = currentMove;
                if (tile[end] != 0) {
                    removePieceFromRemainingPieces(tile[end]);
                    piecePositions.get(Piece.getColor(tile[end])).remove(Integer.valueOf(end));
                }
            }

            //make the move
            tile[end] = tile[start];
            tile[start] = 0;
            piecePositions.get(Piece.getColor(tile[end])).remove(Integer.valueOf(start));
            piecePositions.get(Piece.getColor(tile[end])).add(end);

            updateGameState(move.start(), move.end());

            //if pawn moved up 2 squares, make en passant possible for next move
            if (isPawn(end) && Math.abs(start - end) == 16) {
                enPassant = end;
                hash ^= Zobrist.ENPASSANT[enPassant];
            }
            return;
        }

        if (flag == Move.CASTLE) {
            //move the king
            tile[end] = tile[start];
            tile[start] = 0;
            piecePositions.get(Piece.getColor(tile[end])).remove(Integer.valueOf(start));
            piecePositions.get(Piece.getColor(tile[end])).add(end);

            //move the rook
            if (start - end < 0) {
                hash ^= Zobrist.getKey(start + 3, tile[start + 3]);
                tile[start + 1] = tile[start + 3];
                tile[start + 3] = 0;
                piecePositions.get(Piece.getColor(tile[start + 1])).remove(Integer.valueOf(start + 3));
                piecePositions.get(Piece.getColor(tile[start + 1])).add(start + 1);
                hash ^= Zobrist.getKey(start + 1, tile[start + 1]);

            } else {
                hash ^= Zobrist.getKey(start - 4, tile[start - 4]);
                tile[start - 1] = tile[start - 4];
                tile[start - 4] = 0;
                piecePositions.get(Piece.getColor(tile[start - 1])).remove(Integer.valueOf(start - 4));
                piecePositions.get(Piece.getColor(tile[start - 1])).add(start - 1);
                hash ^= Zobrist.getKey(start - 1, tile[start - 1]);
            }

            updateGameState(move.start(), move.end());
            return;
        }

        if (flag == Move.ENPASSANT) {
            //capture enemy pawn
            hash ^= Zobrist.getKey(enPassant, tile[enPassant]);
            removePieceFromRemainingPieces(tile[enPassant]);
            piecePositions.get(Piece.getColor(tile[enPassant])).remove(Integer.valueOf(enPassant));
            tile[enPassant] = 0;
            lastCaptureOrPawnAdv = currentMove;

            //move the pawn
            tile[end] = tile[start];
            tile[start] = 0;
            piecePositions.get(Piece.getColor(tile[end])).remove(Integer.valueOf(start));
            piecePositions.get(Piece.getColor(tile[end])).add(end);

            lastCaptureOrPawnAdv = currentMove;
            updateGameState(move.start(), move.end());
            return;
        }

        if (flag == Move.PROMOTION) {
            //remove end piece if capture
            if (tile[end] != 0) {
                removePieceFromRemainingPieces(tile[end]);
                piecePositions.get(Piece.getColor(tile[end])).remove(Integer.valueOf(end));
            }

            //create promoted piece
            tile[end] = Piece.createPiece(move.getPromotion(), Piece.getColor(tile[start]));
            addToRemainingPiece(tile[end]);
            piecePositions.get(Piece.getColor(tile[end])).add(end);

            //remove old pawn
            removePieceFromRemainingPieces(tile[start]);
            piecePositions.get(Piece.getColor(tile[end])).remove(Integer.valueOf(start));
            tile[start] = 0;

            lastCaptureOrPawnAdv = currentMove;
            updateGameState(move.start(), move.end());
        }
    }

    /**
     * Updates variables that keeps track of game state, namely colorToMove, positionHistory, currentMove,
     * enPassant.
     */
    private void updateGameState(int start, int end) {
        hash ^= Zobrist.CASTLEKEYS[castleRights];
        hash ^= Zobrist.getKey(end, tile[end]);

        //handle castling rights
        if (getPieceColor(end) == Piece.WHITE && (castleRights & 0b0011) > 0) {
            if (isKing(end)) {
                castleRights &= 0b1100;
            }

            if (isRook(end) && start == 63) {
                castleRights &= 0b1110;
            }

            if (isRook(end) && start == 56) {
                castleRights &= 0b1101;
            }
        }
        if (getPieceColor(end) == Piece.BLACK && (castleRights & 0b1100) > 0) {
            if (isKing(end)) {
                castleRights &= 0b0011;
            }

            if (isRook(end) && start == 7) {
                castleRights &= 0b1011;
            }

            if (isRook(end) && start == 0) {
                castleRights &= 0b0111;
            }
        }
        hash ^= Zobrist.CASTLEKEYS[castleRights];

        //if en passant was possible this move, make sure it's not possible next move
        if (enPassant > 0) {
            hash ^= Zobrist.ENPASSANT[enPassant];
            enPassant = 0;
        }

        hash ^= Zobrist.getColorKey(colorToMove);
        colorToMove = (colorToMove == Piece.WHITE) ? Piece.BLACK : Piece.WHITE;
        hash ^= Zobrist.getColorKey(colorToMove);
        addToPositionHistory(hash);
        checkGameOver();
    }

    /**
     * Unmakes a move on the board by moving the pieces back and restoring past game state variables.
     *
     * @param move move to be unmade.
     */
    public void unmakeMove(Move move) {
        int flag = move.flag();
        int start = move.start();
        int end = move.end();

        //get board state to be restored and delete it from the list
        int state = boardState.get(boardState.size() - 1);
        boardState.remove(boardState.size() - 1);
        removeFromPositionHistory(hash);

        hash ^= Zobrist.getKey(end, tile[end]);
        if (BoardState.getTile(state) != 0) {
            hash ^= Zobrist.getKey(end, BoardState.getTile(state));
        }

        if (flag == Move.NONE) {
            //put piece back where it was
            tile[start] = tile[end];
            tile[end] = BoardState.getTile(state);

            //ensure piecePositions is correct
            piecePositions.get(Piece.getColor(tile[start])).remove(Integer.valueOf(end));
            piecePositions.get(Piece.getColor(tile[start])).add(start);
            if (tile[end] != 0) {
                piecePositions.get(Piece.getColor(tile[end])).add(end);
                addToRemainingPiece(tile[end]);
            }

            revertGameStats(start, end, state);
            return;
        }

        if (flag == Move.CASTLE) {
            //put king back
            tile[start] = tile[end];
            tile[end] = 0;
            piecePositions.get(Piece.getColor(tile[start])).remove(Integer.valueOf(end));
            piecePositions.get(Piece.getColor(tile[start])).add(start);

            //put rook back
            if (start - end < 0) {
                hash ^= Zobrist.getKey(start + 1, tile[start + 1]);
                tile[start + 3] = tile[start + 1];
                tile[start + 1] = 0;
                piecePositions.get(Piece.getColor(tile[start + 3])).remove(Integer.valueOf(start + 1));
                piecePositions.get(Piece.getColor(tile[start + 3])).add(start + 3);
                hash ^= Zobrist.getKey(start + 3, tile[start + 3]);
            } else {
                hash ^= Zobrist.getKey(start - 1, tile[start - 1]);
                tile[start - 4] = tile[start - 1];
                tile[start - 1] = 0;
                piecePositions.get(Piece.getColor(tile[start - 4])).remove(Integer.valueOf(start - 1));
                piecePositions.get(Piece.getColor(tile[start - 4])).add(start - 4);
                hash ^= Zobrist.getKey(start - 4, tile[start - 4]);
            }

            revertGameStats(start, end, state);
            return;
        }

        if (flag == Move.ENPASSANT) {
            //put enemy pawn back
            int pastEnPassant = BoardState.getEnPassant(state);
            tile[pastEnPassant] = (colorToMove == Piece.WHITE) ? Piece.createPiece(Piece.PAWN, Piece.WHITE)
                    : Piece.createPiece(Piece.PAWN, Piece.BLACK);
            addToRemainingPiece(tile[pastEnPassant]);
            piecePositions.get(Piece.getColor(tile[pastEnPassant])).add(pastEnPassant);

            tile[start] = tile[end];
            tile[end] = 0;
            piecePositions.get(Piece.getColor(tile[start])).remove(Integer.valueOf(end));
            piecePositions.get(Piece.getColor(tile[start])).add(start);
            hash ^= Zobrist.getKey(pastEnPassant, tile[pastEnPassant]);

            revertGameStats(start, end, state);
            return;
        }

        if (flag == Move.PROMOTION) {
            int color = Piece.getColor(tile[end]);

            //remove promoted piece
            removePieceFromRemainingPieces(tile[end]);
            piecePositions.get(Piece.getColor(tile[end])).remove(Integer.valueOf(end));
            tile[end] = BoardState.getTile(state);

            //if a piece was captured before promoting, bring it back
            if (tile[end] != 0) {
                addToRemainingPiece(tile[end]);
                piecePositions.get(Piece.getColor(tile[end])).add(end);
            }

            //put pawn back
            tile[start] = Piece.createPiece(Piece.PAWN, color);
            addToRemainingPiece(tile[start]);
            piecePositions.get(color).add(start);

            revertGameStats(start, end, state);
        }
    }

    /**
     * Reverts variables that keep track of the game state to the last move state
     */
    private void revertGameStats(int start, int end, int state) {
        hash ^= Zobrist.CASTLEKEYS[castleRights];
        hash ^= Zobrist.getColorKey(colorToMove);

        currentMove--;
        lastCaptureOrPawnAdv = BoardState.getLastCaptOrPawnAdv(state);

        enPassant = BoardState.getEnPassant(state);
        castleRights = BoardState.getCastleRights(state);
        colorToMove = (colorToMove == Piece.WHITE) ? Piece.BLACK : Piece.WHITE;

        //update hash
        hash ^= Zobrist.getKey(start, tile[start]);
        hash ^= Zobrist.CASTLEKEYS[castleRights];
        hash ^= Zobrist.getColorKey(colorToMove);
        if (enPassant != 0) {
            hash ^= Zobrist.ENPASSANT[enPassant];
        }
        gameOver = false;
    }

    /**
     * Removes piece from list that the board uses to keep track of remaining pieces.
     *
     * @param piece piece you want to remove.
     */
    private void removePieceFromRemainingPieces(int piece) {
        //remove piece if it's the last one
        if (remainingPieces.get(piece) == 1) {
            remainingPieces.remove(piece);
        } else {
            int nr = remainingPieces.get(piece);
            nr--;
            remainingPieces.replace(piece, nr);
        }
    }

    /**
     * Removes hash from hashmap used to keep track of past positions.
     *
     * @param hash hash you want to remove.
     */
    private void removeFromPositionHistory(long hash) {
        //remove piece if it's the last one
        if (positionHistory.get(hash) == 1) {
            positionHistory.remove(hash);
        } else {
            int nr = positionHistory.get(hash);
            nr--;
            positionHistory.replace(hash, nr);
        }
    }

    /**
     * Adds piece to list that board uses to keep track of remaining pieces.
     *
     * @param piece piece you want to add.
     */
    private void addToRemainingPiece(int piece) {
        try {
            int nr = remainingPieces.get(piece);
            nr++;
            remainingPieces.replace(piece, nr);
        } catch (NullPointerException e) {
            remainingPieces.put(piece, 1);
        }
    }

    /**
     * Adds Zobrist hash representing the current position to the hashmap that keeps track of past positions.
     *
     * @param hash Zobrist hash representing current position.
     */
    private void addToPositionHistory(long hash) {
        try {
            int nr = positionHistory.get(hash);
            nr++;
            positionHistory.replace(hash, nr);
        } catch (NullPointerException e) {
            positionHistory.put(hash, 1);
        }
    }

    /**
     * Determines if the king of the given color is in check.
     *
     * @param color color of the king.
     * @return true if the king is in check, false otherwise.
     */
    public boolean isCheck(int color, List<List<Integer>> attackedSquares) {
        int kingIndex = getKingIndex(color);

        for (List<Integer> lineOfSight : attackedSquares) {
            if (lineOfSight.contains(kingIndex)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Goes through the board to get the coordinates of the king of the
     * given color.
     *
     * @param color color of the king you want the coordinates of.
     * @return an array containing the x and y-axis coordinates of the king.
     */
    public int getKingIndex(int color) {
        int king = Piece.createPiece(Piece.KING, color);

        //white king is more likely to be at the end of the board
        if (isWhite(color)) {
            for (int index = 63; index >= 0; index--) {
                if (tile[index] == king) {
                    return index;
                }
            }
        } else {
            //black king is more likely to be at the start of the board
            for (int index = 0; index < 64; index++) {
                if (tile[index] == king) {
                    return index;
                }
            }
        }
        return -1;
    }

    /**
     * Makes the current position into a unique, or at least close to unique, hash.
     */
    public void hashPosition() {
        hash = 0;

        for (int index = 0; index < 64; index++) {
            if (tile[index] == 0) {
                continue;
            }

            hash ^= Zobrist.getKey(index, tile[index]);
        }


        hash ^= Zobrist.getColorKey(colorToMove);
        hash ^= Zobrist.CASTLEKEYS[castleRights];
        if (enPassant != 0) {
            hash ^= Zobrist.ENPASSANT[enPassant];
        }
    }

    /**
     * Translates the current position to a fen string.
     *
     * @return a fen string of the current position.
     */
    public String positionToFen() {
        StringBuilder fen = new StringBuilder();
        String castleRightsWhite = "";
        String castleRightsBlack = "";
        String enPassant = "-";
        int emptyRowCont = 0;

        for (int index = 0; index < 64; index++) {
            //count empty tiles
            if (tile[index] == 0) {
                emptyRowCont++;
                if ((index + 1) % 8 == 0 && index / 8 != 7) {
                    fen.append(emptyRowCont).append("/");
                    emptyRowCont = 0;
                }

                continue;
            }

            //if we get here we found a piece, so add how many empty tiles there were
            if (emptyRowCont > 0) {
                fen.append(emptyRowCont);
                emptyRowCont = 0;
            }

            //represent piece as white or black and add to fen
            fen.append(Piece.makeString(tile[index]));

            //add enPassant
            if (isPawn(index) && this.enPassant == index && index > 0) {
                if (getPieceColor(index) == Piece.WHITE) {
                    enPassant = Utils.getChessCoordinates(index + 8);
                } else {
                    enPassant = Utils.getChessCoordinates(index - 8);
                }
            }

            //add the slash at the end of row
            if ((index + 1) % 8 == 0 && index / 8 != 7) {
                fen.append("/");
            }
        }

        //constructing castleRights
        if (canCastleKingSide(Piece.WHITE)) {
            castleRightsWhite += "K";
        }
        if (canCastleQueenSide(Piece.WHITE)) {
            castleRightsWhite += "Q";
        }
        if (canCastleKingSide(Piece.BLACK)) {
            castleRightsBlack += "k";
        }
        if (canCastleQueenSide(Piece.BLACK)) {
            castleRightsBlack += "q";
        }

        String castleRights = (castleRightsWhite.equals("") && castleRightsBlack.equals("")) ?
                "-" : castleRightsWhite + castleRightsBlack;
        String colorToMoveString = (colorToMove == Piece.WHITE) ? "w" : "b";
        fen.append(" ").append(colorToMoveString).append(" ").append(castleRights).append(" ").append(enPassant)
                .append(" ").append(currentMove - lastCaptureOrPawnAdv).append(" ").append(currentMove / 2);
        return fen.toString();
    }

    /**
     * Constructs the board from the given fen string.
     *
     * @param fenString fen string of the position.
     */
    private void makeBoardFromFen(String fenString) {
        piecePositions = new HashMap<>();
        piecePositions.put(Piece.WHITE, new ArrayList<>());
        piecePositions.put(Piece.BLACK, new ArrayList<>());
        String[] fenSplit = fenString.split(" ");
        fenString = fenSplit[0];

        colorToMove = (fenSplit[1].equals("w")) ? Piece.WHITE : Piece.BLACK;
        char[] castleRights = fenSplit[2].toCharArray();

        if (isWhite(colorToMove)) {
            if (fenSplit[5].equals("1")) {
                currentMove = 2;
            } else {
                currentMove = (Integer.parseInt(fenSplit[5]) * 2);
            }
        } else {
            currentMove = (Integer.parseInt(fenSplit[5]) * 2) + 1;
        }

        lastCaptureOrPawnAdv = currentMove - Integer.parseInt(fenSplit[4]);
        int color;
        int index = 0;
        //place pieces on the board
        for (int i = 0; i < fenString.length(); i++) {
            if (Character.isDigit(fenString.charAt(i))) {
                index += Character.getNumericValue(fenString.charAt(i));
                continue;
            }
            if (fenString.charAt(i) == '/') {
                continue;
            }
            if (Character.isUpperCase(fenString.charAt(i))) {
                color = Piece.WHITE;
            } else {
                color = Piece.BLACK;
            }

            String typeString = Character.toString(Character.toLowerCase(fenString.charAt(i)));
            int type = 0;
            switch (typeString) {
                case "k":
                    type = Piece.KING;
                    break;
                case "q":
                    type = Piece.QUEEN;
                    break;
                case "n":
                    type = Piece.KNIGHT;
                    break;
                case "b":
                    type = Piece.BISHOP;
                    break;
                case "r":
                    type = Piece.ROOK;
                    break;
                case "p":
                    type = Piece.PAWN;
                    break;
            }
            tile[index] = Piece.createPiece(type, color);
            piecePositions.get(color).add(index);
            addToRemainingPiece(tile[index]);

            index++;
        }

        //set up castling rights
        for (char castle : castleRights) {
            if (castle == 'K') {
                this.castleRights += 1;
                continue;
            }
            if (castle == 'Q') {
                this.castleRights += 2;
                continue;
            }
            if (castle == 'k') {
                this.castleRights += 4;
                continue;
            }
            if (castle == 'q') {
                this.castleRights += 8;
            }
        }

        //make en passant possible if necessary
        if (!fenSplit[3].equals("-")) {
            int enPassantCoordinates = Utils.getIndexFromChessCoordinates(fenSplit[3]);
            //if it's on y = 2 that means we are dealing with black
            if (colorToMove == Piece.WHITE) {
                enPassant = enPassantCoordinates + 8;
            } else {
                enPassant = enPassantCoordinates - 8;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder board = new StringBuilder();
        board.append(" +---+---+---+---+---+---+---+---+\n");

        for (int rank = 0; rank < 8; rank++) {
            board.append(Math.abs(rank - 8)).append("| ");
            for (int file = 0; file < 8; file++) {
                int index = rank * 8 + file;
                int piece = tile[index];
                if (piece == 0) {
                    board.append("  | ");
                } else {
                    board.append(Piece.makeString(piece)).append(" | ");
                }
            }
            board.append(Math.abs(rank - 8)).append("\n");
            if (rank > 0) {
                board.append(" |---+---+---+---+---+---+---+---|\n");
            } else {
                board.append(" +---+---+---+---+---+---+---+---+\n");
            }
        }

        board.append("   a   b   c   d   e   f   g   h\n");
        return board.toString();
    }

    private boolean isWhite(int color) {
        return color == Piece.WHITE;
    }

    public boolean isKing(int index) {
        return Piece.getType(tile[index]) == Piece.KING;
    }

    public boolean isQueen(int index) {
        return Piece.getType(tile[index]) == Piece.QUEEN;
    }

    public boolean isBishop(int index) {
        return Piece.getType(tile[index]) == Piece.BISHOP;
    }

    public boolean isRook(int index) {
        return Piece.getType(tile[index]) == Piece.ROOK;
    }

    public boolean isKnight(int index) {
        return Piece.getType(tile[index]) == Piece.KNIGHT;
    }

    public boolean isPawn(int index) {
        return Piece.getType(tile[index]) == Piece.PAWN;
    }

    public boolean isEmptyTile(int index) {
        return tile[index] == 0;
    }

    public boolean isColor(int index, int color) {
        return Piece.getColor(tile[index]) == color;
    }

    public int getPieceType(int index) {
        return Piece.getType(tile[index]);
    }

    public int getPieceColor(int index) {
        return Piece.getColor(tile[index]);
    }

    public int getColorToMove() {
        return colorToMove;
    }

    public void setEnPassant(int enPassant) {
        this.enPassant = enPassant;
    }

    public int getEnPassant() {
        return enPassant;
    }

    public void setColorToMove(int colorToMove) {
        this.colorToMove = colorToMove;
    }

    public void setCurrentMove(int currentMove) {
        this.currentMove = currentMove;
    }

    public void setLastCaptureOrPawnAdv(int lastCaptureOrPawnAdv) {
        this.lastCaptureOrPawnAdv = lastCaptureOrPawnAdv;
    }

    public void setPositionHistory(HashMap<Long, Integer> positionHistory) {
        this.positionHistory = positionHistory;
    }

    public void setRemainingPieces(HashMap<Integer, Integer> remainingPieces) {
        this.remainingPieces = remainingPieces;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public int getCurrentMove() {
        return currentMove;
    }

    public boolean getGameOver() {
        return gameOver;
    }

    public List<Integer> getPiecePositions(int color) {
        return piecePositions.get(color);
    }

    public void setPiecePositions(HashMap<Integer, List<Integer>> piecePositions) {
        this.piecePositions = piecePositions;
    }

    public boolean canCastleQueenSide(int color) {
        if (color == Piece.WHITE) {
            return (castleRights & 0b0010) == 0b0010;
        }

        return (castleRights & 0b1000) == 0b1000;
    }

    public boolean canCastleKingSide(int color) {
        if (color == Piece.WHITE) {
            return (castleRights & 0b0001) == 0b0001;
        }
        return (castleRights & 0b0100) == 0b0100;
    }
}