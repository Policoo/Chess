package utils;

public class Utils {

    private Utils() {
        throw new AssertionError();
    }

    public static String getChessCoordinates(int index) {
        int startLetterAscii = (index % 8) + 97;
        int startY = 8 - (index / 8);
        return (char) startLetterAscii + "" + startY;
    }

    public static int getIndexFromChessCoordinates(String coordinates)  {
        char[] chars = coordinates.toCharArray();
        int x = (int) chars[0] - 97;
        int y = 8 * (8 - Integer.parseInt(String.valueOf(chars[1])));

        return y + x;
    }

    public static String bitboardToString(long bitboard) {
        StringBuilder result = new StringBuilder();

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                long mask = 1L << (rank * 8 + file);
                int bit = (bitboard & mask) != 0 ? 1 : 0;
                result.append(bit).append(" ");
            }
            result.append("\n");
        }

        return result.toString();
    }
}
