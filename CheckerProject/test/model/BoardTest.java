package model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * BoardTest - Kiểm thử lớp Board
 * Người thực hiện: Đoàn Ngọc Ánh
 * Ngày: 07/06/2026
 * 
 * Liên quan đến các UC:
 * - UC1.1 - Khởi tạo ván chơi
 * - UC1.7 - Tạo bàn cờ 8x8
 * - UC1.8 - Đặt quân ban đầu
 * - UC1.13 - Xóa quân bị ăn
 */
class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    // ========================================================================
    // UC1.7 - Tạo bàn cờ 8x8
    // ========================================================================

    @Test
    @DisplayName("UC1.7.1 - Board có kích thước 8x8 - tất cả ô đều trong biên")
    void testBoardDimensions() {
        // Kiểm tra 8 hàng, mỗi hàng 8 cột - tất cả đều inBounds
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                assertTrue(board.inBounds(r, c),
                        "Ô (" + r + "," + c + ") phải trong biên");
            }
        }
        // Các ô ngoài biên không hợp lệ
        assertFalse(board.inBounds(-1, 0));
        assertFalse(board.inBounds(8, 0));
    }

    @Test
    @DisplayName("UC1.7.2 - inBounds kiểm tra biên đúng")
    void testInBounds() {
        // Các ô hợp lệ
        assertTrue(board.inBounds(0, 0), "(0,0) phải hợp lệ");
        assertTrue(board.inBounds(7, 7), "(7,7) phải hợp lệ");
        assertTrue(board.inBounds(0, 7), "(0,7) phải hợp lệ");
        assertTrue(board.inBounds(7, 0), "(7,0) phải hợp lệ");
        assertTrue(board.inBounds(3, 4), "(3,4) phải hợp lệ");

        // Các ô không hợp lệ
        assertFalse(board.inBounds(-1, 0), "(-1,0) không hợp lệ");
        assertFalse(board.inBounds(0, -1), "(0,-1) không hợp lệ");
        assertFalse(board.inBounds(8, 0), "(8,0) không hợp lệ");
        assertFalse(board.inBounds(0, 8), "(0,8) không hợp lệ");
        assertFalse(board.inBounds(-1, -1), "(-1,-1) không hợp lệ");
        assertFalse(board.inBounds(8, 8), "(8,8) không hợp lệ");
    }

    // ========================================================================
    // UC1.8 - Đặt quân ban đầu
    // ========================================================================

    @Test
    @DisplayName("UC1.8.1 + UC1.8.2 - Có đúng 12 quân trắng và 12 quân đen")
    void testInitialPieceCount() {
        assertEquals(12, board.countWhitePieces(), "Phải có 12 quân trắng");
        assertEquals(12, board.countBlackPieces(), "Phải có 12 quân đen");
        assertEquals(24, board.countWhitePieces() + board.countBlackPieces(), "Tổng cộng 24 quân");
    }

    @Test
    @DisplayName("UC1.8.3 - Nếu có quân thì phải ở ô đen (r+c)%2==1")
    void testPiecesOnlyOnDarkSquares() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPiece(r, c);
                // Nếu có quân, nó phải ở ô đen (chỉ số hàng+cột lẻ)
                if (p != null) {
                    assertEquals(1, (r + c) % 2,
                            "Quân tại (" + r + "," + c + ") phải ở ô đen");
                }
                // Ô trắng (chẵn) không kiểm tra vì rows 3-4 có thể null ở cả 2 loại ô
            }
        }
    }

    @Test
    @DisplayName("UC1.8.4 - Quân trắng ở đúng vị trí (hàng 5-7)")
    void testWhitePiecesPositions() {
        for (int r = 5; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPiece(r, c);
                if ((r + c) % 2 == 1) {
                    assertNotNull(p, "Ô (" + r + "," + c + ") phải có quân");
                    assertTrue(p.isWhite, "Quân tại (" + r + "," + c + ") phải là trắng");
                    assertFalse(p.isKing, "Quân ban đầu không được là vua");
                }
            }
        }
    }

    @Test
    @DisplayName("UC1.8.5 - Quân đen ở đúng vị trí (hàng 0-2)")
    void testBlackPiecesPositions() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPiece(r, c);
                if ((r + c) % 2 == 1) {
                    assertNotNull(p, "Ô (" + r + "," + c + ") phải có quân");
                    assertFalse(p.isWhite, "Quân tại (" + r + "," + c + ") phải là đen");
                    assertFalse(p.isKing, "Quân ban đầu không được là vua");
                }
            }
        }
    }

    @Test
    @DisplayName("UC1.8.6 - Hàng giữa (3-4) không có quân")
    void testMiddleRowsEmpty() {
        for (int r = 3; r < 5; r++) {
            for (int c = 0; c < 8; c++) {
                assertNull(board.getPiece(r, c),
                        "Hàng " + r + " phải không có quân");
            }
        }
    }

    @Test
    @DisplayName("UC1.8.7 - getPiece trả về null cho ô ngoài biên")
    void testGetPieceOutOfBounds() {
        assertNull(board.getPiece(-1, 0), "getPiece(-1,0) phải null");
        assertNull(board.getPiece(0, -1), "getPiece(0,-1) phải null");
        assertNull(board.getPiece(8, 0), "getPiece(8,0) phải null");
        assertNull(board.getPiece(0, 8), "getPiece(0,8) phải null");
    }

    // ========================================================================
    // UC1.13 - Xóa quân bị ăn
    // ========================================================================

    @Test
    @DisplayName("UC1.13.1 - setPiece đặt quân vào ô đích")
    void testSetPiece() {
        Piece p = new Piece(true);
        board.setPiece(3, 4, p);
        assertSame(p, board.getPiece(3, 4), "setPiece phải đặt quân đúng vị trí");
    }

    @Test
    @DisplayName("UC1.13.2 - clearCell xóa quân khỏi ô")
    void testClearCell() {
        // Ô (5,0) có quân trắng ban đầu
        assertNotNull(board.getPiece(5, 0), "Ô (5,0) phải có quân trước khi xóa");
        board.clearCell(5, 0);
        assertNull(board.getPiece(5, 0), "Ô (5,0) phải null sau khi xóa");
    }

    @Test
    @DisplayName("UC1.13.3 - clearCell không throw với ô ngoài biên")
    void testClearCellOutOfBounds() {
        // Không throw exception
        assertDoesNotThrow(() -> board.clearCell(-1, 0));
        assertDoesNotThrow(() -> board.clearCell(8, 8));
    }

    @Test
    @DisplayName("UC1.13.4 - setPiece không throw với ô ngoài biên")
    void testSetPieceOutOfBounds() {
        Piece p = new Piece(true);
        assertDoesNotThrow(() -> board.setPiece(-1, 0, p));
        assertDoesNotThrow(() -> board.setPiece(8, 8, p));
    }

    // ========================================================================
    // UC1.1 - Khởi tạo ván chơi (liên quan)
    // ========================================================================

    @Test
    @DisplayName("UC1.1.1 - Constructor Board() gọi initialize() tự động")
    void testBoardConstructorCallsInitialize() {
        // Board() đã gọi initialize() trong setUp
        assertEquals(12, board.countWhitePieces(), "Constructor phải gọi initialize()");
        assertEquals(12, board.countBlackPieces(), "Constructor phải gọi initialize()");
    }

    @Test
    @DisplayName("UC1.1.2 - Board copy hoạt động đúng - deep copy")
    void testBoardCopy() {
        Board copy = board.copy();
        
        // Copy có cùng số quân
        assertEquals(board.countWhitePieces(), copy.countWhitePieces());
        assertEquals(board.countBlackPieces(), copy.countBlackPieces());

        // Copy là bản sao độc lập (deep copy)
        copy.clearCell(5, 0);
        assertNotNull(board.getPiece(5, 0), "Board gốc không bị ảnh hưởng khi xóa trên copy");
        assertNull(copy.getPiece(5, 0), "Copy phải bị xóa");
    }

    @Test
    @DisplayName("UC1.8.8 - initialize() reset bàn cờ về trạng thái ban đầu")
    void testInitializeResetsBoard() {
        // Sửa đổi board
        board.clearCell(5, 0);
        assertEquals(11, board.countWhitePieces());
        
        // Reset
        board.initialize();
        
        // Kiểm tra lại trạng thái ban đầu
        assertEquals(12, board.countWhitePieces());
        assertEquals(12, board.countBlackPieces());
        assertNotNull(board.getPiece(5, 0), "Ô (5,0) phải có quân sau khi reset");
    }

    @Test
    @DisplayName("UC1.7.3 - toString trả về biểu diễn chuỗi bàn cờ")
    void testToString() {
        String str = board.toString();
        assertNotNull(str);
        assertTrue(str.contains("W") || str.contains("B"), 
                "toString phải chứa ký hiệu quân");
    }
}
