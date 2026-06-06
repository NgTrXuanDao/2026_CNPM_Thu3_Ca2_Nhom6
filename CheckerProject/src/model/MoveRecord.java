package model;

import java.awt.Point;
import java.io.Serializable;

public class MoveRecord implements Serializable {

    private static final long serialVersionUID = 20260604_002L;

    // ─── FIELDS (all final – immutable) ──────────────────────────────────────

    /** Số thứ tự nước đi trong ván (bắt đầu từ 1) */
    public final int moveNumber;

    /** Bên thực hiện nước đi: true = Trắng, false = Đen */
    public final boolean isWhite;

    /**
     * Ký hiệu notation theo định dạng:
     *   "W: (r,c)→(r,c)"           — nước đi bình thường
     *   "W: (r,c)→(r,c) [ăn 1Q]"  — ăn 1 quân
     *   "W: (r,c)→(r,c) [ăn 2Q★]" — ăn liên tiếp (UC1.4)
     *   Có thêm " ♛" nếu phong vua trong nước đó
     */
    public final String notation;

    /** true nếu nước này ăn ít nhất 1 quân địch */
    public final boolean isCapture;

    /** true nếu nước này phong vua (quân đến hàng cuối) */
    public final boolean isPromotion;

    /** Số quân bị ăn trong nước này (0 nếu không ăn) */
    public final int captureCount;

    // ─── CONSTRUCTOR ─────────────────────────────────────────────────────────

    /**
     * UC7.3 – Constructor đầy đủ.
     * Nên tạo qua MoveRecord.of() thay vì gọi trực tiếp.
     *
     * @param moveNumber   Số thứ tự nước (1, 2, 3, ...)
     * @param isWhite      true = Trắng thực hiện
     * @param notation     Chuỗi mô tả nước đi (đã format sẵn)
     * @param isCapture    Nước đi có ăn quân không
     * @param isPromotion  Nước đi có phong vua không
     * @param captureCount Số quân bị ăn
     */
    public MoveRecord(int moveNumber, boolean isWhite, String notation,
                      boolean isCapture, boolean isPromotion, int captureCount) {
        this.moveNumber   = moveNumber;
        this.isWhite      = isWhite;
        this.notation     = notation;
        this.isCapture    = isCapture;
        this.isPromotion  = isPromotion;
        this.captureCount = captureCount;
    }

    // ─── FACTORY METHOD ───────────────────────────────────────────────────────

    /**
     * UC7.3 – Factory: tạo MoveRecord từ một Move object.
     * Tự động format notation dựa trên thông tin Move.
     *
     * @param moveNumber  Số thứ tự nước đi
     * @param isWhite     Bên thực hiện nước
     * @param move        Nước đi đã thực hiện (chứa path + captures)
     * @param isPromotion true nếu nước này phong vua quân thường thành Vua
     * @return            MoveRecord mới đã format sẵn notation
     *
     * LUỒNG XỬ LÝ:
     *   1. Lấy tọa độ from/to từ move.from() / move.to()
     *   2. Xây dựng chuỗi notation (side + tọa độ + số quân ăn + phong vua)
     *   3. Tạo MoveRecord với thông tin đầy đủ
     *
     * ĐƯỢC GỌI BỞI: MoveHistoryManager.recordMove()
     */
    public static MoveRecord of(int moveNumber, boolean isWhite, Move move, boolean isPromotion) {
        String side = isWhite ? "W" : "B";

        // Tọa độ xuất phát (Point.x = col, Point.y = row)
        Point from = move.from();
        Point to   = move.to();
        int fromRow = (from != null) ? from.y : -1;
        int fromCol = (from != null) ? from.x : -1;
        int toRow   = (to   != null) ? to.y   : -1;
        int toCol   = (to   != null) ? to.x   : -1;

        // Ký hiệu cờ (dùng ký tự Unicode cho đẹp trong JList)
        // Hàng hiển thị: đảo từ row 7→row 1 cho thân thiện
        // (row 0 = hàng trên cùng trong mảng → hiển thị hàng 8)
        int dispFromRow = 8 - fromRow;
        int dispToRow   = 8 - toRow;
        char fromColChar = (char)('a' + fromCol);
        char toColChar   = (char)('a' + toCol);

        StringBuilder sb = new StringBuilder();
        sb.append(side).append(": ");
        sb.append(fromColChar).append(dispFromRow);
        sb.append("→");
        sb.append(toColChar).append(dispToRow);

        int capCount = move.captures.size();
        if (capCount > 0) {
            sb.append(" [ăn ").append(capCount).append("Q");
            if (capCount >= 2) sb.append("★"); // ăn liên tiếp nổi bật
            sb.append("]");
        }
        if (isPromotion) {
            sb.append(" ♛"); // phong vua
        }

        String notation = sb.toString();
        return new MoveRecord(moveNumber, isWhite, notation,
                              move.isCapture(), isPromotion, capCount);
    }

    // ─── DISPLAY HELPERS ─────────────────────────────────────────────────────

    /**
     * UC7.3 – Chuỗi hiển thị đầy đủ trong bảng lịch sử.
     * Ví dụ: "  5. W: c6→b5 [ăn 1Q]"
     * @return Chuỗi có padding số thứ tự để căn đều
     */
    @Override
    public String toString() {
        return String.format("%3d. %s", moveNumber, notation);
    }

    /**
     * UC7.3 – Chuỗi ngắn để lưu vào file save (không có padding).
     * @return Chuỗi đơn giản dùng trong GameState.moveHistory
     */
    public String toSaveString() {
        return moveNumber + "|" + (isWhite ? "W" : "B") + "|" + notation
               + "|" + captureCount + "|" + isPromotion;
    }
}