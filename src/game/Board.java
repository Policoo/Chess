package game;

import utils.MoveGenerator;
import utils.Utils;
import utils.Zobrist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Board {
    private final int[] tile;
    private HashMap<Integer, List<Integer>> piecePositions;

    private int enPassant;
    private int castleRights;

    private int currentMove;
    private int lastCaptOrPawnAdv;
    private int turn;


    private HashMap<Long, Integer> positionHistory;
    private HashMap<Integer, Integer> remainingPieces;
    private boolean gameOver;
    private long hash;

    private final List<Integer> boardState;

    // <--> INITIALIZATION <--> //

    public Board() {
        positionHistory = new HashMap<>();
        remainingPieces = new HashMap<>();
        gameOver = false;
        currentMove = 2;
        lastCaptOrPawnAdv = 1;
        enPassant = 0;
        tile = new int[64];
        turn = Piece.WHITE;
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
        lastCaptOrPawnAdv = 0;
        enPassant = 0;
        tile = new int[64];
        turn = Piece.WHITE;
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
        boardCopy.setTurn(turn);
        boardCopy.setCurrentMove(currentMove);
        boardCopy.setLastCaptOrPawnAdv(lastCaptOrPawnAdv);
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

        tile[0] = Piece.create(Piece.ROOK, Piece.BLACK);
        tile[1] = Piece.create(Piece.KNIGHT, Piece.BLACK);
        tile[2] = Piece.create(Piece.BISHOP, Piece.BLACK);
        tile[3] = Piece.create(Piece.QUEEN, Piece.BLACK);
        tile[4] = Piece.create(Piece.KING, Piece.BLACK);
        tile[5] = Piece.create(Piece.BISHOP, Piece.BLACK);
        tile[6] = Piece.create(Piece.KNIGHT, Piece.BLACK);
        tile[7] = Piece.create(Piece.ROOK, Piece.BLACK);
        for (int index = 8; index < 16; index++) {
            piecePositions.get(Piece.BLACK).add(index);
            tile[index] = Piece.create(Piece.PAWN, Piece.BLACK);
        }

        for (int index = 48; index < 56; index++) {
            piecePositions.get(Piece.WHITE).add(index);
            tile[index] = Piece.create(Piece.PAWN, Piece.WHITE);
        }
        tile[56] = Piece.create(Piece.ROOK, Piece.WHITE);
        tile[57] = Piece.create(Piece.KNIGHT, Piece.WHITE);
        tile[58] = Piece.create(Piece.BISHOP, Piece.WHITE);
        tile[59] = Piece.create(Piece.QUEEN, Piece.WHITE);
        tile[60] = Piece.create(Piece.KING, Piece.WHITE);
        tile[61] = Piece.create(Piece.BISHOP, Piece.WHITE);
        tile[62] = Piece.create(Piece.KNIGHT, Piece.WHITE);
        tile[63] = Piece.create(Piece.ROOK, Piece.WHITE);

        piecePositions.get(Piece.WHITE).add(56);
        piecePositions.get(Piece.WHITE).add(57);
        piecePositions.get(Piece.WHITE).add(58);
        piecePositions.get(Piece.WHITE).add(59);
        piecePositions.get(Piece.WHITE).add(60);
        piecePositions.get(Piece.WHITE).add(61);
        piecePositions.get(Piece.WHITE).add(62);
        piecePositions.get(Piece.WHITE).add(63);

        remainingPieces.put(Piece.create(Piece.ROOK, Piece.WHITE), 2);
        remainingPieces.put(Piece.create(Piece.QUEEN, Piece.WHITE), 1);
        remainingPieces.put(Piece.create(Piece.BISHOP, Piece.WHITE), 2);
        remainingPieces.put(Piece.create(Piece.KNIGHT, Piece.WHITE), 2);
        remainingPieces.put(Piece.create(Piece.PAWN, Piece.WHITE), 8);
        remainingPieces.put(Piece.create(Piece.ROOK, Piece.BLACK), 2);
        remainingPieces.put(Piece.create(Piece.QUEEN, Piece.BLACK), 1);
        remainingPieces.put(Piece.create(Piece.BISHOP, Piece.BLACK), 2);
        remainingPieces.put(Piece.create(Piece.KNIGHT, Piece.BLACK), 2);
        remainingPieces.put(Piece.create(Piece.PAWN, Piece.BLACK), 8);
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

        turn = (fenSplit[1].equals("w")) ? Piece.WHITE : Piece.BLACK;
        char[] castleRights = fenSplit[2].toCharArray();

        if (isWhite(turn)) {
            if (fenSplit[5].equals("1")) {
                currentMove = 2;
            } else {
                currentMove = (Integer.parseInt(fenSplit[5]) * 2);
            }
        } else {
            currentMove = (Integer.parseInt(fenSplit[5]) * 2) + 1;
        }

        lastCaptOrPawnAdv = currentMove - Integer.parseInt(fenSplit[4]);
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
            tile[index] = Piece.create(type, color);
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
            if (turn == Piece.WHITE) {
                enPassant = enPassantCoordinates + 8;
            } else {
                enPassant = enPassantCoordinates - 8;
            }
        }
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

        hash ^= Zobrist.getColorKey(turn);
        hash ^= Zobrist.CASTLE_KEYS[castleRights];
        if (enPassant != 0) {
            hash ^= Zobrist.EN_PASSANT[enPassant];
        }
    }

    // <--> INITIALIZATION <--> //

    // <--> MAKING MOVES <--> //

    /**
     * Makes a move on the board.
     *
     * @param move Move you want to make
     */
    public void makeMove(Move move) {
        int flag = move.flag();
        int start = move.start();
        int end = move.end();

        //save current board state
        boardState.add(BoardState.encode(enPassant, castleRights, tile[end], lastCaptOrPawnAdv));

        //remove start piece and end piece (if there is one) from the hash
        hash ^= Zobrist.getKey(start, tile[start]);
        if (tile[end] != 0) {
            hash ^= Zobrist.getKey(end, tile[end]);

            //handle a capture (since we are checking tile[end] != 0 anyway)
            removePieceFromRemainingPieces(tile[end]);
            piecePositions.get(Piece.color(tile[end])).remove(Integer.valueOf(end));
            lastCaptOrPawnAdv = currentMove;
        }

        //move the piece
        tile[end] = tile[start];
        tile[start] = 0;
        piecePositions.get(Piece.color(tile[end])).remove(Integer.valueOf(start));
        piecePositions.get(Piece.color(tile[end])).add(end);

        if (flag == Move.NONE) {
            updateGameState(start, end);

            if (isPawn(end)) {
                lastCaptOrPawnAdv = currentMove;
            }

            //if pawn moved up 2 squares, make en passant possible for next move
            if (isPawn(end) && Math.abs(start - end) == 16) {
                enPassant = end;
                hash ^= Zobrist.EN_PASSANT[enPassant];
            }
            return;
        }

        if (flag == Move.CASTLE) {
            //figure out if we are moving left or right
            int rookStart, rookEnd;
            if (start - end < 0) {
                rookStart = start + 3;
                rookEnd = start + 1;
            } else {
                rookStart = start - 4;
                rookEnd = start - 1;
            }

            //move the rook, while handling the hash and piecePositions
            hash ^= Zobrist.getKey(rookStart, tile[rookStart]);
            tile[rookEnd] = tile[rookStart];
            tile[rookStart] = 0;
            piecePositions.get(Piece.color(tile[rookEnd])).remove(Integer.valueOf(rookStart));
            piecePositions.get(Piece.color(tile[rookEnd])).add(rookEnd);
            hash ^= Zobrist.getKey(rookEnd, tile[rookEnd]);

            updateGameState(start, end);
            return;
        }

        if (flag == Move.ENPASSANT) {
            //capture enemy pawn
            hash ^= Zobrist.getKey(enPassant, tile[enPassant]);
            removePieceFromRemainingPieces(tile[enPassant]);
            piecePositions.get(Piece.color(tile[enPassant])).remove(Integer.valueOf(enPassant));
            tile[enPassant] = 0;

            lastCaptOrPawnAdv = currentMove;
            updateGameState(start, end);
            return;
        }

        if (flag == Move.PROMOTION) {
            //remove old pawn
            removePieceFromRemainingPieces(tile[end]);
            piecePositions.get(Piece.color(tile[end])).remove(Integer.valueOf(start));

            //create promoted piece
            tile[end] = Piece.create(move.getPromotion(), Piece.color(tile[end]));
            addToRemainingPiece(tile[end]);
            piecePositions.get(Piece.color(tile[end])).add(end);

            lastCaptOrPawnAdv = currentMove;
            updateGameState(start, end);
        }
    }

    /**
     * Updates variables that keeps track of game state, namely colorToMove, positionHistory, currentMove,
     * enPassant.
     */
    private void updateGameState(int start, int end) {
        //update the hash with the moved piece
        hash ^= Zobrist.getKey(end, tile[end]);

        //undo castle rights from hash
        hash ^= Zobrist.CASTLE_KEYS[castleRights];

        //update castling rights if needed
        int castleRightsColorMask = (Piece.color(tile[end]) == Piece.WHITE) ? 3 : 12;
        if ((castleRights & castleRightsColorMask) > 0) {
            if (isKing(end)) {
                castleRights &= (castleRightsColorMask ^ 0b1111);
            }

            //if the rooks are not on their start squares, that means they moved or were captured
            if (!isRook(63) || Piece.color(tile[63]) == Piece.BLACK) {
                castleRights &= 0b1110;
            }

            if (!isRook(56) || Piece.color(tile[56]) == Piece.BLACK) {
                castleRights &= 0b1101;
            }

            if (!isRook(7) || Piece.color(tile[7]) == Piece.WHITE) {
                castleRights &= 0b1011;
            }

            if (!isRook(0) || Piece.color(tile[0]) == Piece.WHITE) {
                castleRights &= 0b0111;
            }
        }

        //put new castleRights into hash
        hash ^= Zobrist.CASTLE_KEYS[castleRights];

        //if en passant was possible this move, make sure it's not possible next move
        if (enPassant > 0) {
            hash ^= Zobrist.EN_PASSANT[enPassant];
            enPassant = 0;
        }

        //update turn, make sure hash is accurate
        hash ^= Zobrist.getColorKey(turn);
        turn = (turn == Piece.WHITE) ? Piece.BLACK : Piece.WHITE;
        hash ^= Zobrist.getColorKey(turn);

        addToPositionHistory(hash);
        currentMove++;
        checkGameOver();
    }

    /**
     * Unmakes a move on the board by moving the pieces back and restoring past game state variables.
     *
     * @param move move to be unmade.
     */
    public void undoMove(Move move) {
        int flag = move.flag();
        int start = move.start();
        int end = move.end();

        //get board state to be restored and delete it from the list
        int state = boardState.get(boardState.size() - 1);
        boardState.remove(boardState.size() - 1);
        removeFromPositionHistory(hash);

        //undo the moved piece's hash
        hash ^= Zobrist.getKey(end, tile[end]);

        //move the piece back
        tile[start] = tile[end];
        tile[end] = BoardState.targetTile(state);
        piecePositions.get(Piece.color(tile[start])).remove(Integer.valueOf(end));
        piecePositions.get(Piece.color(tile[start])).add(start);

        //handle a capture
        int targetTile = BoardState.targetTile(state);
        if (targetTile != 0) {
            tile[end] = targetTile;
            addToRemainingPiece(tile[end]);
            piecePositions.get(Piece.color(tile[end])).add(end);

            //update the hash with the captured piece
            hash ^= Zobrist.getKey(end, tile[end]);
        }

        if (flag == Move.NONE) {
            revertGameStats(start, end, state);
            return;
        }

        if (flag == Move.CASTLE) {
            //figure out if we are moving left or right
            int rookStart, rookEnd;
            if (start - end < 0) {
                rookStart = start + 3;
                rookEnd = start + 1;
            } else {
                rookStart = start - 4;
                rookEnd = start - 1;
            }

            //move the rook, while handling the hash and piecePositions
            hash ^= Zobrist.getKey(rookEnd, tile[rookEnd]);
            tile[rookStart] = tile[rookEnd];
            tile[rookEnd] = 0;
            piecePositions.get(Piece.color(tile[rookStart])).remove(Integer.valueOf(rookEnd));
            piecePositions.get(Piece.color(tile[rookStart])).add(rookStart);
            hash ^= Zobrist.getKey(rookStart, tile[rookStart]);

            revertGameStats(start, end, state);
            return;
        }

        if (flag == Move.ENPASSANT) {
            //put enemy pawn back
            int enPassant = BoardState.enPassant(state);
            tile[enPassant] = Piece.create(Piece.PAWN, turn);
            piecePositions.get(Piece.color(tile[enPassant])).add(enPassant);
            addToRemainingPiece(tile[enPassant]);

            hash ^= Zobrist.getKey(enPassant, tile[enPassant]);
            revertGameStats(start, end, state);
            return;
        }

        if (flag == Move.PROMOTION) {
            //remove promotion from remaining pieces
            removePieceFromRemainingPieces(tile[end]);

            //put pawn back
            int color = Piece.color(tile[end]);
            tile[start] = Piece.create(Piece.PAWN, color);
            addToRemainingPiece(tile[start]);

            revertGameStats(start, end, state);
        }
    }

    /**
     * Reverts variables that keep track of the game state to the last move state
     */
    private void revertGameStats(int start, int end, int state) {
        //update hash with reverted piece
        hash ^= Zobrist.getKey(start, tile[start]);

        //revert castle rights
        hash ^= Zobrist.CASTLE_KEYS[castleRights];
        castleRights = BoardState.castleRights(state);
        hash ^= Zobrist.CASTLE_KEYS[castleRights];

        //undo enPassant hash if needed
        enPassant = BoardState.enPassant(state);
        if (enPassant > 0) {
            hash ^= Zobrist.EN_PASSANT[enPassant];
        }

        //revert turn, make sure hash is accurate
        hash ^= Zobrist.getColorKey(turn);
        turn = (turn == Piece.WHITE) ? Piece.BLACK : Piece.WHITE;
        hash ^= Zobrist.getColorKey(turn);

        lastCaptOrPawnAdv = BoardState.getLastCaptOrPawnAdv(state);

        gameOver = false;
        currentMove--;
    }

    // <--> MAKING MOVES <--> //

    // <--> GAME OVER CONDITIONS <--> //

    /**
     * Updates the gameOver variable according to the current game state
     */
    private void checkGameOver() {
        int color = (turn == Piece.WHITE) ? Piece.BLACK : Piece.WHITE;
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
     * Determines if the king is in check and has no legal moves.
     *
     * @return true if its checkmate, false otherwise.
     */
    private boolean isCheckMate(List<List<Integer>> attackedSquares) {
        int color = (turn == Piece.WHITE) ? Piece.BLACK : Piece.WHITE;
        return isCheck(color, attackedSquares);
    }

    /**
     * Determines if there is a stalemate.
     *
     * @return true if it's stalemate, false otherwise.
     */
    private boolean isStalemate(List<List<Integer>> attackedSquares) {
        int color = (turn == Piece.WHITE) ? Piece.BLACK : Piece.WHITE;
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
        return currentMove - lastCaptOrPawnAdv >= 100;
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
        if (remainingPieces.containsKey(Piece.create(Piece.PAWN, Piece.WHITE))
                || remainingPieces.containsKey(Piece.create(Piece.PAWN, Piece.BLACK))) {
            return false;
        }

        //if white or black has rooks or queens there is sufficient material
        if (remainingPieces.containsKey(Piece.create(Piece.QUEEN, Piece.WHITE))
                || remainingPieces.containsKey(Piece.create(Piece.ROOK, Piece.WHITE)) ||
                remainingPieces.containsKey(Piece.create(Piece.QUEEN, Piece.BLACK)) ||
                remainingPieces.containsKey(Piece.create(Piece.ROOK, Piece.BLACK))) {
            return false;
        }

        //if white only has a king and one/two knight(s)
        if (remainingPieces.containsKey(Piece.create(Piece.KNIGHT, Piece.WHITE)) && !remainingPieces.containsKey(Piece.create(Piece.BISHOP, Piece.WHITE))) {
            insufficientMaterialWhite = true;
        }

        //if white only has a king and a bishop
        if (remainingPieces.containsKey(Piece.create(Piece.BISHOP, Piece.WHITE)) &&
                !remainingPieces.containsKey(Piece.create(Piece.KNIGHT, Piece.WHITE)) &&
                remainingPieces.get(Piece.create(Piece.BISHOP, Piece.WHITE)) == 1) {
            insufficientMaterialWhite = true;
        }

        //if black only has a king and one/two knight(s)
        if (remainingPieces.containsKey(Piece.create(Piece.KNIGHT, Piece.BLACK)) && !remainingPieces.containsKey(Piece.create(Piece.BISHOP, Piece.BISHOP))) {
            insufficientMaterialBlack = true;
        }

        //if black only has a king and a bishop
        if (remainingPieces.containsKey(Piece.create(Piece.BISHOP, Piece.BLACK)) &&
                !remainingPieces.containsKey(Piece.create(Piece.KNIGHT, Piece.BLACK)) &&
                remainingPieces.get(Piece.create(Piece.BISHOP, Piece.BLACK)) == 1) {
            insufficientMaterialBlack = true;
        }

        return insufficientMaterialWhite && insufficientMaterialBlack;
    }

    // <--> GAME OVER CONDITIONS <--> //

    // <--> FEN AND PRINTING <--> //

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
            fen.append(Piece.string(tile[index]));

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
        String colorToMoveString = (turn == Piece.WHITE) ? "w" : "b";
        fen.append(" ").append(colorToMoveString).append(" ").append(castleRights).append(" ").append(enPassant)
                .append(" ").append(currentMove - lastCaptOrPawnAdv).append(" ").append(currentMove / 2);
        return fen.toString();
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
                    board.append(Piece.string(piece)).append(" | ");
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

    public void printComplete() {
        System.out.println(this);
        System.out.println(hash);
        System.out.println(piecePositions);
        System.out.println(positionHistory);
        System.out.println(Integer.toBinaryString(castleRights));
    }

    // <--> FEN AND PRINTING <--> //

    // <--> HELPER FUNCTIONS <--> //

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
     * Goes through the board to get the coordinates of the king of the
     * given color.
     *
     * @param color color of the king you want the coordinates of.
     * @return an array containing the x and y-axis coordinates of the king.
     */
    public int getKingIndex(int color) {
        int king = Piece.create(Piece.KING, color);

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

    // <--> HELPER FUNCTIONS <--> //

    // <--> GETTERS, SETTERS AND SUCH <--> //

    private boolean isWhite(int color) {
        return color == Piece.WHITE;
    }

    public boolean isKing(int index) {
        return Piece.type(tile[index]) == Piece.KING;
    }

    public boolean isQueen(int index) {
        return Piece.type(tile[index]) == Piece.QUEEN;
    }

    public boolean isBishop(int index) {
        return Piece.type(tile[index]) == Piece.BISHOP;
    }

    public boolean isRook(int index) {
        return Piece.type(tile[index]) == Piece.ROOK;
    }

    public boolean isKnight(int index) {
        return Piece.type(tile[index]) == Piece.KNIGHT;
    }

    public boolean isPawn(int index) {
        return Piece.type(tile[index]) == Piece.PAWN;
    }

    public boolean isEmpty(int index) {
        return tile[index] == 0;
    }

    public boolean isColor(int index, int color) {
        return Piece.color(tile[index]) == color;
    }

    public int getPieceType(int index) {
        return Piece.type(tile[index]);
    }

    public int getPieceColor(int index) {
        return Piece.color(tile[index]);
    }

    public int getTurn() {
        return turn;
    }

    public void setEnPassant(int enPassant) {
        this.enPassant = enPassant;
    }

    public int getEnPassant() {
        return enPassant;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public void setCurrentMove(int currentMove) {
        this.currentMove = currentMove;
    }

    public void setLastCaptOrPawnAdv(int lastCaptOrPawnAdv) {
        this.lastCaptOrPawnAdv = lastCaptOrPawnAdv;
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

    public boolean isGameOver() {
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

    // <--> GETTERS, SETTERS AND SUCH <--> //
}