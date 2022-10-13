package board;

import utils.Utils;

public class Move {
    private int startX;
    private int startY;
    private int endX;
    private int endY;

    public Move(int startX, int startY, int endX, int endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }

    public Move() {

    }

    public int startX() {
        return startX;
    }

    public int startY() {
        return startY;
    }

    public int endX() {
        return endX;
    }

    public int endY() {
        return endY;
    }

    public String toString() {
        return Utils.getChessCoordinates(startX, startY) + Utils.getChessCoordinates(endX, endY);
    }
}
