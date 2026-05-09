package model;

/**
 * Board 8x8 cho Checkers. ô sẫm nằm ở (r+c)%2==1 chứa piece.
 * Hỗ trợ copy/deepCopy để AI simulate.
 */
public class Board {
    private Piece[][] board = new Piece[8][8];

    public Board() {
        initialize();
    }

    public Board(Piece[][] data) {
        this.board = data;
    }

    public void initialize() {
        // clear
        for (int r=0;r<8;r++)
            for (int c=0;c<8;c++)
                board[r][c] = null;

        // Black (top) rows 0..2
        for (int r=0;r<3;r++) {
            for (int c=0;c<8;c++) {
                if ((r+c)%2==1) board[r][c] = new Piece(false);
            }
        }

        // White (bottom) rows 5..7
        for (int r=5;r<8;r++) {
            for (int c=0;c<8;c++) {
                if ((r+c)%2==1) board[r][c] = new Piece(true);
            }
        }
    }

    public Piece getPiece(int row, int col) {
        if (!inBounds(row,col)) return null;
        return board[row][col];
    }

    public void setPiece(int row, int col, Piece p) {
        if (!inBounds(row,col)) return;
        board[row][col] = p;
    }

    public void clearCell(int row, int col) {
        if (!inBounds(row,col)) return;
        board[row][col] = null;
    }

    public boolean inBounds(int r, int c) {
        return r>=0 && r<8 && c>=0 && c<8;
    }

    // deep copy toàn bộ board
    public Board copy() {
        Piece[][] data = new Piece[8][8];
        for (int r=0;r<8;r++) {
            for (int c=0;c<8;c++) {
                Piece p = board[r][c];
                data[r][c] = (p==null) ? null : p.copy();
            }
        }
        return new Board(data);
    }
    public int countWhitePieces() {
        int count = 0;

        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                Piece p = board[row][col];
                if (p != null && p.isWhite) {
                    count++;
                }
            }
        }

        return count;
    }


    // Optional: helper hiển thị (debug)
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int r=0;r<8;r++) {
            for (int c=0;c<8;c++) {
                Piece p = board[r][c];
                sb.append((p==null?".":p.toString()) + "\t");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

	public int countBlackPieces() {
		 int count = 0;

	        for (int row = 0; row < board.length; row++) {
	            for (int col = 0; col < board[row].length; col++) {
	                Piece p = board[row][col];
	                if (p != null && !p.isWhite) {
	                    count++;
	                }
	            }
	        }

	        return count;
	}
}
