package model;

/**
 * Piece: đơn giản lưu màu và trạng thái king.
 */
public class Piece {
    public boolean isWhite;
    public boolean isKing;

    public Piece(boolean isWhite) {
        this.isWhite = isWhite;
        this.isKing = false;
    }

    public Piece(boolean isWhite, boolean isKing) {
        this.isWhite = isWhite;
        this.isKing = isKing;
    }

    public Piece copy() {
        return new Piece(this.isWhite, this.isKing);
    }

    @Override
    public String toString() {
        if (isWhite) return isKing ? "Wk" : "W";
        else return isKing ? "Bk" : "B";
    }
}
