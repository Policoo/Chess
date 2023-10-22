package utils;

public class Utils {

    private Utils() {
        throw new AssertionError();
    }

    public static int XYToIndex(int x, int y) {
        return (y * 8) + x;
    }

    public static int[] getXYCoordinatesFromClick(int x, int y) {
        int[] realCoordinates = new int[2];
        realCoordinates[0] = (int) Math.floor((double) x / 64);
        realCoordinates[1] = (int) Math.floor((double) y / 64);
        return realCoordinates;
    }

    public static String getChessCoordinates(int index) {
        int startLetterAscii = (index % 8) + 97;
        int startY = 8 - (index / 8);
        return (char) startLetterAscii + "" + startY;
    }

    public static int getIndexFromChessCoordinates(String coordinates)  {
        char[] chars = coordinates.toCharArray();
        int x = (int) chars[0] - 97;
        int y = 8 * (Integer.parseInt(String.valueOf(chars[1])) - 1);

        return y + x;
    }
}
