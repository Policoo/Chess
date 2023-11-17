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
    public static final int WHITE = 0;
    public static final int BLACK = 8;

    public static int createPiece(int type, int color) {
        return type | color;
    }

    public static int getType(int piece) {
        return piece & 7;
    }

    public static int getColor(int piece) {
        return piece & 8;
    }

    public static String makeString(int piece) {
        int color = getColor(piece);
        int type = getType(piece);

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

    public static String getColorString(int color) {
        if (color == WHITE) {
            return "white";
        } else {
            return "black";
        }
    }
}
