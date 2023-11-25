package utils;

import game.Piece;

import java.util.Random;

public class Zobrist {
    private static long[][] KEYS;
    private static long[] COLOR;

    public static long[] CASTLEKEYS;
    public static long[] ENPASSANT;

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

        CASTLEKEYS = new long[16];
        for (int right = 0; right < 16; right++) {
            CASTLEKEYS[right] = random.nextLong();
        }

        COLOR = new long[2];
        for (int color = 0; color < 2; color++) {
            COLOR[color] = random.nextLong();
        }

        ENPASSANT = new long[64];
        for (int index = 0; index < 4; index++) {
            ENPASSANT[index] = random.nextLong();
        }
    }

    public static long getKey(int index, int piece) {
        piece = (piece > 6) ? piece - 3 : piece - 1;
        return KEYS[index][piece];
    }

    public static long getColorKey(int color) {
        if (color == Piece.BLACK) {
            return COLOR[1];
        }

        return COLOR[color];
    }
}
