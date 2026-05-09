package model;

import java.util.ArrayList;
import java.util.List;

public class Board {
   private static Piece[] listAddressPiece = new Piece[24];

   // use case: khơi tạo ván chơi
   public Board() {
      this.initialize();
   }

   // use case: tạo bàn cờ 8x8, đặt quân cờ ban đầu
   public void initialize() {
      int rowBoard;
      int colBoard;
      int i=0;
      for (rowBoard = 0; rowBoard < 3; ++rowBoard) {
         for (colBoard = (rowBoard + 1) % 2; colBoard < 8; colBoard += 2) {
            listAddressPiece[i] = new Piece(true, rowBoard, colBoard);
            i++;
         }
      }

      for (rowBoard = 5; rowBoard < 8; ++rowBoard) {
         for (colBoard = (rowBoard + 1) % 2; colBoard < 8; colBoard += 2) {
            listAddressPiece[i] = new Piece(false, rowBoard, colBoard);
            i++;
         }
      }

   }

   // use case: Xác định người đi trước và chon quân
   public Piece getPiece(int row, int col) {
      for (Piece piece : listAddressPiece) {
         if (piece.isValid() && piece.getCol() == col && piece.getRow() == row)
            return piece;
      }
      return null;   
   }

   // use case: Hiển thị nước đi hợp lệ
   public Move[] getPieceMoveToDisplay(Piece pieceSelect) {
    Move[] arr = new Move[4];
    int rowS = pieceSelect.getRow();
    int colS = pieceSelect.getCol();

    // 4 hướng: trên-trái, trên-phải, dưới-phải, dưới-trái
    int[][] directions = {
        {-1, -1}, // trên-trái
        {-1,  1}, // trên-phải
        { 1,  1}, // dưới-phải
        { 1, -1}  // dưới-trái
    };

    for (int i = 0; i < directions.length; i++) {
        int dr = directions[i][0];
        int dc = directions[i][1];
        arr[i] = getMoveForDirection(pieceSelect, rowS, colS, dr, dc);
    }

    pieceSelect.setMove(arr);
    return arr;
}

/**
 * use case: Di chuyển quân cờ
 * use case: Kiểm tra nước đi hợp lệ
 * use case: Ăn quân 
 */
private Move getMoveForDirection(Piece piece, int rowS, int colS, int dr, int dc) {
    int row1 = rowS + dr;
    int col1 = colS + dc;

    // use case: Không đi sai luật hoặc vượt biên
    if (isOutOfBounds(row1, col1)) return null;

    Piece first = getPiece(row1, col1);

    // use case: Ô trống đi bình thường
    // use case: Nước đi bình thường - đi chéo 1 ô trống
    if (first == null) {
        Move mo = new MoveNomarl();
        mo.setMove(rowS, colS, row1, col1);
        return mo;
    }

    // use case: Nhảy qua quân đối phương
    int row2 = rowS + 2 * dr;
    int col2 = colS + 2 * dc;

    if (isOutOfBounds(row2, col2)) return null;


    Piece behind = getPiece(row2, col2);

    // use case: Bắt buộc ăn quân nếu có thể
    if (behind == null && first.isWhite() != piece.isWhite()) {
        Move mo = new MoveOFF();
        mo.setMove(rowS, colS, row2, col2);
        return mo;
    }
   
   // use case: Chặn nước đi thường nếu có thể ăn
    return null;
}

// use case: Không đi sai luật hoặc vượt biên
private boolean isOutOfBounds(int r, int c) {
    return (r < 0 || r > 7 || c < 0 || c > 7);
}

// use case: di chuyển quân cờ
public void setPiece(int row, int col, Piece pieceSelect) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'setPiece'");
}

}
