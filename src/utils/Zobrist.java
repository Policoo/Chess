package utils;

import game.Piece;

import java.util.Random;

public class Zobrist {
    private static long[][] KEYS;
    private static long[] COLOR;

    public static long[] CASTLE_KEYS;
    public static long[] EN_PASSANT;

    /**
     * Initializes the Zobrist keys
     */
    public static void initialize() {
        Random random = new Random();

        KEYS = new long[64][12];
        for (int piece = 1; piece < 12; piece++) {
            for (int index = 0; index < 64; index++) {
                KEYS[index][piece] = random.nextLong();
            }
        }

        CASTLE_KEYS = new long[16];
        for (int right = 0; right < 16; right++) {
            CASTLE_KEYS[right] = random.nextLong();
        }

        COLOR = new long[2];
        for (int color = 0; color < 2; color++) {
            COLOR[color] = random.nextLong();
        }

        EN_PASSANT = new long[64];
        for (int index = 0; index < 4; index++) {
            EN_PASSANT[index] = random.nextLong();
        }
    }

    public static long getKey(int index, int piece) {
        piece = Piece.ignoreIndex(piece);
        piece = (piece > 14) ? piece  - 13 : piece - 9;
        return KEYS[index][piece];
    }

    public static long getColorKey(int color) {
        if (color == Piece.WHITE) {
            return COLOR[0];
        }

        return COLOR[1];
    }
}
