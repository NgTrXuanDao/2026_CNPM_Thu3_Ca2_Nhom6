package model;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * MoveTest - Kiểm thử lớp Move
 * Người thực hiện: Đoàn Ngọc Ánh
 * Ngày: 07/06/2026
 * 
 * Liên quan đến các UC:
 * - UC1.2 - Di chuyển quân cờ
 * - UC1.10 - Chọn quân & hiển thị nước đi hợp lệ
 * - UC1.12 - Nhảy qua quân đối phương
 * - UC1.13 - Xóa quân bị ăn
 * - UC1.14 - Kiểm tra chuỗi ăn tiếp theo
 */
class MoveTest {

    // ========================================================================
    // UC1.2 - Di chuyển quân cờ
    // ========================================================================

    @Test
    @DisplayName("UC1.2.1 - Tạo Move đơn giản (2 ô)")
    void testSimpleMove() {
        Move move = new Move(5, 0, 4, 1);
        assertEquals(2, move.path.size(), "Path đơn giản phải có 2 điểm");
        
        Point from = move.from();
        assertEquals(0, from.x, "from.x phải là cột");
        assertEquals(5, from.y, "from.y phải là hàng");

        Point to = move.to();
        assertEquals(1, to.x, "to.x phải là cột");
        assertEquals(4, to.y, "to.y phải là hàng");
    }

    @Test
    @DisplayName("UC1.2.2 - from() trả về ô xuất phát")
    void testFrom() {
        Move move = new Move(5, 0, 4, 1);
        Point from = move.from();
        assertNotNull(from);
        assertEquals(0, from.x);
        assertEquals(5, from.y);
    }

    @Test
    @DisplayName("UC1.2.3 - to() trả về ô đích")
    void testTo() {
        Move move = new Move(5, 0, 4, 1);
        Point to = move.to();
        assertNotNull(to);
        assertEquals(1, to.x);
        assertEquals(4, to.y);
    }

    @Test
    @DisplayName("UC1.2.4 - from() và to() trả về null cho Move rỗng")
    void testNullFromTo() {
        Move empty = new Move();
        assertNull(empty.from(), "Move rỗng phải trả về null");
        assertNull(empty.to(), "Move rỗng phải trả về null");
    }

    @Test
    @DisplayName("UC1.2.5 - getFromRow, getFromCol, getToRow, getToCol hoạt động đúng")
    void testGetters() {
        Move move = new Move(5, 0, 4, 1);
        assertEquals(5, move.getFromRow());
        assertEquals(0, move.getFromCol());
        assertEquals(4, move.getToRow());
        assertEquals(1, move.getToCol());
    }

    @Test
    @DisplayName("UC1.2.6 - Move mặc định là path rỗng, không captures")
    void testEmptyMove() {
        Move empty = new Move();
        assertTrue(empty.path.isEmpty(), "Move rỗng path phải rỗng");
        assertTrue(empty.captures.isEmpty(), "Move rỗng captures phải rỗng");
        assertFalse(empty.isCapture(), "Move rỗng không phải capture");
    }

    // ========================================================================
    // UC1.12 - Nhảy qua quân đối phương
    // ========================================================================

    @Test
    @DisplayName("UC1.12.1 - addCapture ghi nhận quân bị ăn")
    void testAddCapture() {
        Move move = new Move(5, 0, 3, 2);
        move.addCapture(4, 1);
        
        assertEquals(1, move.captures.size(), "Phải có 1 quân bị ăn");
        assertTrue(move.isCapture(), "Phải là capture move");
        
        Point cap = move.captures.get(0);
        assertEquals(1, cap.x, "capture.x phải là cột");
        assertEquals(4, cap.y, "capture.y phải là hàng");
    }

    @Test
    @DisplayName("UC1.12.2 - isCapture true khi có captures")
    void testIsCaptureTrue() {
        Move move = new Move(5, 0, 3, 2);
        move.addCapture(4, 1);
        assertTrue(move.isCapture());
    }

    @Test
    @DisplayName("UC1.12.3 - isCapture false khi không có captures")
    void testIsCaptureFalse() {
        Move move = new Move(5, 0, 4, 1);
        assertFalse(move.isCapture());
    }

    @Test
    @DisplayName("UC1.12.4 - Move khởi tạo với path có captures rỗng")
    void testMoveWithoutCapture() {
        Move move = new Move(5, 0, 4, 1);
        assertFalse(move.isCapture());
        assertTrue(move.captures.isEmpty());
    }

    // ========================================================================
    // UC1.14 - Kiểm tra chuỗi ăn tiếp theo
    // ========================================================================

    @Test
    @DisplayName("UC1.14.1 - addStep thêm bước trung gian trong chuỗi ăn")
    void testAddStep() {
        Move move = new Move();
        move.addStep(5, 0); // start
        move.addStep(3, 2); // after first capture
        move.addStep(1, 4); // after second capture
        
        assertEquals(3, move.path.size(), "Phải có 3 bước trong path");
        
        assertEquals(0, move.path.get(0).x, "Bước 1: cột");
        assertEquals(5, move.path.get(0).y, "Bước 1: hàng");
        assertEquals(4, move.path.get(2).x, "Bước 3: cột");
        assertEquals(1, move.path.get(2).y, "Bước 3: hàng");
    }

    @Test
    @DisplayName("UC1.14.2 - Chuỗi ăn 2 quân (path 3 điểm + 2 captures)")
    void testChainCapture() {
        Move move = new Move();
        move.addStep(5, 0);  // vị trí bắt đầu
        move.addStep(3, 2);  // sau khi ăn quân thứ nhất
        move.addStep(1, 4);  // sau khi ăn quân thứ hai
        
        move.addCapture(4, 1); // quân bị ăn thứ nhất
        move.addCapture(2, 3); // quân bị ăn thứ hai
        
        assertEquals(3, move.path.size(), "Path phải có 3 điểm");
        assertEquals(2, move.captures.size(), "Phải có 2 quân bị ăn");
        assertTrue(move.isCapture(), "Phải là capture move");
    }

    // ========================================================================
    // UC1.10 - Chọn quân & hiển thị nước đi hợp lệ (liên quan)
    // ========================================================================

    @Test
    @DisplayName("UC1.10.1 - Move từ (fromRow, fromCol) đến (toRow, toCol)")
    void testMoveFromToCoordinates() {
        Move move = new Move(2, 3, 3, 4);
        assertEquals(2, move.getFromRow());
        assertEquals(3, move.getFromCol());
        assertEquals(3, move.getToRow());
        assertEquals(4, move.getToCol());
    }

    // ========================================================================
    // UC1.13 - Xóa quân bị ăn (liên quan)
    // ========================================================================

    @Test
    @DisplayName("UC1.13.1 - captures lưu danh sách ô cần xóa")
    void testCapturesForClear() {
        Move move = new Move(5, 0, 3, 2);
        move.addCapture(4, 1);
        
        // captures chứa tọa độ quân bị ăn
        assertEquals(1, move.captures.size());
        Point cap = move.captures.get(0);
        assertEquals(4, cap.y, "capture.y là hàng của quân bị ăn");
        assertEquals(1, cap.x, "capture.x là cột của quân bị ăn");
    }

    @Test
    @DisplayName("UC1.13.2 - ToString hiển thị thông tin Move")
    void testToString() {
        Move move = new Move(5, 0, 4, 1);
        String str = move.toString();
        assertNotNull(str);
        assertTrue(str.contains("Move:"));
    }

    @Test
    @DisplayName("UC1.12.5 - ToString hiển thị captures")
    void testToStringWithCapture() {
        Move move = new Move(5, 0, 3, 2);
        move.addCapture(4, 1);
        String str = move.toString();
        assertTrue(str.contains("captures:"), 
                "toString phải hiển thị captures");
    }
}
