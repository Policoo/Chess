package game;

import utils.Utils;

import java.util.HashMap;

public class PrecomputedGameData {
    /**
     * HashMap that holds information about how many squares you can go in a direction until you
     * hit the edge of the board. The key is the direction you are going in. The value is an int[], where
     * accessing the element at 0 would tell you how many squares a piece at index 0 on the board can go in a
     * direction, before it hits the edge of the board.
     */
    public static HashMap<Integer, int[]> edgeOfBoard;

    /**
     * HashMap that holds information about which directions each piece can go in. The key is the piece type,
     * or, in case of pawns, the piece type + color (this is because white and black pawns go in different directions).
     * The value is an int[] containing all the directions that a piece can go in.
     */
    public static HashMap<Integer, int[]> pieceDirections;

    /**
     * HashMap that contains all the squares that a sliding piece can see from any given square on the board.
     * The key is the piece type, or, in case of pawns, the piece type + color (this is because white
     * and black pawns go in different directions). The value is a long[]. To access a specific bitboard,
     * you ask for the element at the pieceIndex.
     * <br>
     * <br>
     * For example, if I have a pawn on index 0 on the board, I will ask for the element at index 0 in the list.
     * The returned value will be a bitboard representing all the squares that that piece can see.
     */
    public static HashMap<Integer, long[]> pieceAttackTilesBitboards;

    /**
     * Calculates and stores the distance to the edge of the board for each direction, stores all the
     * directions that each piece type can go in, initializes the bitboards corresponding to the attacked tiles
     */
    public static void initialize() {
        initializeEdgeOfBoard();
        initializePieceDirections();
        initializeAttackTilesBitboards();
    }

    private static void initializeEdgeOfBoard() {
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
    }

    private static void initializePieceDirections() {
        pieceDirections = new HashMap<>();
        pieceDirections.put(Piece.KING, new int[]{-9, -8, -7, -1, 1, 7, 8, 9});
        pieceDirections.put(Piece.QUEEN, new int[]{-9, -8, -7, -1, 1, 7, 8, 9});
        pieceDirections.put(Piece.BISHOP, new int[]{-9, -7, 7, 9});
        pieceDirections.put(Piece.ROOK, new int[]{-8, -1, 1, 8});
        pieceDirections.put(Piece.KNIGHT, new int[]{-17, -15, -10, -6, 6, 10, 15, 17});
        pieceDirections.put(Piece.create(Piece.PAWN, Piece.WHITE), new int[]{-8, -9, -7});
        pieceDirections.put(Piece.create(Piece.PAWN, Piece.BLACK), new int[]{8, 9, 7});
    }

    private static void initializeAttackTilesBitboards() {
        pieceAttackTilesBitboards = new HashMap<>();
        pieceAttackTilesBitboards.put(Piece.create(Piece.PAWN, Piece.WHITE), new long[64]);
        int[] directions = pieceDirections.get(Piece.create(Piece.PAWN, Piece.WHITE));
        for (int i = 0; i < 64; i++) {
            long bitboard = 0;

            for (int dirIndex = 1; dirIndex < 3; dirIndex++) {
                //if the pawn wouldn't go off the board, it can attack
                if (edgeOfBoard.get(directions[dirIndex])[i] > 0) {
                    bitboard |= (1L << (i + directions[dirIndex]));
                }
            }

            pieceAttackTilesBitboards.get(Piece.create(Piece.PAWN, Piece.WHITE))[i] = bitboard;
        }

        pieceAttackTilesBitboards.put(Piece.create(Piece.PAWN, Piece.BLACK), new long[64]);
        directions = pieceDirections.get(Piece.create(Piece.PAWN, Piece.BLACK));
        for (int i = 0; i < 64; i++) {
            long bitboard = 0;

            for (int dirIndex = 1; dirIndex < 3; dirIndex++) {
                //if the piece wouldn't go off the board, it can attack
                if (edgeOfBoard.get(directions[dirIndex])[i] > 0) {
                    bitboard |= (1L << (i + directions[dirIndex]));
                }
            }

            pieceAttackTilesBitboards.get(Piece.create(Piece.PAWN, Piece.BLACK))[i] = bitboard;
        }

        pieceAttackTilesBitboards.put(Piece.KNIGHT, new long[64]);
        directions = pieceDirections.get(Piece.KNIGHT);
        for (int i = 0; i < 64; i++) {
            long bitboard = 0;

            for (int dir : directions) {
                //if the piece wouldn't go off the board, it can attack
                if (edgeOfBoard.get(dir)[i] > 0) {
                    bitboard |= (1L << (i + dir));
                }
            }

            pieceAttackTilesBitboards.get(Piece.KNIGHT)[i] = bitboard;
        }

        pieceAttackTilesBitboards.put(Piece.KING, new long[64]);
        directions = pieceDirections.get(Piece.KING);
        for (int i = 0; i < 64; i++) {
            long bitboard = 0;

            for (int dir : directions) {
                //if the piece wouldn't go off the board, it can attack
                if (edgeOfBoard.get(dir)[i] > 0) {
                    bitboard |= (1L << (i + dir));
                }
            }

            pieceAttackTilesBitboards.get(Piece.KING)[i] = bitboard;
        }

        pieceAttackTilesBitboards.put(Piece.QUEEN, new long[64]);
        directions = pieceDirections.get(Piece.QUEEN);
        for (int i = 0; i < 64; i++) {
            long bitboard = 0;

            for (int dir : directions) {
                int numSteps = edgeOfBoard.get(dir)[i];
                int curIndex = i;

                for (int step = numSteps; step > 0; step--) {
                    curIndex = curIndex + dir;
                    bitboard |= (1L << (curIndex));
                }
            }

            pieceAttackTilesBitboards.get(Piece.QUEEN)[i] = bitboard;
        }

        pieceAttackTilesBitboards.put(Piece.ROOK, new long[64]);
        directions = pieceDirections.get(Piece.ROOK);
        for (int i = 0; i < 64; i++) {
            long bitboard = 0;

            for (int dir : directions) {
                int numSteps = edgeOfBoard.get(dir)[i];
                int curIndex = i;

                for (int step = numSteps; step > 0; step--) {
                    curIndex = curIndex + dir;
                    bitboard |= (1L << (curIndex));
                }
            }

            pieceAttackTilesBitboards.get(Piece.ROOK)[i] = bitboard;
        }

        pieceAttackTilesBitboards.put(Piece.BISHOP, new long[64]);
        directions = pieceDirections.get(Piece.BISHOP);
        for (int i = 0; i < 64; i++) {
            long bitboard = 0;

            for (int dir : directions) {
                int numSteps = edgeOfBoard.get(dir)[i];
                int curIndex = i;

                for (int step = numSteps; step > 0; step--) {
                    curIndex = curIndex + dir;
                    bitboard |= (1L << (curIndex));
                }
            }

            pieceAttackTilesBitboards.get(Piece.BISHOP)[i] = bitboard;
        }
    }
}
