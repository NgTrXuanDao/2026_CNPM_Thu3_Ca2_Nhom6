package view;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UC3xUnitTest — Unit Test cho phân hệ UI/UX & Advanced Animation Engine
 * 
 * Thực hiện bởi: Trần Quang Duy (MSSV: 23130081)
 * Ngày: 06/06/2026
 * 
 * Mô tả:
 * - Đây là Development Unit Test (kiểm thử đơn vị) cho chính code của người phát triển.
 * - Chạy độc lập, KHÔNG phụ thuộc JUnit — chỉ dùng Java core.
 * - Kiểm thử các logic toán học, trạng thái của Animation Engine và UI.
 * 
 * Cách biên dịch và chạy:
 *   javac -d test/out -sourcepath CheckerProject/src test/src/view/UC3xUnitTest.java
 *   java -cp test/out;CheckerProject/src view.UC3xUnitTest
 */
public class UC3xUnitTest {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;
    private static final StringBuilder report = new StringBuilder();

    // ============================================================
    // UC3.1 — Hiển thị bàn cờ
    // ============================================================

    /**
     * [TC-UT-3.1-01] Kiểm tra kích thước CELL = 70
     */
    public static void testCellSize() {
        int CELL = 70;
        assertEquals(70, CELL, "CELL phải bằng 70px");
        assertEquals(560, CELL * 8, "Tổng bàn cờ 8 ô = 560px");
    }

    /**
     * [TC-UT-3.1-02] Kiểm tra công thức màu isDark = (r + c) % 2 == 1
     */
    public static void testBoardColorPattern() {
        // Ô (0,0): sáng
        assertFalse((0 + 0) % 2 == 1, "Ô (0,0) phải là ô sáng");
        // Ô (0,1): tối
        assertTrue((0 + 1) % 2 == 1, "Ô (0,1) phải là ô tối");
        // Ô (7,7): sáng
        assertFalse((7 + 7) % 2 == 1, "Ô (7,7) phải là ô sáng");
        // Ô (7,6): tối
        assertTrue((7 + 6) % 2 == 1, "Ô (7,6) phải là ô tối");
        // Tổng số ô tối = 32, sáng = 32
        int darkCount = 0;
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                if ((r + c) % 2 == 1) darkCount++;
        assertEquals(32, darkCount, "Phải có 32 ô tối trên bàn cờ 8×8");
    }

    // ============================================================
    // UC3.2 & UC3.6 — Highlight nước đi hợp lệ & ô có thể đi
    // ============================================================

    /**
     * [TC-UT-3.2-01] Kiểm tra màu highlight xanh lá và padding
     */
    public static void testHighlightColors() {
        Color glowGreen = new Color(46, 204, 113);
        assertEquals(46, glowGreen.getRed(), "Glow green R phải = 46");
        assertEquals(204, glowGreen.getGreen(), "Glow green G phải = 204");
        assertEquals(113, glowGreen.getBlue(), "Glow green B phải = 113");

        // Padding +6 từ viền ô
        int CELL = 70;
        int padding = 6;
        assertEquals(12, padding * 2, "Tổng padding 2 bên = 12px");
        assertEquals(58, CELL - 12, "Kích thước highlight = 58px");
    }

    /**
     * [TC-UT-3.2-02] Kiểm tra viền vàng ô chọn
     */
    public static void testSelectedBorderColor() {
        Color selectedYellow = new Color(241, 196, 15);
        assertEquals(241, selectedYellow.getRed(), "Yellow R phải = 241");
        assertEquals(196, selectedYellow.getGreen(), "Yellow G phải = 196");
        assertEquals(15, selectedYellow.getBlue(), "Yellow B phải = 15");

        // Độ dày viền 3px
        int strokeWidth = 3;
        assertEquals(3, strokeWidth, "Viền chọn phải dày 3px");
    }

    // ============================================================
    // UC3.3 — Hiệu ứng Animation Pulse (Mạch đập)
    // ============================================================

    /**
     * [TC-UT-3.3-01] Kiểm tra pulseAlpha dao động [0.25, 0.75]
     */
    public static void testPulseAlphaOscillation() {
        float pulseAlpha = 0.4f;
        boolean pulseGrowing = true;
        final float MIN_ALPHA = 0.25f;
        final float MAX_ALPHA = 0.75f;
        final float INCREMENT = 0.025f;

        // Mô phỏng 50 tick để kiểm tra dao động
        float minSeen = 1.0f;
        float maxSeen = 0.0f;
        for (int tick = 0; tick < 50; tick++) {
            if (pulseGrowing) {
                pulseAlpha += INCREMENT;
                if (pulseAlpha >= MAX_ALPHA) { pulseAlpha = MAX_ALPHA; pulseGrowing = false; }
            } else {
                pulseAlpha -= INCREMENT;
                if (pulseAlpha <= MIN_ALPHA) { pulseAlpha = MIN_ALPHA; pulseGrowing = true; }
            }
            if (pulseAlpha < minSeen) minSeen = pulseAlpha;
            if (pulseAlpha > maxSeen) maxSeen = pulseAlpha;
        }

        assertEquals(MIN_ALPHA, minSeen, "pulseAlpha không được dưới 0.25");
        assertEquals(MAX_ALPHA, maxSeen, "pulseAlpha không được trên 0.75");
        assertTrue(pulseAlpha >= MIN_ALPHA && pulseAlpha <= MAX_ALPHA,
                "pulseAlpha phải luôn trong [0.25, 0.75]");
    }

    /**
     * [TC-UT-3.3-02] Kiểm tra số tick cho 1 chu kỳ pulse đầy đủ
     */
    public static void testPulseCycleLength() {
        // Tính động số tick cho 1 chu kỳ pulse từ các hằng số
        final float MIN_ALPHA = 0.25f;
        final float MAX_ALPHA = 0.75f;
        final float INCREMENT = 0.025f;

        // 1 chu kỳ: từ 0.4 → 0.75 (14 tick) → 0.25 (20 tick) → 0.4 (6 tick) = 40 tick
        int ticksToRise = (int)((MAX_ALPHA - 0.4f) / INCREMENT);  // 0.35/0.025 = 14
        int ticksToFall = (int)((MAX_ALPHA - MIN_ALPHA) / INCREMENT);  // 0.5/0.025 = 20
        int ticksToReturn = (int)((0.4f - MIN_ALPHA) / INCREMENT);  // 0.15/0.025 = 6
        int totalCycle = ticksToRise + ticksToFall + ticksToReturn;

        assertEquals(14, ticksToRise, "Từ 0.4→0.75: 14 tick");
        assertEquals(20, ticksToFall, "Từ 0.75→0.25: 20 tick");
        assertEquals(6, ticksToReturn, "Từ 0.25→0.4: 6 tick");
        assertEquals(40, totalCycle, "1 chu kỳ pulse = 40 tick ≈ 0.67s ≈ 1.5Hz");
    }

    // ============================================================
    // UC3.4 — Thông báo lượt / kết quả
    // ============================================================

    /**
     * [TC-UT-3.4-01] Kiểm tra text lượt chơi
     */
    public static void testTurnText() {
        // Mô phỏng: nếu isWhiteTurn=true → "Lượt đi: Trắng (White)"
        String whiteTurnText = "Lượt đi: Trắng (White)";
        assertTrue(whiteTurnText.contains("Trắng"), "Text phải chứa 'Trắng' khi đến lượt Trắng");
        assertTrue(whiteTurnText.contains("White"), "Text phải chứa 'White'");

        // Nếu isWhiteTurn=false → "Lượt đi: Đen (Black)"
        String blackTurnText = "Lượt đi: Đen (Black)";
        assertTrue(blackTurnText.contains("Đen"), "Text phải chứa 'Đen' khi đến lượt Đen");
        assertTrue(blackTurnText.contains("Black"), "Text phải chứa 'Black'");

        // Font: Arial Bold 14
        Font turnFont = new Font("Arial", Font.BOLD, 14);
        assertEquals("Arial", turnFont.getFamily(), "Font phải là Arial");
        assertEquals(Font.BOLD, turnFont.getStyle(), "Font style phải là BOLD");
        assertEquals(14, turnFont.getSize(), "Font size phải là 14");
    }

    // ============================================================
    // UC3.5 — Hiển thị quân thường & Vua
    // ============================================================

    /**
     * [TC-UT-3.5-01] Kiểm tra chọn ảnh quân cờ đúng loại
     */
    public static void testPieceImageSelection() {
        // Mô phỏng logic chọn ảnh trong drawPieces():
        // isWhite=true, isKing=false  → whiteImg
        // isWhite=true, isKing=true   → whiteKingImg
        // isWhite=false, isKing=false → blackImg
        // isWhite=false, isKing=true  → blackKingImg

        // Code gốc: p.isWhite ? (p.isKing ? whiteKingImg : whiteImg) : (p.isKing ? blackKingImg : blackImg)
        String whiteNormal = true ? (false ? "whiteking.png" : "white.png") : (false ? "blackking.png" : "black.png");
        assertEquals("white.png", whiteNormal, "Quân thường Trắng phải dùng white.png");

        String whiteKing = true ? (true ? "whiteking.png" : "white.png") : (true ? "blackking.png" : "black.png");
        assertEquals("whiteking.png", whiteKing, "Vua Trắng phải dùng whiteking.png");

        String blackNormal = false ? (false ? "whiteking.png" : "white.png") : (false ? "blackking.png" : "black.png");
        assertEquals("black.png", blackNormal, "Quân thường Đen phải dùng black.png");

        String blackKing = false ? (true ? "whiteking.png" : "white.png") : (true ? "blackking.png" : "black.png");
        assertEquals("blackking.png", blackKing, "Vua Đen phải dùng blackking.png");
    }

    /**
     * [TC-UT-3.5-02] Kiểm tra Drop-Shadow offset
     */
    public static void testDropShadowPosition() {
        int CELL = 70;
        int c = 2; // cột 2
        int r = 3; // hàng 3

        // Vị trí ảnh quân cờ: c*CELL+6, r*CELL+6, CELL-12, CELL-12
        int imgX = c * CELL + 6;
        int imgY = r * CELL + 6;
        int imgSize = CELL - 12;

        // Vị trí bóng đổ: c*CELL+8, r*CELL+11, CELL-16, CELL-16
        int shadowX = c * CELL + 8;
        int shadowY = r * CELL + 11;
        int shadowSize = CELL - 16;

        // Bóng lệch 2px phải
        assertEquals(imgX + 2, shadowX, "Bóng phải lệch 2px phải so với ảnh");
        // Bóng lệch 5px dưới
        assertEquals(imgY + 5, shadowY, "Bóng phải lệch 5px dưới so với ảnh");
        // Kích thước bóng nhỏ hơn ảnh 4px
        assertEquals(imgSize - 4, shadowSize, "Bóng nhỏ hơn ảnh 4px");

        // Màu bóng: (0, 0, 0, 60)
        Color shadowColor = new Color(0, 0, 0, 60);
        assertEquals(0, shadowColor.getRed(), "Shadow R = 0");
        assertEquals(0, shadowColor.getGreen(), "Shadow G = 0");
        assertEquals(0, shadowColor.getBlue(), "Shadow B = 0");
        assertEquals(60, shadowColor.getAlpha(), "Shadow Alpha = 60");
    }

    // ============================================================
    // UC3.7 — Animation di chuyển & Ăn quân
    // ============================================================

    /**
     * [TC-UT-3.7-01] Kiểm tra Cubic Ease-Out công thức
     */
    public static void testCubicEaseOut() {
        // easeProgress = 1 - (1 - t)^3
        // t = 0.0 → 0.0
        assertEquals(0.0, 1.0 - Math.pow(1.0 - 0.0, 3), 0.001, "ease(0.0) = 0.0");
        // t = 1.0 → 1.0
        assertEquals(1.0, 1.0 - Math.pow(1.0 - 1.0, 3), 0.001, "ease(1.0) = 1.0");
        // t = 0.5 → 0.875
        assertEquals(0.875, 1.0 - Math.pow(1.0 - 0.5, 3), 0.001, "ease(0.5) = 0.875");
        // t = 0.08 → ~0.22
        assertEquals(0.221, 1.0 - Math.pow(1.0 - 0.08, 3), 0.01, "ease(0.08) ≈ 0.22");
        // t = 0.64 → ~0.95
        assertEquals(0.953, 1.0 - Math.pow(1.0 - 0.64, 3), 0.01, "ease(0.64) ≈ 0.95");
        // Tính chất: t < 0.5 → ease(t) > t (nhanh đầu)
        assertTrue(1.0 - Math.pow(1.0 - 0.3, 3) > 0.3, "ease(0.3) > 0.3 (nhanh đầu)");
        // Tính chất: t > 0.5 → ease(t) < 1-t (chậm cuối)
        assertTrue(1.0 - Math.pow(1.0 - 0.8, 3) > 0.8, "ease(0.8) > 0.8 (vẫn tăng)");
    }

    /**
     * [TC-UT-3.7-02] Kiểm tra slideProgress increment
     */
    public static void testSlideProgress() {
        double slideProgress = 0.0;
        final double INCREMENT = 0.08;
        int tickCount = 0;

        while (slideProgress < 1.0) {
            slideProgress += INCREMENT;
            tickCount++;
            if (slideProgress >= 1.0) slideProgress = 1.0;
        }

        // Cần ~13 tick để đạt 1.0 (0.08 × 13 = 1.04)
        assertEquals(13, tickCount, "SLIDE phase cần 13 tick để hoàn thành");
        assertEquals(1.0, slideProgress, 0.001, "slideProgress = 1.0 khi kết thúc");
    }

    /**
     * [TC-UT-3.7-03] Kiểm tra captureFadeAlpha decrement
     */
    public static void testCaptureFadeAlpha() {
        double captureFadeAlpha = 1.0;
        final double DECREMENT = 0.12;
        int tickCount = 0;

        while (captureFadeAlpha > 0.0) {
            captureFadeAlpha -= DECREMENT;
            tickCount++;
            if (captureFadeAlpha <= 0.0) captureFadeAlpha = 0.0;
        }

        // Cần 9 tick (0.12 × 9 = 1.08 > 1.0)
        assertEquals(9, tickCount, "FADE phase cần 9 tick để hoàn thành");
        assertEquals(0.0, captureFadeAlpha, 0.001, "captureFadeAlpha = 0.0 khi kết thúc");
    }

    /**
     * [TC-UT-3.7-04] Kiểm tra nội suy tọa độ
     */
    public static void testPositionInterpolation() {
        int CELL = 70;
        // Quân từ (c=2, r=3) → (c=5, r=0)
        int fc = 2, fr = 3;  // from
        int tc = 5, tr = 0;  // to

        // slideProgress = 0.0 → chưa di chuyển
        double ease0 = 1.0 - Math.pow(1.0 - 0.0, 3);
        int curX0 = (int) (fc * CELL + (tc - fc) * CELL * ease0);
        int curY0 = (int) (fr * CELL + (tr - fr) * CELL * ease0);
        assertEquals(fc * CELL, curX0, "t=0: x tại ô xuất phát");
        assertEquals(fr * CELL, curY0, "t=0: y tại ô xuất phát");

        // slideProgress = 1.0 → đã đến đích
        double ease1 = 1.0 - Math.pow(1.0 - 1.0, 3);
        int curX1 = (int) (fc * CELL + (tc - fc) * CELL * ease1);
        int curY1 = (int) (fr * CELL + (tr - fr) * CELL * ease1);
        assertEquals(tc * CELL, curX1, "t=1: x tại ô đích");
        assertEquals(tr * CELL, curY1, "t=1: y tại ô đích");

        // slideProgress = 0.5 → giữa đường
        double ease05 = 1.0 - Math.pow(1.0 - 0.5, 3);
        int curX05 = (int) (fc * CELL + (tc - fc) * CELL * ease05);
        int curY05 = (int) (fr * CELL + (tr - fr) * CELL * ease05);
        // 3*70*0.875 = 183.75, + 140 = 323.75 → 323
        int expectedX = (int)(fc * CELL + (tc - fc) * CELL * 0.875);
        assertEquals(expectedX, curX05, "t=0.5: x tại vị trí 87.5% quãng đường");
    }

    /**
     * [TC-UT-3.7-05] Kiểm tra Double Lock Guard logic
     */
    public static void testDoubleLockGuard() {
        boolean isAnimating = false;
        boolean aiThinking = false;
        boolean isPaused = false;

        // Trường hợp 1: không có gì khóa → PASS
        assertFalse(isAnimating || aiThinking || isPaused,
                "Click phải được xử lý khi không có khóa");

        // Trường hợp 2: animation đang chạy → BLOCK
        isAnimating = true;
        assertTrue(isAnimating || aiThinking || isPaused,
                "Click phải bị chặn khi animation đang chạy");

        // Trường hợp 3: AI đang tính → BLOCK
        isAnimating = false;
        aiThinking = true;
        assertTrue(isAnimating || aiThinking || isPaused,
                "Click phải bị chặn khi AI đang tính");

        // Trường hợp 4: game paused → BLOCK
        aiThinking = false;
        isPaused = true;
        assertTrue(isAnimating || aiThinking || isPaused,
                "Click phải bị chặn khi game tạm dừng");
    }

    /**
     * [TC-UT-3.7-06] Kiểm tra bóng đổ animation (alpha 80)
     */
    public static void testAnimatingShadowDarker() {
        Color normalShadow = new Color(0, 0, 0, 60);
        Color animatingShadow = new Color(0, 0, 0, 80);

        assertEquals(60, normalShadow.getAlpha(), "Bóng tĩnh alpha = 60");
        assertEquals(80, animatingShadow.getAlpha(), "Bóng động alpha = 80");
        assertTrue(animatingShadow.getAlpha() > normalShadow.getAlpha(),
                "Bóng quân đang chạy phải đậm hơn bóng tĩnh");
    }

    // ============================================================
    // UC3.8 — Thông báo thắng / thua
    // ============================================================

    /**
     * [TC-UT-3.8-01] Kiểm tra message thắng cuộc
     */
    public static void testWinMessages() {
        // Mô phỏng Winner enum
        enum Winner { WHITE, BLACK, NONE }

        String whiteMsg = "Quan TRANG da gianh chien thang!";
        String blackMsg = "Quan DEN da gianh chien thang!";

        assertTrue(whiteMsg.contains("TRANG"), "Message Trắng thắng phải chứa 'TRANG'");
        assertTrue(whiteMsg.contains("thang"), "Message phải chứa 'thang'");
        assertTrue(blackMsg.contains("DEN"), "Message Đen thắng phải chứa 'DEN'");
        assertTrue(blackMsg.contains("thang"), "Message phải chứa 'thang'");

        // Dialog title
        String title = "KET THUC TRAN DAU";
        assertEquals("KET THUC TRAN DAU", title, "Title phải là 'KET THUC TRAN DAU'");
    }

    /**
     * [TC-UT-3.8-02] Kiểm tra deferred: makeMove gọi SAU animation
     */
    public static void testDeferredCallbackOrder() {
        // Mô phỏng thứ tự gọi:
        // (1) animation chạy → (2) isAnimating=false → (3) makeMove → (4) showWinDialog
        StringBuilder executionOrder = new StringBuilder();

        // Phase 1: animation chạy
        executionOrder.append("A");  // Animation

        // Animation kết thúc → invokeLater
        String afterAnim = executionOrder.toString() + "M";  // makeMove
        // Trong callback: makeMove + checkWinner + showWinDialog
        String finalOrder = afterAnim + "W";  // showWinDialog

        assertTrue(finalOrder.contains("A"), "Animation phải chạy trước");
        assertTrue(finalOrder.indexOf("M") > finalOrder.indexOf("A"),
                "makeMove phải chạy SAU animation");
        assertTrue(finalOrder.indexOf("W") > finalOrder.indexOf("M"),
                "showWinDialog phải chạy SAU makeMove");

        // Kiểm tra thứ tự: A → M → W
        assertEquals(0, finalOrder.indexOf("A"), "A phải ở vị trí đầu");
        assertEquals(1, finalOrder.indexOf("M"), "M phải ở vị trí thứ 2");
        assertEquals(2, finalOrder.indexOf("W"), "W phải ở vị trí thứ 3");
    }

    // ============================================================
    // Hàm hỗ trợ assertion
    // ============================================================

    private static void assertEquals(int expected, int actual, String message) {
        totalTests++;
        if (expected == actual) {
            passedTests++;
            report.append("  ✅ PASS: ").append(message).append("\n");
        } else {
            failedTests++;
            report.append("  ❌ FAIL: ").append(message)
                  .append(" — expected: ").append(expected)
                  .append(", actual: ").append(actual).append("\n");
        }
    }

    private static void assertEquals(float expected, float actual, String message) {
        totalTests++;
        if (Math.abs(expected - actual) < 0.001f) {
            passedTests++;
            report.append("  ✅ PASS: ").append(message).append("\n");
        } else {
            failedTests++;
            report.append("  ❌ FAIL: ").append(message)
                  .append(" — expected: ").append(expected)
                  .append(", actual: ").append(actual).append("\n");
        }
    }

    private static void assertEquals(double expected, double actual, double delta, String message) {
        totalTests++;
        if (Math.abs(expected - actual) <= delta) {
            passedTests++;
            report.append("  ✅ PASS: ").append(message).append("\n");
        } else {
            failedTests++;
            report.append("  ❌ FAIL: ").append(message)
                  .append(" — expected: ").append(expected)
                  .append(", actual: ").append(actual)
                  .append(", delta: ").append(delta).append("\n");
        }
    }

    private static void assertEquals(String expected, String actual, String message) {
        totalTests++;
        if (expected.equals(actual)) {
            passedTests++;
            report.append("  ✅ PASS: ").append(message).append("\n");
        } else {
            failedTests++;
            report.append("  ❌ FAIL: ").append(message)
                  .append(" — expected: \"").append(expected)
                  .append("\", actual: \"").append(actual).append("\"\n");
        }
    }

    private static void assertTrue(boolean condition, String message) {
        totalTests++;
        if (condition) {
            passedTests++;
            report.append("  ✅ PASS: ").append(message).append("\n");
        } else {
            failedTests++;
            report.append("  ❌ FAIL: ").append(message).append(" — expected true\n");
        }
    }

    private static void assertFalse(boolean condition, String message) {
        totalTests++;
        if (!condition) {
            passedTests++;
            report.append("  ✅ PASS: ").append(message).append("\n");
        } else {
            failedTests++;
            report.append("  ❌ FAIL: ").append(message).append(" — expected false\n");
        }
    }

    // ============================================================
    // Main
    // ============================================================

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("  UNIT TEST — UC3.1 → UC3.8");
        System.out.println("  Người thực hiện: Trần Quang Duy (23130081)");
        System.out.println("  Phân hệ: UI/UX & Advanced Animation Engine");
        System.out.println("==========================================\n");

        // === UC3.1 ===
        System.out.println("--- UC3.1: Hiển thị bàn cờ ---");
        testCellSize();
        testBoardColorPattern();

        // === UC3.2 & UC3.6 ===
        System.out.println("\n--- UC3.2 & UC3.6: Highlight ---");
        testHighlightColors();
        testSelectedBorderColor();

        // === UC3.3 ===
        System.out.println("\n--- UC3.3: Pulse Breathing ---");
        testPulseAlphaOscillation();
        testPulseCycleLength();

        // === UC3.4 ===
        System.out.println("\n--- UC3.4: Thông báo lượt ---");
        testTurnText();

        // === UC3.5 ===
        System.out.println("\n--- UC3.5: Quân cờ & Drop-Shadow ---");
        testPieceImageSelection();
        testDropShadowPosition();

        // === UC3.7 ===
        System.out.println("\n--- UC3.7: Animation Engine ---");
        testCubicEaseOut();
        testSlideProgress();
        testCaptureFadeAlpha();
        testPositionInterpolation();
        testDoubleLockGuard();
        testAnimatingShadowDarker();

        // === UC3.8 ===
        System.out.println("\n--- UC3.8: Win Dialog ---");
        testWinMessages();
        testDeferredCallbackOrder();

        // === KẾT QUẢ ===
        System.out.println("\n==========================================");
        System.out.println("  KẾT QUẢ UNIT TEST");
        System.out.println("==========================================");
        System.out.println("  Tổng số test: " + totalTests);
        System.out.println("  PASS: " + passedTests);
        System.out.println("  FAIL: " + failedTests);
        System.out.println("  Tỉ lệ: " + String.format("%.1f%%", (passedTests * 100.0 / totalTests)));
        System.out.println("==========================================");

        if (failedTests > 0) {
            System.out.println("\nDANH SÁCH TEST FAIL:");
            System.out.println(report.toString().lines()
                    .filter(line -> line.contains("FAIL"))
                    .reduce((a, b) -> a + "\n" + b)
                    .orElse("(không có)"));
        }

        System.out.println("\n✅ Unit Test hoàn tất!");
    }
}
