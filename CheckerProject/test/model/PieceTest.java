package model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * PieceTest - Kiểm thử lớp Piece
 * Người thực hiện: Đoàn Ngọc Ánh
 * Ngày: 07/06/2026
 * 
 * Liên quan đến các UC:
 * - UC1.8 - Đặt quân ban đầu
 * - UC1.9 - Xác định người đi trước
 * - UC1.11 - Đi chéo 1 ô (không lùi trừ vua)
 */
class PieceTest {

    // ========================================================================
    // UC1.8 - Đặt quân ban đầu
    // ========================================================================

    @Test
    @DisplayName("UC1.8.1 - Tạo quân trắng (isWhite = true)")
    void testCreateWhitePiece() {
        Piece p = new Piece(true);
        assertTrue(p.isWhite, "Quân trắng phải có isWhite = true");
        assertFalse(p.isKing, "Quân mới tạo không được là vua");
    }

    @Test
    @DisplayName("UC1.8.2 - Tạo quân đen (isWhite = false)")
    void testCreateBlackPiece() {
        Piece p = new Piece(false);
        assertFalse(p.isWhite, "Quân đen phải có isWhite = false");
        assertFalse(p.isKing, "Quân mới tạo không được là vua");
    }

    @Test
    @DisplayName("UC1.8.3 - Tạo quân vua (isKing = true)")
    void testCreateKingPiece() {
        Piece whiteKing = new Piece(true, true);
        assertTrue(whiteKing.isWhite);
        assertTrue(whiteKing.isKing);

        Piece blackKing = new Piece(false, true);
        assertFalse(blackKing.isWhite);
        assertTrue(blackKing.isKing);
    }

    @Test
    @DisplayName("UC1.8.4 - copy() tạo bản sao deep copy")
    void testCopy() {
        Piece original = new Piece(true, true);
        Piece copy = original.copy();

        // Kiểm tra giá trị
        assertEquals(original.isWhite, copy.isWhite);
        assertEquals(original.isKing, copy.isKing);

        // Kiểm tra độc lập (deep copy)
        copy.isKing = false;
        assertTrue(original.isKing, "Original không bị ảnh hưởng khi thay đổi copy");
    }

    @Test
    @DisplayName("UC1.8.5 - copy() tạo bản sao từ quân thường")
    void testCopyRegularPiece() {
        Piece original = new Piece(false);
        Piece copy = original.copy();

        assertFalse(copy.isWhite);
        assertFalse(copy.isKing);
    }

    // ========================================================================
    // UC1.9 - Xác định người đi trước (liên quan)
    // ========================================================================

    @Test
    @DisplayName("UC1.9.1 - isWhite phân biệt quân trắng/đen để xác định lượt")
    void testIsWhiteForTurn() {
        Piece white = new Piece(true);
        Piece black = new Piece(false);

        assertTrue(white.isWhite, "Quân trắng dùng để xác định lượt trắng");
        assertFalse(black.isWhite, "Quân đen dùng để xác định lượt đen");
    }

    // ========================================================================
    // UC1.11 - Đi chéo 1 ô (không lùi trừ vua)
    // ========================================================================

    @Test
    @DisplayName("UC1.11.1 - Quân thường không phải vua (isKing = false)")
    void testRegularPieceNotKing() {
        Piece p = new Piece(true);
        assertFalse(p.isKing, "Quân thường không phải vua");
    }

    @Test
    @DisplayName("UC1.11.2 - Vua (isKing = true) có thể đi 4 hướng")
    void testKingPiece() {
        Piece king = new Piece(true, true);
        assertTrue(king.isKing, "Vua phải có isKing = true");
    }

    @Test
    @DisplayName("UC1.11.3 - toString hiển thị đúng ký hiệu quân")
    void testToString() {
        Piece white = new Piece(true);
        Piece black = new Piece(false);
        Piece whiteKing = new Piece(true, true);
        Piece blackKing = new Piece(false, true);

        assertEquals("W", white.toString(), "Quân trắng thường hiển thị 'W'");
        assertEquals("B", black.toString(), "Quân đen thường hiển thị 'B'");
        assertEquals("Wk", whiteKing.toString(), "Vua trắng hiển thị 'Wk'");
        assertEquals("Bk", blackKing.toString(), "Vua đen hiển thị 'Bk'");
    }
}
