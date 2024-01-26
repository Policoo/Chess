package game;

public class Piece {
    // Constants for piece types
    public static final int PAWN = 1;
    public static final int ROOK = 2;
    public static final int KNIGHT = 3;
    public static final int BISHOP = 4;
    public static final int QUEEN = 5;
    public static final int KING = 6;

    // Constants for piece colors
    public static final int WHITE = 8;
    public static final int BLACK = 16;

    // Masks
    private static final int TYPE_MASK = 0x7;
    private static final int COLOR_MASK = 0x18;
    private static final int INDEX_MASK = 0x3E0;

    public static int create(int type, int color) {
        return type | color;
    }

    public static int type(int piece) {
        return piece & TYPE_MASK;
    }

    public static int color(int piece) {
        return piece & COLOR_MASK;
    }

    public static int index(int piece) {
        return (piece & INDEX_MASK) >> 5;
    }

    public static int setIndex(int piece, int index) {
        return piece | (index << 5);
    }

    public static int ignoreIndex(int piece) {
        return piece & (TYPE_MASK | COLOR_MASK);
    }

    public static String string(int piece) {
        int color = color(piece);
        int type = type(piece);

        String pieceStr = "";

        if (color == WHITE) {
            switch (type) {
                case PAWN:
                    pieceStr = "P";
                    break;
                case KNIGHT:
                    pieceStr = "N";
                    break;
                case BISHOP:
                    pieceStr = "B";
                    break;
                case ROOK:
                    pieceStr = "R";
                    break;
                case QUEEN:
                    pieceStr = "Q";
                    break;
                case KING:
                    pieceStr = "K";
                    break;
            }
        } else if (color == BLACK) {
            switch (type) {
                case PAWN:
                    pieceStr = "p";
                    break;
                case KNIGHT:
                    pieceStr = "n";
                    break;
                case BISHOP:
                    pieceStr = "b";
                    break;
                case ROOK:
                    pieceStr = "r";
                    break;
                case QUEEN:
                    pieceStr = "q";
                    break;
                case KING:
                    pieceStr = "k";
                    break;
            }
        }

        return pieceStr;
    }
}
