package model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * FirstTurnModeTest - Kiểm thử Enum FirstTurnMode
 * Người thực hiện: Đoàn Ngọc Ánh
 * Ngày: 07/06/2026
 * 
 * Liên quan đến UC:
 * - UC1.9 - Xác định người đi trước
 */
class FirstTurnModeTest {

    // ========================================================================
    // UC1.9 - Xác định người đi trước
    // ========================================================================

    @Test
    @DisplayName("UC1.9.1 - Có đúng 3 chế độ: WHITE, BLACK, RANDOM")
    void testEnumValues() {
        FirstTurnMode[] values = FirstTurnMode.values();
        assertEquals(3, values.length, "Phải có 3 chế độ");
        
        // Kiểm tra tên các giá trị
        assertEquals("WHITE", values[0].name());
        assertEquals("BLACK", values[1].name());
        assertEquals("RANDOM", values[2].name());
    }

    @Test
    @DisplayName("UC1.9.2 - Có thể sử dụng từng chế độ")
    void testEnumUsage() {
        // WHITE - Trắng đi trước
        FirstTurnMode whiteMode = FirstTurnMode.WHITE;
        assertNotNull(whiteMode);
        assertEquals(FirstTurnMode.WHITE, whiteMode);

        // BLACK - Đen đi trước
        FirstTurnMode blackMode = FirstTurnMode.BLACK;
        assertNotNull(blackMode);
        assertEquals(FirstTurnMode.BLACK, blackMode);

        // RANDOM - Ngẫu nhiên
        FirstTurnMode randomMode = FirstTurnMode.RANDOM;
        assertNotNull(randomMode);
        assertEquals(FirstTurnMode.RANDOM, randomMode);
    }

    @Test
    @DisplayName("UC1.9.3 - WHITE != BLACK != RANDOM")
    void testEnumInequality() {
        assertNotEquals(FirstTurnMode.WHITE, FirstTurnMode.BLACK);
        assertNotEquals(FirstTurnMode.WHITE, FirstTurnMode.RANDOM);
        assertNotEquals(FirstTurnMode.BLACK, FirstTurnMode.RANDOM);
    }

    @Test
    @DisplayName("UC1.9.4 - Chuyển enum thành string")
    void testEnumToString() {
        assertEquals("WHITE", FirstTurnMode.WHITE.toString());
        assertEquals("BLACK", FirstTurnMode.BLACK.toString());
        assertEquals("RANDOM", FirstTurnMode.RANDOM.toString());
    }

    @Test
    @DisplayName("UC1.9.5 - valueOf trả về đúng giá trị")
    void testValueOf() {
        assertEquals(FirstTurnMode.WHITE, FirstTurnMode.valueOf("WHITE"));
        assertEquals(FirstTurnMode.BLACK, FirstTurnMode.valueOf("BLACK"));
        assertEquals(FirstTurnMode.RANDOM, FirstTurnMode.valueOf("RANDOM"));
    }

    @Test
    @DisplayName("UC1.9.6 - valueOf với tên không hợp lệ throw IllegalArgumentException")
    void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, 
                () -> FirstTurnMode.valueOf("INVALID"));
    }
}
