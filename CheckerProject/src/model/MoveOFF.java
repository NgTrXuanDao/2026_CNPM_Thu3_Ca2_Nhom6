package model;
// use case: ăn quân
public class MoveOFF extends Move {
    // use case: nhảy qua quân đối phương
    @Override
    protected void moveInPiece() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'moveInPiece'");
    }

    public int getDestX() {
        return destX;
    }

    public int getDestY() {
        return destY;
    }

    @Override
    public int getType() {
        return 2;
    }

    // use case: xác định tọa độ nước đi (bắt đầu và kết thúc)
    @Override
    protected void setMove(int startX, int startY, int destX, int destY) {
        this.startX = startX;
        this.startY = startY;
        this.destX = destX;
        this.destY = destY;
    }

    // use case: xem lịch sử nước đi
    @Override
    public void printTermianl() {
        System.out.println("start: " + startX + " " + startY + " end: " + destX + " " + destY);

    }

}
