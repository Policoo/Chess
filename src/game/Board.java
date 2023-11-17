package game;

import java.util.*;

import utils.MoveGenerator;
import utils.Utils;

public class Board {
    private final int[] tile;
    private int enPassant;
    private int colorToMove;
    private int currentMove;
    private int lastCaptureOrPawnAdv;
    private HashMap<String, Integer> positionHistory;
    private HashMap<Integer, Integer> remainingPieces;
    private HashMap<Integer, List<Integer>> piecePositions;
    private boolean gameOver;
    private boolean castleQueenSideW;
    private boolean castleKingSideW;
    private boolean castleQueenSideB;
    private boolean castleKingSideB;

    public Board() {
        positionHistory = new HashMap<>();
        remainingPieces = new HashMap<>();
        gameOver = false;
        currentMove = 0;
        lastCaptureOrPawnAdv = 0;
        enPassant = -1;
        tile = new int[64];
        colorToMove = Piece.WHITE;
        reset();
        castleKingSideW = true;
        castleQueenSideW = true;
        castleKingSideB = true;
        castleQueenSideB = true;
    }

    public Board(String fenString) {
        positionHistory = new HashMap<>();
        remainingPieces = new HashMap<>();
        gameOver = false;
        currentMove = 0;
        lastCaptureOrPawnAdv = 0;
        enPassant = -1;
        tile = new int[64];
        colorToMove = Piece.WHITE;
        castleKingSideW = false;
        castleQueenSideW = false;
        castleKingSideB = false;
        castleQueenSideB = false;
        makeBoardFromFen(fenString);
    }

    /**
     * Makes a deep copy of the board.
     *
     * @return a deep copy of the board.
     */
    public Board deepCopy() {
        Board boardCopy = new Board();
        System.arraycopy(this.tile, 0, boardCopy.tile, 0, 64);
        if (enPassant >= 0) {
            boardCopy.setEnPassant(enPassant);
        }
        boardCopy.setColorToMove(colorToMove);
        boardCopy.setCurrentMove(currentMove);
        boardCopy.setLastCaptureOrPawnAdv(lastCaptureOrPawnAdv);
        boardCopy.castleKingSideW = castleKingSideW;
        boardCopy.castleKingSideB = castleKingSideB;
        boardCopy.castleQueenSideW = castleQueenSideW;
        boardCopy.castleQueenSideB = castleQueenSideB;

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
        List<Move> moves = MoveGenerator.generateMoves(this);
        int color = (colorToMove == Piece.WHITE) ? Piece.BLACK : Piece.WHITE;
        List<List<Integer>> attackedSquares = MoveGenerator.getAttackedSquares(this, color);

        if (isCheckMate(attackedSquares, moves) || isDraw(attackedSquares, moves)) {
            gameOver = true;
        }
    }

    /**
     * Determines if the king is in check and has no legal moves.
     *
     * @return true if its checkmate, false otherwise.
     */
    private boolean isCheckMate(List<List<Integer>> attackedSquares, List<Move> moves) {
        if (moves.size() > 0) {
            return false;
        }

        int color = (colorToMove == Piece.WHITE) ? Piece.BLACK : Piece.WHITE;
        return isCheck(color, attackedSquares);
    }

    /**
     * Determines if the game is a draw, based on stalemate, repetition rule, fifty
     * move rule or insufficient material rule.
     *
     * @return true if it's a draw, false otherwise.
     */
    private boolean isDraw(List<List<Integer>> attackedSquares, List<Move> moves) {
        return isStalemate(attackedSquares, moves) || isRepetition() || fiftyMoveRule() || insufficientMaterial();
    }

    /**
     * Determines if there is a stalemate.
     *
     * @return true if it's stalemate, false otherwise.
     */
    private boolean isStalemate(List<List<Integer>> attackedSquares, List<Move> moves) {
        if (moves.size() > 0) {
            return false;
        }

        int color = (colorToMove == Piece.WHITE) ? Piece.BLACK : Piece.WHITE;
        return !isCheck(color, attackedSquares);
    }

    /**
     * Determines if the same position has been reached 3 times.
     *
     * @return true if it's the 3 repetition rule, false otherwise.
     */
    private boolean isRepetition() {
        //only add pieces position and color to move to history
        String[] splitFen = positionToFen().split(" ");
        String fenHistory = splitFen[0] + " " + splitFen[1];
        return positionHistory.containsKey(fenHistory) && positionHistory.get(fenHistory) > 1;
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

    /**
     * Makes a move on the board.
     *
     * @param move Move you want to make
     */
    public void makeMove(Move move) {
        int flag = move.flag();
        int start = move.start();
        int end = move.end();

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

            updateGameState(move.end());

            //if pawn moved up 2 squares, make en passant possible for next move
            if (isPawn(end) && Math.abs(start - end) == 16) {
                enPassant = end;
            }

            return;
        }

        if (flag == Move.CASTLE) {
            //move the rook
            if (start - end < 0) {
                tile[start + 1] = tile[start + 3];
                tile[start + 3] = 0;
                piecePositions.get(Piece.getColor(tile[start + 1])).remove(Integer.valueOf(start + 3));
                piecePositions.get(Piece.getColor(tile[start + 1])).add(start + 1);
            } else {
                tile[start - 1] = tile[start - 4];
                tile[start - 4] = 0;
                piecePositions.get(Piece.getColor(tile[start - 1])).remove(Integer.valueOf(start - 4));
                piecePositions.get(Piece.getColor(tile[start - 1])).add(start - 1);
            }

            //move the king
            tile[end] = tile[start];
            tile[start] = 0;
            piecePositions.get(Piece.getColor(tile[end])).remove(Integer.valueOf(start));
            piecePositions.get(Piece.getColor(tile[end])).add(end);

            updateGameState(move.end());
            return;
        }

        if (flag == Move.ENPASSANT) {
            //capture enemy pawn
            removePieceFromRemainingPieces(tile[enPassant]);
            piecePositions.get(Piece.getColor(tile[enPassant])).remove(Integer.valueOf(enPassant));
            tile[enPassant] = 0;
            lastCaptureOrPawnAdv = currentMove;

            //move the pawn
            tile[end] = tile[start];
            tile[start] = 0;
            piecePositions.get(Piece.getColor(tile[end])).remove(Integer.valueOf(start));
            piecePositions.get(Piece.getColor(tile[end])).add(end);

            updateGameState(move.end());
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

            updateGameState(move.end());
        }
    }

    /**
     * Updates variables that keeps track of game state, namely colorToMove, positionHistory, currentMove,
     * enPassant.
     */
    private void updateGameState(int end) {
        //handle castling rights
        if (getPieceColor(end) == Piece.WHITE) {
            if ((castleKingSideW || castleQueenSideW) && isKing(end)) {
                castleKingSideW = false;
                castleQueenSideW = false;
            }

            if (castleKingSideW) {
                if (isRook(end) && end == 63) {
                    castleKingSideW = false;
                }
            }

            if (castleQueenSideW) {
                if (isRook(end) && end == 56) {
                    castleQueenSideW = false;
                }
            }
        } else {
            if ((castleKingSideB || castleQueenSideB) && isKing(end)) {
                castleKingSideB = false;
                castleQueenSideB = false;
            }

            if (castleKingSideB) {
                if (isRook(end) && end == 7) {
                    castleKingSideB = false;
                }
            }

            if (castleQueenSideB) {
                if (isRook(end) && end == 0) {
                    castleQueenSideB = false;
                }
            }
        }

        //if en passant was possible this move, make sure it's not possible next move
        if (enPassant >= 0) {
            enPassant = -1;
        }

        //only add pieces position and color to move to history
        String[] splitFen = positionToFen().split(" ");
        String fenHistory = splitFen[0] + " " + splitFen[1];
        if (positionHistory.containsKey(fenHistory)) {
            int nr = positionHistory.get(fenHistory);
            nr++;
            positionHistory.put(fenHistory, nr);
        } else {
            positionHistory.put(fenHistory, 0);
        }

        currentMove++;
        checkGameOver();
        colorToMove = (colorToMove == Piece.WHITE) ? Piece.BLACK : Piece.WHITE;
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
     * Translates the current position to a fen string.
     *
     * @return a fen string of the current position.
     */
    public String positionToFen() {
        StringBuilder fen = new StringBuilder();
        StringBuilder castleRightsWhite = new StringBuilder();
        StringBuilder castleRightsBlack = new StringBuilder();
        String castleRights;
        String enPassant = "-";
        int emptyRowCont = 0;
        for (int index = 0; index < 64; index++) {
            //count empty tiles
            if (tile[index] == 0) {
                emptyRowCont++;
                if ((index + 1) % 8 == 0) {
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

            //add castling rights
            if (isKing(index)) {
                if (getPieceColor(index) == Piece.WHITE) {
                    castleRightsWhite.append(MoveGenerator.getCastleRights(index, this).toUpperCase());
                } else {
                    castleRightsBlack.append(MoveGenerator.getCastleRights(index, this));
                }
            }

            //add enPassant
            if (isPawn(index) && this.enPassant == index) {
                if (getPieceColor(index) == Piece.WHITE) {
                    enPassant = Utils.getChessCoordinates(index + 8);
                } else {
                    enPassant = Utils.getChessCoordinates(index - 8);
                }
            }

            //add the slash at the end of row
            if ((index + 1) % 8 == 0) {
                fen.append("/");
            }
        }

        //constructing castleRights
        if (castleRightsWhite.length() == 0 && castleRightsBlack.length() == 0) {
            castleRights = "-";
        } else {
            castleRights = castleRightsWhite + castleRightsBlack.toString();
        }

        String colorToMoveString = (colorToMove == Piece.WHITE) ? "w" : "b";
        fen.append(" ").append(colorToMoveString).append(" ").append(castleRights).append(" ").append(enPassant).
                append(" ").append(currentMove - lastCaptureOrPawnAdv).append(" ").append(currentMove / 2);
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
                currentMove = (Integer.parseInt(fenSplit[5]) * 2) - 1;
            }
        } else {
            currentMove = (Integer.parseInt(fenSplit[5]) * 2);
        }

        setLastCaptureOrPawnAdv(currentMove - Integer.parseInt(fenSplit[4]));
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
                castleKingSideW = true;
                continue;
            }
            if (castle == 'Q') {

                castleQueenSideW = true;
                continue;
            }
            if (castle == 'k') {
                castleKingSideB = true;
                continue;
            }
            if (castle == 'q') {
                castleQueenSideB = true;
            }
        }

        //make en passant possible if necessary
        if (!fenSplit[3].equals("-")) {
            int enPassantCoordinates = Utils.getIndexFromChessCoordinates(fenSplit[3]);
            //if it's on y = 2 that means we are dealing with black
            if (enPassantCoordinates / 8 == 1) {
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
            board.append(rank + 1).append("| ");
            for (int file = 0; file < 8; file++) {
                int index = rank * 8 + file;
                int piece = tile[index];
                if (piece == 0) {
                    board.append("  | ");
                } else {
                    board.append(Piece.makeString(piece)).append(" | ");
                }
            }
            board.append(rank + 1).append("\n");
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

    public void setPositionHistory(HashMap<String, Integer> positionHistory) {
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
            return castleQueenSideW;
        }
        return castleQueenSideB;
    }

    public boolean canCastleKingSide(int color) {
        if (color == Piece.WHITE) {
            return castleKingSideW;
        }
        return castleKingSideB;
    }
}