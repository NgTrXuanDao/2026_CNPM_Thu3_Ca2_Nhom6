
package model;

public class Piece {
	private boolean isValid;
	private boolean isWhite;
	private boolean isKing;
	private int row;
	private int col;
	private Move[] moves; // 0 góc 10h chiều đông hồ

	// use case: tạo quân cờ ban đầu
	public Piece(boolean isWhite, int row, int col) {
		this.isWhite = isWhite;
		this.row = row;
		this.col = col;
		this.isKing = false;
		this.isValid = true;
	}

	// Màu quân cờ
	public boolean isWhite() {
		return this.isWhite;
	}

	// Phải vua không?
	public boolean isKing() {
		return this.isKing;
	}

	// use case: đánh dấu quân bị ăn
	public boolean isValid() {
		return this.isValid;
	}

	// use case: phong cấp - vua
	public void makeKing() {
		this.isKing = true;
	}

	public int getRow() {
		return this.row;
	}

	public int getCol() {
		return this.col;
	}

	public void setRowCol(int row, int col) {
		this.row = row;
		this.col = col;
	}

	public Move[] getMove() {
		return moves;
	}

	public void calculateMove() {

	}

	public void setMove(Move[] arr) {
		this.moves = arr;
	}

	public void action(Move m) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'action'");
	}

}
