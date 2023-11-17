package game;

import utils.Utils;

public class Move {
    public static final int NONE = 0;
    public static final int CASTLE = 1;
    public static final int ENPASSANT = 2;
    public static final int PROMOTION = 3;
    private static final int START_INDEX_MASK = 0x3F;
    private static final int END_INDEX_MASK = 0xFC0;
    public static final int FLAG_MASK = 0x3000;
    public static final int PROMOTION_MASK = 0x1C000;
    private int move;

    /**
     * Constructs a move with the given information and flag.
     *
     * @param start start index of the move.
     * @param end end index of the move.
     * @param flag move flag: 1 - castle, 2 - enPassant, 3 - promotion.
     * @param promotion 0 if not promoting pawn, piece number otherwise.
     */
    public Move(int start, int end, int flag, int promotion) {
        move = start | (end << 6) | (flag << 12) | (promotion << 14);
    }

    public Move() {

    }

    public int getMove() {
        return move;
    }

    public int start() {
        return move & START_INDEX_MASK;
    }

    public int end() {
        return (move & END_INDEX_MASK) >> 6;
    }

    public int flag() {
        return (move & FLAG_MASK) >> 12;
    }

    public int getPromotion() {
        return (move >> 14);
    }

    public void setPromotion(int piece) {
        move = (move & ~PROMOTION_MASK) | (piece << 14);
    }

    @Override
    public String toString() {
        String promotion = "";
        if (((move & FLAG_MASK) >> 12) == 3) {
            promotion = Piece.makeString(getPromotion()).toLowerCase();
        }
        return Utils.getChessCoordinates(start()) + Utils.getChessCoordinates(end()) + promotion;
    }
}
