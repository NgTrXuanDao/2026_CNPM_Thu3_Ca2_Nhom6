package model;
// use case: nước đi bình thường - đi chéo 1 ô trống
public class MoveNomarl extends Move {

    // use case: di chuyển quân cờ
    @Override
    protected void moveInPiece() {
        piece.setRowCol(destX, destY);
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
        System.out.print("start: " + this.startX);
        System.out.print(this.startY + " " + "end: ");
        System.out.print(this.destX);
        System.out.print(this.destY);

    }

    // lấy tọa độ điểm đến của nước đi
    public int getDestX() {
        return destX;
    }

    // lấy tọa độ điểm đến của nước đi
    public int getDestY() {
        return destY;
    }

    // use case: Kiểm tra nước đi hợp lệ
    @Override
    public int getType() {
        return 1;
    }

}