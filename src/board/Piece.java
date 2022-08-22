package board;

import java.util.List;

public class Piece {
    private final String color;
    private String type;
    private boolean hasMoved;
    private boolean enPassantPossible;

    public Piece(String color, String type) {
        this.color = color;
        this.type = type;
        this.hasMoved = false;
        this.enPassantPossible = false;
    }

    public String getColor() {
        return color;
    }

    public boolean isColor(String color) {
        return this.color.equals(color);
    }

    public String getType() {
        return type;
    }

    public boolean isType(String type) {
        return this.type.equals(type);
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public boolean isEnPassantPossible() {
        return enPassantPossible;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    public void setEnPassantPossible(boolean enPassantPossible) {
        this.enPassantPossible = enPassantPossible;
    }
}
