package game;

public class BoardState {
    private static final int EN_PASSANT_MASK = 0x3F;
    private static final int CASTLE_MASK = 0x3C0;
    private static final int TILE_MASK = 0xFFC00;
    private static final int EN_PASSANT_INDEX_MASK = 0x1F00000;

    public static int encode(int enPassant, int castleRights, int tile, int lastCaptureOrPawnAdv, int enPassantIndex) {
        return enPassant | (castleRights << 6) | (tile << 10) | (enPassantIndex << 20) | (lastCaptureOrPawnAdv << 25);
    }

    public static int enPassant(int boardState) {
        return boardState & EN_PASSANT_MASK;
    }

    public static int castleRights(int boardState) {
        return (boardState & CASTLE_MASK) >> 6;
    }

    public static int targetTile(int boardState) {
        return (boardState & TILE_MASK) >> 10;
    }
    public static int enPassantIndex(int boardState) {
        return (boardState & EN_PASSANT_INDEX_MASK) >> 20;
    }

    public static int getLastCaptOrPawnAdv(int boardState) {
        return boardState >> 25;
    }
}
