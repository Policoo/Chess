package game;

import utils.MoveGenerator;
import utils.Utils;
import utils.Zobrist;

import java.util.*;

public class Board {
    private final int[] tile;

    private HashMap<Integer, List<Integer>> piecePositions;
    private HashMap<Long, Integer> positionHistory;
    private HashMap<Integer, Integer> remainingPieces;
    private final List<Integer> boardState;
    private final HashMap<Integer, Integer> kingPositions;
    private HashMap<Integer, List<Long>> attackedTiles;
    private HashMap<Integer, Long> attackMap;
    private HashMap<Integer, List<Long>> pins;

    private int enPassant;
    private int castleRights;
    private int currentMove;
    private int lastCaptOrPawnAdv;
    private int turn;
    private long hash;
    private long check;
    private long doubleCheck;
    private boolean gameOver;

    // <--> INITIALIZATION <--> //

    public Board() {
        positionHistory = new HashMap<>();
        remainingPieces = new HashMap<>();
        kingPositions = new HashMap<>();

        gameOver = false;
        currentMove = 2;
        lastCaptOrPawnAdv = 1;
        enPassant = 0;
        tile = new int[64];
        turn = Piece.WHITE;
        castleRights = 15;
        boardState = new ArrayList<>();

        startPosition();
        hashPosition();
        initializePiecePositions();
        initializeAttackTiles();
        initializePinLines();
        determineCheckLine();
    }

    public Board(String fenString) {
        positionHistory = new HashMap<>();
        remainingPieces = new HashMap<>();
        kingPositions = new HashMap<>();
        piecePositions = new HashMap<>();

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
        initializePiecePositions();
        initializeAttackTiles();
        initializePinLines();
        determineCheckLine();
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
     * Sets up the board in the start position.
     */
    public void startPosition() {
        piecePositions = new HashMap<>();
        piecePositions.put(Piece.BLACK, new ArrayList<>());
        piecePositions.put(Piece.WHITE, new ArrayList<>());

        tile[0] = Piece.create(Piece.ROOK, Piece.BLACK);
        tile[1] = Piece.create(Piece.KNIGHT, Piece.BLACK);
        tile[2] = Piece.create(Piece.BISHOP, Piece.BLACK);
        tile[3] = Piece.create(Piece.QUEEN, Piece.BLACK);
        tile[4] = Piece.create(Piece.KING, Piece.BLACK);
        tile[5] = Piece.create(Piece.BISHOP, Piece.BLACK);
        tile[6] = Piece.create(Piece.KNIGHT, Piece.BLACK);
        tile[7] = Piece.create(Piece.ROOK, Piece.BLACK);
        for (int index = 8; index < 16; index++) {
            tile[index] = Piece.create(Piece.PAWN, Piece.BLACK);
        }

        for (int index = 48; index < 56; index++) {
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

        kingPositions.put(Piece.BLACK, 4);
        kingPositions.put(Piece.WHITE, 60);
    }

    /**
     * Constructs the board from the given fen string.
     *
     * @param fenString fen string of the position.
     */
    private void makeBoardFromFen(String fenString) {
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
        HashMap<Integer, Integer> indexes = new HashMap<>();
        indexes.put(Piece.WHITE, 0);
        indexes.put(Piece.BLACK, 0);
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
                    kingPositions.put(color, index);
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
            tile[index] = Piece.setIndex(tile[index], indexes.get(Piece.color(tile[index])));
            indexes.replace(Piece.color(tile[index]), indexes.get(Piece.color(tile[index])) + 1);
            addToRemainingPiece(Piece.ignoreIndex(tile[index]));

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

    private void initializePiecePositions() {
        piecePositions.put(Piece.WHITE, new ArrayList<>(20));
        piecePositions.put(Piece.BLACK, new ArrayList<>(20));

        int whiteIndex = 0;
        int blackIndex = 0;
        for (int index = 0; index < 64; index++) {
            if (tile[index] == 0) {
                continue;
            }

            if (Piece.color(tile[index]) == Piece.WHITE) {
                tile[index] = Piece.setIndex(tile[index], whiteIndex);
                whiteIndex++;
            } else {
                tile[index] = Piece.setIndex(tile[index], blackIndex);
                blackIndex++;
            }

            piecePositions.get(Piece.color(tile[index])).add(index);
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
        boardState.add(BoardState.encode(enPassant, castleRights, tile[end], lastCaptOrPawnAdv, Piece.index(tile[enPassant])));

        //remove start piece and end piece (if there is one) from the hash
        hash ^= Zobrist.getKey(start, tile[start]);
        if (tile[end] != 0) {
            hash ^= Zobrist.getKey(end, tile[end]);

            //handle a capture (since we are checking tile[end] != 0 anyway)
            removePieceFromRemainingPieces(Piece.ignoreIndex(tile[end]));
            piecePositions.get(Piece.color(tile[end])).set(Piece.index(tile[end]), -1);
            attackedTiles.get(Piece.color(tile[end])).set(Piece.index(tile[end]), 0L);
            lastCaptOrPawnAdv = currentMove;
        }

        //move the piece
        tile[end] = tile[start];
        tile[start] = 0;
        piecePositions.get(Piece.color(tile[end])).set(Piece.index(tile[end]), end);

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
            piecePositions.get(Piece.color(tile[rookEnd])).set(Piece.index(tile[rookEnd]), rookEnd);
            hash ^= Zobrist.getKey(rookEnd, tile[rookEnd]);

            for (int color : attackedTiles.keySet()) {
                List<Long> attackedTilesColor = attackedTiles.get(color);

                for (int i = 0; i < attackedTilesColor.size(); i++) {
                    //no point in looking at the moved piece, since we will recalculate the attacked tiles for it anyway
                    if (i == rookEnd) {
                        continue;
                    }

                    if ((attackedTilesColor.get(i) & (1L << rookStart)) != 0 || (attackedTilesColor.get(i) & (1L << rookEnd)) != 0) {
                        int piecePosition = piecePositions.get(color).get(i);
                        if (piecePosition != -1) {
                            attackedTiles.get(color).set(i, calculateAttackedTiles(piecePosition));
                        }
                    }
                }
            }

            attackedTiles.get(Piece.color(tile[rookEnd])).set(Piece.index(tile[rookEnd]), calculateAttackedTiles(rookEnd));

            updateGameState(start, end);
            return;
        }

        if (flag == Move.EN_PASSANT) {
            //capture enemy pawn
            hash ^= Zobrist.getKey(enPassant, tile[enPassant]);
            removePieceFromRemainingPieces(tile[enPassant]);
            piecePositions.get(Piece.color(tile[enPassant])).set(Piece.index(tile[enPassant]), -1);
            attackedTiles.get(Piece.color(tile[enPassant])).set(Piece.index(tile[enPassant]), 0L);
            tile[enPassant] = 0;

            lastCaptOrPawnAdv = currentMove;
            updateGameState(start, end);
            return;
        }

        if (flag == Move.PROMOTION) {
            //edit remaining pieces
            removePieceFromRemainingPieces(tile[end]);
            addToRemainingPiece(Piece.ignoreIndex(tile[end]));

            //create promoted piece
            int index = Piece.index(tile[end]);
            tile[end] = Piece.create(move.getPromotion(), Piece.color(tile[end]));
            tile[end] = Piece.setIndex(tile[end], index);

            lastCaptOrPawnAdv = currentMove;
            updateGameState(start, end);
        }
    }

    /**
     * Updates variables that keeps track of game state, namely colorToMove, positionHistory, currentMove,
     * enPassant.
     */
    private void updateGameState(int start, int end) {
        updateAttackedTilesAndPins(start, end);

        if (isKing(end)) {
            kingPositions.replace(turn, end);
        }

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
        determineCheckLine();
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
        piecePositions.get(Piece.color(tile[start])).set(Piece.index(tile[start]), start);

        //handle a capture
        if (tile[end] != 0) {
            addToRemainingPiece(Piece.ignoreIndex(tile[end]));
            piecePositions.get(Piece.color(tile[end])).set(Piece.index(tile[end]), end);
            attackedTiles.get(Piece.color(tile[end])).set(Piece.index(tile[end]), calculateAttackedTiles(end));

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
            piecePositions.get(Piece.color(tile[rookStart])).set(Piece.index(tile[rookStart]), rookStart);
            hash ^= Zobrist.getKey(rookStart, tile[rookStart]);

            for (int color : attackedTiles.keySet()) {
                List<Long> attackedTilesColor = attackedTiles.get(color);

                for (int i = 0; i < attackedTilesColor.size(); i++) {
                    //no point in looking at the moved piece, since we will recalculate the attacked tiles for it anyway
                    if (i == rookEnd) {
                        continue;
                    }

                    if ((attackedTilesColor.get(i) & (1L << rookStart)) != 0 || (attackedTilesColor.get(i) & (1L << rookEnd)) != 0) {
                        int piecePosition = piecePositions.get(color).get(i);
                        if (piecePosition != -1) {
                            attackedTiles.get(color).set(i, calculateAttackedTiles(piecePosition));
                        }
                    }
                }
            }

            attackedTiles.get(Piece.color(tile[rookStart])).set(Piece.index(tile[rookStart]), calculateAttackedTiles(rookStart));
            revertGameStats(start, end, state);
            return;
        }

        if (flag == Move.EN_PASSANT) {
            //put enemy pawn back
            int enPassant = BoardState.enPassant(state);
            tile[enPassant] = Piece.create(Piece.PAWN, turn);
            tile[enPassant] = Piece.setIndex(tile[enPassant], BoardState.enPassantIndex(state));
            piecePositions.get(Piece.color(tile[enPassant])).set(Piece.index(tile[enPassant]), enPassant);
            addToRemainingPiece(Piece.ignoreIndex(tile[enPassant]));
            attackedTiles.get(Piece.color(tile[enPassant])).set(Piece.index(tile[enPassant]), calculateAttackedTiles(enPassant));

            hash ^= Zobrist.getKey(enPassant, tile[enPassant]);
            revertGameStats(start, end, state);
            return;
        }

        if (flag == Move.PROMOTION) {
            //remove promotion from remaining pieces
            removePieceFromRemainingPieces(tile[end]);

            //put pawn back
            int color = Piece.color(tile[start]);
            int index = Piece.index(tile[start]);
            tile[start] = Piece.create(Piece.PAWN, color);
            tile[start] = Piece.setIndex(tile[start], index);
            addToRemainingPiece(Piece.ignoreIndex(tile[start]));

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

        if (isKing(start)) {
            kingPositions.replace(turn, start);
        }

        lastCaptOrPawnAdv = BoardState.getLastCaptOrPawnAdv(state);

        gameOver = false;
        currentMove--;
        updateAttackedTilesAndPins(end, start);
        determineCheckLine();
    }

    private void updateAttackedTilesAndPins(int oldIndex, int newIndex) {
        int[] colors = new int[]{Piece.WHITE, Piece.BLACK};
        int pieceIndex = Piece.index(tile[newIndex]);

        //calculate the attacked tiles for the moved piece
        attackedTiles.get(turn).set(pieceIndex, calculateAttackedTiles(newIndex));

        for (int color : colors) {
            List<Long> attackedTilesColor = attackedTiles.get(color);
            long attackMapBitboard = 0L;

            for (int i = 0; i < attackedTilesColor.size(); i++) {
                //no point in looking at the moved piece, since we will recalculate the attacked tiles for it anyway
                if (i == pieceIndex) {
                    attackMapBitboard |= attackedTilesColor.get(i);
                    continue;
                }

                if ((attackedTilesColor.get(i) & (1L << oldIndex)) != 0 || (attackedTilesColor.get(i) & (1L << newIndex)) != 0) {
                    int piecePosition = piecePositions.get(color).get(i);
                    if (piecePosition != -1) {
                        attackedTiles.get(color).set(i, calculateAttackedTiles(piecePosition));
                    }
                }

                attackMapBitboard |= attackedTilesColor.get(i);
            }

            attackMap.replace(color, attackMapBitboard);
        }

        pins.get(turn).clear();

        //check if any piece is pinning anything
        List<Integer> piecePositionsTurn = piecePositions.get(turn);
        for (int piecePosition : piecePositionsTurn) {
            if (piecePosition == -1) {
                continue;
            }

            long pinLine = calculatePinLine(piecePosition);
            if (pinLine != 0L) {
                pins.get(turn).add(pinLine);
            }
        }
    }

    // <--> MAKING MOVES <--> //

    // <--> ATTACKED TILES AND PIN LINES <--> //

    private void initializeAttackTiles() {
        int[] colors = new int[]{Piece.WHITE, Piece.BLACK};
        attackedTiles = new HashMap<>();
        attackMap = new HashMap<>();

        for (int color : colors) {
            attackedTiles.put(color, new ArrayList<>());

            long attackMapBitboard = 0L;
            for (int piecePosition : piecePositions.get(color)) {
                long pieceLineOfSights = calculateAttackedTiles(piecePosition);
                attackMapBitboard |= pieceLineOfSights;

                if (pieceLineOfSights != 0L) {
                    attackedTiles.get(color).add(pieceLineOfSights);
                }
            }

            attackMap.put(color, attackMapBitboard);
        }
    }

    private long calculateAttackedTiles(int index) {
        int pieceType = (isPawn(index)) ? Piece.ignoreIndex(tile[index]) : Piece.type(tile[index]);
        int pieceColor = Piece.color(tile[index]);
        int[] directions = PrecomputedGameData.pieceDirections.get(pieceType);

        switch (Piece.type(tile[index])) {
            case Piece.PAWN:
            case Piece.KING:
            case Piece.KNIGHT:
                //we precomputed this data already
                return PrecomputedGameData.pieceAttackTilesBitboards.get(pieceType)[index];
            default:
                //sliding pieces would go in default
                long bitboard = 0L;

                for (int dir : directions) {
                    int numSteps = PrecomputedGameData.edgeOfBoard.get(dir)[index];
                    int curIndex = index;

                    for (int step = numSteps; step > 0; step--) {
                        curIndex = curIndex + dir;
                        bitboard |= (1L << (curIndex));

                        //if the tile is empty, keep going
                        if (tile[curIndex] == 0) {
                            continue;
                        }

                        //if we find the opponents king, keep going. This is to cover an edge case in checks
                        if (isKing(curIndex) && Piece.color(tile[curIndex]) != pieceColor) {
                            continue;
                        }

                        //if we get here it means that we found a piece we don't care about, so break
                        break;
                    }
                }

                return bitboard;
        }
    }

    private void initializePinLines() {
        pins = new HashMap<>();
        pins.put(Piece.WHITE, new ArrayList<>());
        pins.put(Piece.BLACK, new ArrayList<>());

        for (int index = 0; index < 64; index++) {
            if (tile[index] == 0) {
                continue;
            }

            long pinLine = calculatePinLine(index);
            if (pinLine != 0) {
                pins.get(Piece.color(tile[index])).add(pinLine);
            }
        }
    }

    private long calculatePinLine(int index) {
        int otherTurn = (turn == Piece.WHITE) ? Piece.BLACK : Piece.WHITE;
        int kingIndex = kingPositions.get(otherTurn);

        //pawns, knights and kings can't pin
        if (isPawn(index) || isKnight(index) || isKing(index)) {
            return 0L;
        }

        int[] directions = PrecomputedGameData.pieceDirections.get(Piece.type(tile[index]));
        for (int dir : directions) {
            int posDif = kingIndex - index;
            int steps = PrecomputedGameData.edgeOfBoard.get(dir)[index];

            //if piece can't see the king in this direction, or if it's at the edge of the board, don't search
            if (dir * posDif < 0 || posDif % dir != 0 || steps == 0) {
                continue;
            }

            //everything % 1 == 0, so make sure that it's actually worth searching this direction
            if (Math.abs(dir) == 1 && index / 8 != kingIndex / 8) {
                continue;
            }

            int curIndex = index;
            boolean canPin = false;

            //we add the square that the piece is on, so that captures can break the pin
            long bitboard = (1L << index);

            for (int step = 0; step < steps; step++) {
                curIndex = curIndex + dir;

                //if tile is empty, add it to the bitboard
                if (tile[curIndex] == 0) {
                    bitboard |= (1L << curIndex);
                    continue;
                }

                //if you see your own piece, that means it's not a pin in that direction
                if (Piece.color(tile[index]) == Piece.color(tile[curIndex])) {
                    break;
                }

                //if we find a king of the opposite color, and it's a pin, so return the pin line
                if (isKing(curIndex)) {
                    if (canPin) {
                        bitboard |= (1L << curIndex);
                        return bitboard;
                    } else {
                        break;
                    }
                }

                /*if we get here, the tile contains a piece that is of the other color. If canPin is true, this is
                the second opponents piece we encountered, so a pin is not possible*/
                if (canPin) {
                    break;
                }

                //if we get here that means we found an opponents piece that is not a king, so a pin is possible
                bitboard |= (1L << curIndex);
                canPin = true;
            }
        }

        return 0L;
    }

    // <--> ATTACKED TILES AND PIN LINES <--> //

    // <--> GAME OVER, CHECKS AND MOVE LEGALITY <--> //

    /**
     * Updates the gameOver variable according to the current game state
     */
    private void checkGameOver() {
        //if no legal moves exist, it's either checkmate or stalemate
        if (!MoveGenerator.legalMovesExist(this)) {
            gameOver = true;
            return;
        }

        if (isRepetition() || fiftyMoveRule() || insufficientMaterial()) {
            gameOver = true;
        }
    }

    /**
     * Determines if the king of player whose turn it is, is in check.
     *
     * @return true if the king is in check, false otherwise.
     */
    public boolean isCheck() {
        return check != 0;
    }

    /**
     * Determines if the king is in check and has no legal moves.
     *
     * @return true if its checkmate, false otherwise.
     */
    private boolean isCheckMate() {
        return isCheck();
    }

    /**
     * Determines if there is a stalemate.
     *
     * @return true if it's stalemate, false otherwise.
     */
    private boolean isStalemate() {
        int color = (turn == Piece.WHITE) ? Piece.BLACK : Piece.WHITE;
        return !isCheck();
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

    private void determineCheckLine() {
        int otherColor = (turn == Piece.WHITE) ? Piece.BLACK : Piece.WHITE;
        int kingIndex = kingPositions.get(turn);
        List<Long> attackedTilesColor = attackedTiles.get(otherColor);
        check = 0L;
        doubleCheck = 0L;

        for (int index = 0; index < attackedTilesColor.size(); index++) {
            long pieceAttackTiles = attackedTilesColor.get(index);

            //if this piece can't see the king, continue
            if ((pieceAttackTiles & (1L << kingIndex)) == 0L) {
                continue;
            }

            //all this shit is to figure out in which direction the king is getting checked
            int piecePosition = piecePositions.get(otherColor).get(index);
            int pieceType = (isPawn(piecePosition)) ? Piece.ignoreIndex(tile[piecePosition]) : Piece.type(tile[piecePosition]);
            int[] directions = PrecomputedGameData.pieceDirections.get(pieceType);
            int posDif = kingIndex - piecePosition;
            int finalDir = 0;
            for (int dir : directions) {
                if (dir == posDif) {
                    finalDir = dir;
                    break;
                }

                if (dir * posDif < 0 || posDif % dir != 0) {
                    continue;
                }

                if (Math.abs(dir) == 1) {
                    if (kingIndex / 8 == piecePosition / 8) {
                        finalDir = dir;
                        break;
                    }
                    continue;
                }

                finalDir = dir;
            }

            int numSteps = PrecomputedGameData.edgeOfBoard.get(finalDir)[piecePosition];
            long bitboard = (1L << piecePosition);

            int curIndex = piecePosition;
            for (int step = numSteps; step > 0; step--) {
                curIndex = curIndex + finalDir;
                bitboard |= (1L << (curIndex));

                //if we find a piece, we can't see past it, so break
                if (tile[curIndex] != 0) {
                    break;
                }
            }

            if (check == 0L) {
                check = bitboard;
            } else {
                doubleCheck = bitboard;
                return;
            }
        }
    }

    public boolean isLegalMove(int start, int end) {
        int otherColor = (turn == Piece.WHITE) ? Piece.BLACK : Piece.WHITE;
        List<Long> pinLinesColor = pins.get(otherColor);

        //if we are moving the king, make sure he is not in a square attacked by the opponent
        if (isKing(start)) {
            //it is a legal move if the king is not attacked
            return (attackMap.get(otherColor) & (1L << end)) == 0;
        }

        //if we get here that means we are not moving the king, and it's a double check, so move is not legal
        if (doubleCheck != 0L) {
            return false;
        }

        //if the piece is pinned, it can't move out of the pin line, so make sure it doesn't do that
        for (long pinLine : pinLinesColor) {
            //if this piece is not pinned, continue
            if ((pinLine & (1L << start)) == 0) {
                continue;
            }

            //if we get here the piece is pinned, so make sure it's not illegally moving out of the pin
            if ((pinLine & (1L << end)) == 0) {
                return false;
            }

            //if we get here that means the piece is pinned, but this move is in the pin line, so it could be legal
            break;
        }

        //if we are in check, make sure to either block it or capture the piece checking the king
        if (check != 0L) {
            //if we are either capturing the piece checking us or moving in front of the check, it's legal
            return (check & (1L << end)) != 0;
        }

        //if we get here, the piece isn't pinned, and it's not a check, so it's free to move
        return true;
    }

    // <--> GAME OVER, CHECKS AND MOVE LEGALITY <--> //

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

    public String getFullState() {
        int[] colors = new int[]{Piece.WHITE, Piece.BLACK};

        StringBuilder state = new StringBuilder(positionToFen() + "\n");
        state.append(this).append("\n");
        state.append("Turn: ").append(turn).append("\n");
        state.append("EnPassant: ").append(enPassant).append("\n");
        state.append("Current move: ").append(currentMove).append("\n");
        state.append("Last capture or pawn advancement: ").append(lastCaptOrPawnAdv).append("\n");
        state.append("Hash: ").append(hash).append("\n");
        state.append("Piece positions:\n" +
                "    White: ").append(piecePositions.get(Piece.WHITE)).append("\n" +
                "    Black: ").append(piecePositions.get(Piece.BLACK)).append("\n");
        state.append("Position history: ").append(positionHistory).append("\n");
        state.append("Castle rights:" +
                " ").append(String.format("%04d", Integer.parseInt(Integer.toBinaryString(castleRights)))).append("\n");
        state.append("King positions:" +
                " White = ").append(kingPositions.get(Piece.WHITE)).append("" +
                ", Black = ").append(kingPositions.get(Piece.BLACK)).append("\n");
        state.append("Check:\n").append(Utils.bitboardToString(check)).append("\n");
        state.append("Double check:\n").append(Utils.bitboardToString(doubleCheck)).append("\n");
        state.append("Attack map white:\n");
        for (int color : colors) {
            if (color == Piece.BLACK) {
                state.append("Attack map black:\n");
            }
            state.append(Utils.bitboardToString(attackMap.get(color)));
        }
        state.append("Attacked tiles white:\n");
        for (int color : colors) {
            if (color == Piece.BLACK) {
                state.append("Attacked tiles black:\n");
            }

            for (int i = 0; i < attackedTiles.get(color).size(); i++) {
                state.append(Utils.bitboardToString(attackedTiles.get(color).get(i))).append("\n");
            }
        }
        state.append("Pins white:\n");
        for (int color : colors) {
            if (color == Piece.BLACK) {
                state.append("Pins black:\n");
            }

            for (int i = 0; i < pins.get(color).size(); i++) {
                state.append(Utils.bitboardToString(pins.get(color).get(i))).append("\n");
            }
        }
        return state.toString();
    }

    // <--> FEN AND PRINTING <--> //

    // <--> HELPER FUNCTIONS <--> //

    /**
     * Removes piece from list that the board uses to keep track of remaining pieces.
     *
     * @param piece piece you want to remove.
     */
    private void removePieceFromRemainingPieces(int piece) {
        int noIndexPiece = Piece.ignoreIndex(piece);

        //remove piece if it's the last one
        if (remainingPieces.get(noIndexPiece) == 1) {
            remainingPieces.remove(noIndexPiece);
        } else {
            int nr = remainingPieces.get(noIndexPiece);
            nr--;
            remainingPieces.replace(noIndexPiece, nr);
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