package model;

// UC1.1 - Khởi tạo ván chơi - Đoàn Ngọc Ánh
// UC1.7 - Tạo bàn cờ 8x8
// UC1.8 - Đặt quân ban đầu
public class Board {
    private Piece[][] board = new Piece[8][8];

    // UC1.1.1 - Hệ thống khởi tạo Board, gọi initialize() để tạo bàn cờ 8x8 (gọi UC1.7)
    public Board() {
        initialize();
    }

    public Board(Piece[][] data) {
        this.board = data;
    }

    // UC1.7.1 - GameController gọi hàm khởi tạo Board, tạo đối tượng Board mới qua new Board()
    // UC1.7.2 - Constructor Board() gọi initialize(); vòng lặp r=0..7, c=0..7 thiết lập board[r][c] = null
    // UC1.8.1 - initialize() tiến hành vòng lặp r=0..2, c=0..7: tạo Piece(false) - quân đen
    // UC1.8.2 - Gán quân đen vào board[r][c] tương ứng
    // UC1.8.3 - initialize() tiến hành vòng lặp r=5..7, c=0..7: tạo Piece(true) - quân trắng
    // UC1.8.4 - Gán quân trắng vào board[r][c] tương ứng
    // UC1.8.5 - Hoàn tất: 24 quân được đặt đúng vị trí
    public void initialize() {
        // UC1.7.2: Vòng lặp khởi tạo toàn bộ 64 ô về null
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                board[r][c] = null;

        // UC1.8.1: Đặt quân đen (false) ở 3 hàng trên, ô lẻ (r+c)%2==1
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 8; c++) {
                if ((r + c) % 2 == 1) board[r][c] = new Piece(false);
            }
        }

        // UC1.8.3: Đặt quân trắng (true) ở 3 hàng dưới, ô lẻ (r+c)%2==1
        for (int r = 5; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if ((r + c) % 2 == 1) board[r][c] = new Piece(true);
            }
        }
    }

    // UC1.10.2 - Hỗ trợ: lấy quân tại ô (gameplay selection)
    // UC5.1 - Kiểm tra nước đi hợp lệ (hỗ trợ - lấy quân tại ô)
    public Piece getPiece(int row, int col) {
        if (!inBounds(row, col)) return null;
        return board[row][col];
    }

    // UC1.13.1 - Hỗ trợ: đặt quân vào ô đích sau khi di chuyển hoặc ăn quân
    // UC1.2 - Di chuyển quân cờ (hỗ trợ - đặt quân vào ô mới)
    // UC1.5 - Phong cấp vua (hỗ trợ - cập nhật ô sau phong cấp)
    public void setPiece(int row, int col, Piece p) {
        if (!inBounds(row, col)) return;
        board[row][col] = p;
    }

    // UC1.13.2 - Hỗ trợ: xóa quân khỏi ô nguồn và các ô bị ăn (captures)
    // UC1.13 - Xóa quân bị ăn
    // UC1.3 - Ăn quân (hỗ trợ - xóa ô sau khi ăn)
    public void clearCell(int row, int col) {
        if (!inBounds(row, col)) return;
        board[row][col] = null;
    }

    // UC5.3 - Không đi vào ô đã có quân (hỗ trợ kiểm tra biên)
    // UC5.1 - Kiểm tra nước đi hợp lệ (hỗ trợ kiểm tra biên bàn cờ)
    public boolean inBounds(int r, int c) {
        return r >= 0 && r < 8 && c >= 0 && c < 8;
    }

    // UC6.1 - Tính toán nước đi (AI cần bản sao bàn cờ để duyệt cây)
    // UC6.3 - Minimax – Medium (cần deep copy để không ảnh hưởng bàn gốc)
    // UC6.4 - Alpha-Beta Pruning – Hard (tương tự Minimax)
    public Board copy() {
        Piece[][] data = new Piece[8][8];
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                data[r][c] = (p == null) ? null : p.copy();
            }
        }
        return new Board(data);
    }

    // UC6.5 - Đếm số quân & vua (đếm quân trắng còn lại)
    // UC1.17 - Hết quân → thua (dùng để kiểm tra điều kiện thua)
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

    // UC6.2 - Đánh giá bàn cờ (hỗ trợ - hiển thị trạng thái để debug/log)
    // UC7.3 - Xem lịch sử nước đi (hỗ trợ in trạng thái bàn cờ)
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                sb.append((p == null ? "." : p.toString()) + "\t");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // UC6.5 - Đếm số quân & vua (đếm quân đen còn lại)
    // UC1.17 - Hết quân → thua (dùng để kiểm tra điều kiện thua của đen)
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