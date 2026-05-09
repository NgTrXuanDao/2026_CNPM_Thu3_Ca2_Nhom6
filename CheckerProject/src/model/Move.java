package model;

public abstract class Move {
    protected Piece piece;
    protected int startX, startY;
    protected int destX, destY;

    // use case: di chuyển quân cờ
    protected abstract void moveInPiece();

    // use case: xác định tọa độ nước đi (bắt đầu và kết thúc)
    public abstract int getDestX();
    public abstract int getDestY();
    public abstract int getType();

    protected abstract void setMove(int startX, int startY, int destX, int destY);

    // use case: xem lịch sử nước đi
    public abstract void printTermianl();

}
