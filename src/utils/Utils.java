package utils;

public class Utils {

    private Utils() {
        throw new AssertionError();
    }

    public static int getXFromFormat(int move) {
        int x;
        if (move < 10) {
            x = 0;
        } else {
            x = move / 10;
        }
        return x;
    }

    public static int getYFromFormat(int move) {
        int y;
        if (move == 0) {
            y = 0;
        } else if (move < 10) {
            y = move;
        } else {
            y = move % 10;
        }
        return y;
    }

    public static int formatXY(int x, int y) {
        return x * 10 + y;
    }

    public static int[] getXYCoordinatesFromClick(int x, int y) {
        int[] realCoordinates = new int[2];
        realCoordinates[0] = (int) Math.floor((double) x / 64);
        realCoordinates[1] = (int) Math.floor((double) y / 64);
        return realCoordinates;
    }

    public static String getChessCoordinates(int x, int y) {
        int startLetterAscii = x + 97;
        int startY = 8 - y;
        return (char) startLetterAscii + "" + startY;
    }

    public static int[] getXYFromChessCoordinates(String coordinates)  {
        int[] results = new int[2];
        char[] chars = coordinates.toCharArray();
        int x = (int) chars[0] - 97;
        int y = -1 * (Integer.parseInt(String.valueOf(chars[1])) - 8);

        results[0] = x;
        results[1] = y;
        return results;
    }
}
