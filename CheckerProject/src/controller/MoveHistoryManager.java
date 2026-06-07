package controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.Board;
import model.Move;
import model.MoveRecord;
import model.Piece;

/**
 * UC7.3 – Xem lịch sử nước đi
 * Người thực hiện: Nguyễn Trần Xuân Đào
 * Ngày thực hiện: 04/06/2026
 * Mô tả: Quản lý toàn bộ lịch sử nước đi trong một ván cờ. Nhận thông báo mỗi khi có nước đi mới, tạo MoveRecord, lưu vào danh sách nội bộ, và cung cấp dữ liệu cho UI (HistoryPanel) và SaveLoadManager.
 */
public class MoveHistoryManager {

    // ─── FIELDS ──────────────────────────────────────────────────────────────

    /**
     * Danh sách tất cả MoveRecord theo thứ tự thực hiện.
     * Index 0 = nước đầu tiên của ván.
     */
    private final List<MoveRecord> records;

    /** Đếm tổng số nước đã đi (dùng làm số thứ tự cho MoveRecord tiếp theo) */
    private int moveCounter;

    // ─── CONSTRUCTOR ─────────────────────────────────────────────────────────

    /**
     * Khởi tạo MoveHistoryManager mới cho ván cờ mới.
     * Bộ đếm bắt đầu từ 1 (nước đầu tiên có số thứ tự = 1).
     */
    public MoveHistoryManager() {
        this.records     = new ArrayList<>();
        this.moveCounter = 1;
    }

    // ─── PUBLIC METHODS ───────────────────────────────────────────────────────

    /**
     * UC7.3 – Ghi nhận một nước đi mới vào lịch sử.
     * Phân tích Move để phát hiện: ăn quân, ăn liên tiếp, phong vua.
     * Tạo MoveRecord và thêm vào danh sách.
     *
     * @param isWhite      Bên thực hiện nước (true = Trắng, false = Đen)
     * @param move         Nước đi vừa thực hiện (chứa path + captures)
     * @param boardBefore  Bàn cờ TRƯỚC KHI áp dụng move (dùng để detect phong vua)
     *
     * LUỒNG XỬ LÝ:
     *   1. Kiểm tra phong vua: quân thường đến hàng cuối (row 0 nếu Trắng, row 7 nếu Đen)
     *   2. Gọi MoveRecord.of() để tạo record với notation đầy đủ
     *   3. Thêm vào danh sách records
     *   4. Tăng moveCounter
     *
     * ĐƯỢC GỌI BỞI: GameController.makeMove() (sau applyMove)
     * TRẢ VỀ: void (trạng thái nội bộ thay đổi)
     */
    public void recordMove(boolean isWhite, Move move, Board boardBefore) {
        boolean isPromotion = detectPromotion(isWhite, move, boardBefore);
        MoveRecord record   = MoveRecord.of(moveCounter, isWhite, move, isPromotion);
        records.add(record);
        moveCounter++;
    }

    /**
     * UC7.3 – Overload không cần boardBefore (dùng khi không detect phong vua).
     * Tiện dụng cho AI move hoặc các trường hợp đơn giản.
     *
     * @param isWhite Bên thực hiện
     * @param move    Nước đi
     */
    public void recordMove(boolean isWhite, Move move) {
        recordMove(isWhite, move, null);
    }

    /**
     * UC7.3 – Lấy danh sách toàn bộ MoveRecord (read-only).
     * HistoryPanel gọi hàm này để render danh sách nước đi.
     *
     * @return Unmodifiable list các MoveRecord, thứ tự từ nước 1 đến nước cuối
     *
     * ĐƯỢC GỌI BỞI: HistoryPanel.updateHistory()
     *               SaveLoadManager.saveGame() (để lấy lịch sử lưu file)
     */
    public List<MoveRecord> getRecords() {
        return Collections.unmodifiableList(records);
    }

    /**
     * UC7.3 – Lấy danh sách notation dạng String (dùng để lưu vào GameState).
     * Mỗi phần tử là kết quả MoveRecord.toString().
     *
     * @return List<String> các chuỗi notation, thứ tự từ nước 1 đến nước cuối
     *
     * ĐƯỢC GỌI BỞI: SaveLoadManager.saveGame() qua GameController.getHistoryNotations()
     */
    public List<String> getNotations() {
        List<String> list = new ArrayList<>(records.size());
        for (MoveRecord r : records) {
            list.add(r.toString());
        }
        return list;
    }

    /**
     * UC7.3 – Khôi phục lịch sử từ danh sách String (dùng khi load game).
     * Tạo các MoveRecord "placeholder" chỉ chứa notation, không có Move object.
     *
     * @param notations Danh sách string đã lưu trong GameState.moveHistory
     *
     * LUỒNG XỬ LÝ:
     *   1. Clear danh sách hiện tại
     *   2. Parse từng string → MoveRecord đơn giản (chỉ có notation)
     *   3. Cập nhật moveCounter = records.size() + 1
     *
     * ĐƯỢC GỌI BỞI: SaveLoadManager.loadGame() → GameController.restoreHistory()
     */
    public void restoreFromStrings(List<String> notations) {
        clear();
        for (String s : notations) {
            // Tạo MoveRecord tối giản chỉ để hiển thị
            // Format: "  N. W: ..." → parse ra moveNumber và notation
            boolean isWhite = s.contains("W:");
            MoveRecord r = new MoveRecord(moveCounter, isWhite, s.trim(),
                                          s.contains("[ăn"), s.contains("♛"), 0);
            records.add(r);
            moveCounter++;
        }
    }

    /**
     * UC7.1/UC7.3 – Xóa toàn bộ lịch sử và reset bộ đếm về 1.
     * Gọi khi bắt đầu ván mới hoặc Restart.
     *
     * ĐƯỢC GỌI BỞI: GameController.resetGame()
     */
    public void clear() {
        records.clear();
        moveCounter = 1;
    }

    /**
     * UC7.3 – Trả về số nước đã đi trong ván hiện tại.
     * @return Số nguyên >= 0
     */
    public int getMoveCount() {
        return records.size();
    }

    /**
     * UC7.3 – Lấy nước đi gần nhất (nước cuối cùng).
     * Dùng để hiển thị "Last move" trên UI.
     *
     * @return MoveRecord cuối cùng, hoặc null nếu chưa có nước nào
     */
    public MoveRecord getLastMove() {
        if (records.isEmpty()) return null;
        return records.get(records.size() - 1);
    }

    /**
     * UC7.3 – Lấy tất cả nước đi của một bên (Trắng hoặc Đen).
     * Dùng để thống kê cuối ván.
     *
     * @param forWhite true = lấy nước Trắng, false = lấy nước Đen
     * @return Danh sách MoveRecord của bên được chọn
     */
    public List<MoveRecord> getMovesForSide(boolean forWhite) {
        List<MoveRecord> result = new ArrayList<>();
        for (MoveRecord r : records) {
            if (r.isWhite == forWhite) result.add(r);
        }
        return result;
    }

    // ─── PRIVATE HELPERS ─────────────────────────────────────────────────────

    /**
     * Phát hiện xem nước đi này có phong vua hay không.
     * Quân Trắng phong vua khi đến row 0, quân Đen khi đến row 7.
     *
     * @param isWhite     Bên thực hiện
     * @param move        Nước đi vừa thực hiện
     * @param boardBefore Bàn cờ trước khi di chuyển (để kiểm tra quân có phải King trước đó không)
     * @return true nếu nước đi này tạo ra quân Vua mới
     */
    private boolean detectPromotion(boolean isWhite, Move move, Board boardBefore) {
        if (boardBefore == null || move.to() == null) return false;
        int toRow = move.to().y;
        int toCol = move.to().x;
        int fromRow = move.from() != null ? move.from().y : -1;
        int fromCol = move.from() != null ? move.from().x : -1;

        // Điều kiện phong vua: đến hàng cuối
        boolean reachesPromotionRow = isWhite ? (toRow == 0) : (toRow == 7);
        if (!reachesPromotionRow) return false;

        // Kiểm tra quân xuất phát chưa phải King trước nước đi
        if (fromRow >= 0 && fromCol >= 0) {
            Piece p = boardBefore.getPiece(fromRow, fromCol);
            return (p != null && !p.isKing); // Chỉ tính phong vua nếu trước đó là quân thường
        }
        return false;
    }
}