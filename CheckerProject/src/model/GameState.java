package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameState implements Serializable {

    /** serialVersionUID bắt buộc để đảm bảo tương thích khi deserialize */
    private static final long serialVersionUID = 20260604_001L;

    /** Phiên bản format file save – dùng để validate khi load */
    public static final String CURRENT_VERSION = "1.0";

    // ─── FIELDS ──────────────────────────────────────────────────────────────

    /**
     * UC7.1 – Mã hóa bàn cờ 8x8 thành mảng int.
     * Dùng int thay vì Piece để tránh phụ thuộc class khi deserialize
     * và giảm kích thước file.
     * boardData[row][col] ∈ {0,1,2,3,4}
     */
    public int[][] boardData;

    /**
     * UC7.1 – Lưu lượt đi: true = lượt Trắng, false = lượt Đen.
     * Cần thiết để khi load lại (UC7.2) biết ai được đi tiếp.
     */
    public boolean whiteTurn;

    /**
     * UC7.3 – Danh sách lịch sử nước đi dạng chuỗi ký tự đã format.
     * Ví dụ: "1. W: (5,0)→(4,1) [ăn 1 quân]"
     * Được lưu cùng trạng thái để khi load lại vẫn hiển thị đúng lịch sử.
     */
    public List<String> moveHistory;

    /** Phiên bản format – để kiểm tra tương thích khi đọc file cũ */
    public String saveVersion;

    // ─── CONSTRUCTOR ─────────────────────────────────────────────────────────

    /**
     * UC7.1 – Constructor chính: chuyển đổi Board + trạng thái game → GameState.
     *
     * @param whiteTurn   true nếu đến lượt Trắng, false nếu lượt Đen
     * @param board       Bàn cờ hiện tại (đọc từng ô, mã hóa thành int[][])
     * @param moveHistory Danh sách string lịch sử nước đi (từ MoveHistoryManager)
     *
     * LUỒNG XỬ LÝ:
     *   1. Ghi nhận whiteTurn và version
     *   2. Duyệt Board 8×8, lấy từng Piece → mã hóa thành int
     *   3. Copy danh sách moveHistory để tránh mutation sau khi lưu
     *
     * ĐƯỢC GỌI BỞI: SaveLoadManager.saveGame()
     */
    public GameState(boolean whiteTurn, Board board, List<String> moveHistory) {
        this.whiteTurn   = whiteTurn;
        this.saveVersion = CURRENT_VERSION;

        // Mã hóa Board → int[8][8]
        this.boardData = new int[8][8];
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                this.boardData[r][c] = encodePiece(board.getPiece(r, c));
            }
        }

        // Deep copy danh sách lịch sử (tránh reference sharing)
        this.moveHistory = new ArrayList<>(moveHistory);
    }

    // ─── PUBLIC METHODS ───────────────────────────────────────────────────────

    /**
     * UC7.2 – Chuyển đổi ngược: GameState → Board object.
     * Dùng khi load game để khôi phục bàn cờ.
     *
     * @return Board mới với đúng vị trí và loại quân đã lưu
     *
     * LUỒNG XỬ LÝ:
     *   1. Tạo mảng Piece[8][8] rỗng
     *   2. Duyệt boardData, giải mã int → Piece
     *   3. Tạo Board mới từ mảng Piece đó
     *
     * ĐƯỢC GỌI BỞI: SaveLoadManager.loadGame() → GameController
     */
    public Board toBoard() {
        Piece[][] pieces = new Piece[8][8];
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                pieces[r][c] = decodePiece(boardData[r][c]);
            }
        }
        return new Board(pieces);
    }

    /**
     * UC7.2 – Kiểm tra phiên bản file có tương thích với phiên bản hiện tại không.
     * Dùng khi load để cảnh báo người dùng nếu file quá cũ.
     *
     * @return true nếu tương thích, false nếu không
     */
    public boolean isVersionCompatible() {
        return CURRENT_VERSION.equals(this.saveVersion);
    }

    // ─── PRIVATE HELPERS ─────────────────────────────────────────────────────

    /**
     * Mã hóa một ô Piece → int (0..4).
     * null → 0, White → 1, Black → 2, WhiteKing → 3, BlackKing → 4
     *
     * @param p Quân cờ (có thể null)
     * @return  Mã int tương ứng
     */
    private int encodePiece(Piece p) {
        if (p == null)          return 0;
        if (p.isWhite  && !p.isKing) return 1;
        if (!p.isWhite && !p.isKing) return 2;
        if (p.isWhite  &&  p.isKing) return 3;
        /* !p.isWhite &&  p.isKing */ return 4;
    }

    /**
     * Giải mã int → Piece object.
     * 0 → null, 1 → White, 2 → Black, 3 → WhiteKing, 4 → BlackKing
     *
     * @param code Mã int đã lưu
     * @return     Piece object tương ứng (null nếu code == 0)
     */
    private Piece decodePiece(int code) {
        switch (code) {
            case 1: return new Piece(true,  false);
            case 2: return new Piece(false, false);
            case 3: return new Piece(true,  true);
            case 4: return new Piece(false, true);
            default: return null; // ô trống
        }
    }

    // ─── toString ────────────────────────────────────────────────────────────

    /**
     * UC7.3 – Hiển thị thông tin GameState để debug / log.
     * @return Chuỗi tóm tắt: version, lượt, số nước đã đi
     */
    @Override
    public String toString() {
        return String.format("GameState[version=%s, turn=%s, moves=%d]",
                saveVersion,
                whiteTurn ? "WHITE" : "BLACK",
                moveHistory != null ? moveHistory.size() : 0);
    }
}