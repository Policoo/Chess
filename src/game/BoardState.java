package game;

public class BoardState {
    private static final int ENPASSANT_MASK = 0x3F;
    private static final int CASTLE_MASK = 0x3C0;
    private static final int TILE_MASK = 0x7C00;

    public static int encode(int enPassant, int castleRights, int tile, int lastCaptureOrPawnAdv) {
        return enPassant | (castleRights << 6) | (tile << 10) | (lastCaptureOrPawnAdv << 15);
    }

    public static int enPassant(int boardState) {
        return boardState & ENPASSANT_MASK;
    }

    public static int castleRights(int boardState) {
        return (boardState & CASTLE_MASK) >> 6;
    }

    public static int targetTile(int boardState) {
        return (boardState & TILE_MASK) >> 10;
    }

    public static int getLastCaptOrPawnAdv(int boardState) {
        return boardState >> 15;
    }
}
