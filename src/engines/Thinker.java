package engines;

import board.Board;

public class Thinker implements Engine{
    private final String name;
    private final String color;

    public Thinker(String color) {
        this.color = color;
        this.name = "thinker";
    }

    @Override
    public int[] determineMove(Board board) {
        return new int[0];
    }

    @Override
    public boolean isWhite() {
        return color.equals("w");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getResults() {
        return null;
    }
}
