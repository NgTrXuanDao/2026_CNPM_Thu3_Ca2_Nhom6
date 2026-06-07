package controller;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import model.Board;
import model.Move;
import model.Piece;

/**
 * GameControllerTest - Kiểm thử các UC liên quan đến Luật chơi (Rule Engine),
 * bắt buộc ăn quân, chế độ chơi PvP/PvE và kiểm tra tính hợp lệ của nước đi
 * 
 * Người thực hiện: Trần Trọng Bảo (23130024)
 * Ngày: 07/06/2026
 * 
 * Liên quan đến các UC:
 * - UC5.1 - Kiểm tra nước đi hợp lệ
 * - UC5.2 - Bắt buộc ăn quân nếu có thể
 * - UC5.3 - Không đi vào ô đã có quân
 * - UC5.4 - Chặn nước đi thường khi có thể ăn
 * - UC2.1 - Chế độ PvP (người vs người)
 * - UC2.2 - Chế độ PvE (người vs máy)
 * - UC1.1 - Khởi tạo ván chơi
 * - UC1.2 - Di chuyển quân cờ
 * - UC1.10 - Chọn quân & hiển thị nước đi hợp lệ
 * - UC1.11 - Đi chéo 1 ô
 * - UC1.12 - Nhảy qua quân đối phương
 * - UC1.13 - Xóa quân bị ăn
 */
class GameControllerTest_TrongBao {

    private Board board;
    private GameController controller;

    @BeforeEach
    void setUp() {
        board = new Board();
        controller = new GameController(board);
    }

    // ========================================================================
    // UC5.1 - Kiểm tra nước đi hợp lệ
    // ========================================================================

    @Test
    @DisplayName("UC5.1.1 - Từ chối nước đi ra ngoài biên")
    void testInvalidMoveOutOfBounds() {
        Board b = new Board(new Piece[8][8]);
        b.setPiece(0, 0, new Piece(true));
        GameController gc = new GameController(b, true);

        List<Move> moves = gc.getValidMoves(0, 0);
        assertTrue(moves.isEmpty(), 
                "Quân ở góc (0,0) không có nước đi hợp lệ");
    }

    @Test
    @DisplayName("UC5.1.2 - Chỉ cho phép nước đi chéo")
    void testOnlyDiagonalMoves() {
        List<Move> moves = controller.getValidMoves(5, 2);
        for (Move m : moves) {
            int dr = Math.abs(m.getToRow() - m.getFromRow());
            int dc = Math.abs(m.getToCol() - m.getFromCol());
            assertEquals(1, dr, "dr phải = 1 (đi chéo)");
            assertEquals(1, dc, "dc phải = 1 (đi chéo)");
        }
    }

    @Test
    @DisplayName("UC5.1.3 - Nước đi không được thay đổi quân")
    void testMoveDoesNotCreateNewPiece() {
        controller.makeMove(new Move(5, 0, 4, 1));
        Piece moved = board.getPiece(4, 1);
        assertNotNull(moved, "Quân phải tồn tại ở ô đích");
        assertTrue(moved.isWhite, "Quân ở ô đích phải là trắng");
        assertFalse(moved.isKing, "Quân chưa phải vua");
    }

    // ========================================================================
    // UC5.2 + UC5.4 - Bắt buộc ăn quân nếu có thể
    // ========================================================================

    @Test
    @DisplayName("UC5.2.1 - Khi có thể ăn, chỉ hiển thị nước ăn")
    void testOnlyCaptureWhenAvailable() {
        Board b = new Board(new Piece[8][8]);
        b.setPiece(5, 0, new Piece(true));   // quân trắng
        b.setPiece(4, 1, new Piece(false));  // quân đen kề
        b.setPiece(5, 2, new Piece(true));   // quân trắng khác (có thể normal)
        
        GameController gc = new GameController(b, true);
        
        // Quân ở (5,0) có thể ăn → chỉ trả về capture
        List<Move> moves1 = gc.getValidMoves(5, 0);
        assertFalse(moves1.isEmpty(), "Phải có nước ăn");
        for (Move m : moves1) {
            assertTrue(m.isCapture(), "Bắt buộc ăn");
        }
        
        // Quân ở (5,2) không thể ăn (không có quân đen kề) → normal move
        List<Move> moves2 = gc.getValidMoves(5, 2);
        if (!moves2.isEmpty()) {
            for (Move m : moves2) {
                assertFalse(m.isCapture(), "Không có quân để ăn");
            }
        }
    }

    @Test
    @DisplayName("UC5.2.2 - Bắt buộc ăn: capture priority")
    void testCapturePriority() {
        Board b = new Board(new Piece[8][8]);
        b.setPiece(5, 0, new Piece(true));   // quân trắng
        b.setPiece(3, 2, new Piece(true));   // quân trắng khác  
        b.setPiece(4, 1, new Piece(false));  // quân đen bị ăn
        
        GameController gc = new GameController(b, true);
        List<Move> moves = gc.getValidMoves(5, 0);
        
        assertFalse(moves.isEmpty(), "Phải có nước ăn");
        assertTrue(moves.get(0).isCapture(), "Nước đi phải là capture");
    }

    @Test
    @DisplayName("UC5.2.3 - Không ăn quân cùng màu")
    void testNoFriendlyFire() {
        Board b = new Board(new Piece[8][8]);
        b.setPiece(5, 0, new Piece(true));   // quân trắng
        b.setPiece(4, 1, new Piece(true));   // đồng minh - không được ăn
        
        GameController gc = new GameController(b, true);
        List<Move> moves = gc.getValidMoves(5, 0);
        
        // (5,0) không thể đi đâu vì (4,1) có đồng minh, không capture
        // Và (5,0) không có normal move đến (4,1) vì có quân
        assertTrue(moves.isEmpty(), 
                "Quân bị chặn bởi đồng minh, không có nước đi");
    }

    // ========================================================================
    // UC5.3 - Không đi vào ô đã có quân
    // ========================================================================

    @Test
    @DisplayName("UC5.3.1 - Không đi vào ô có quân đối phương (normal)")
    void testNoMoveIntoEnemy() {
        Board b = new Board(new Piece[8][8]);
        b.setPiece(5, 0, new Piece(true));   // quân trắng
        b.setPiece(4, 1, new Piece(false));  // quân đen
        
        GameController gc = new GameController(b, true);
        List<Move> moves = gc.getValidMoves(5, 0);
        
        // Có thể có capture, nhưng không được có normal move đến (4,1)
        for (Move m : moves) {
            assertFalse(!m.isCapture() && m.getToRow() == 4 && m.getToCol() == 1,
                    "Không được normal move vào ô có quân");
        }
    }

    @Test
    @DisplayName("UC5.3.2 - Không đi vào ô có quân đồng minh")
    void testNoMoveIntoFriendly() {
        Board b = new Board(new Piece[8][8]);
        b.setPiece(5, 0, new Piece(true));   // quân trắng
        b.setPiece(4, 1, new Piece(true));   // đồng minh
        
        GameController gc = new GameController(b, true);
        List<Move> moves = gc.getValidMoves(5, 0);
        
        for (Move m : moves) {
            assertFalse(m.getToRow() == 4 && m.getToCol() == 1,
                    "Không được đi vào ô có đồng minh");
        }
    }

    @Test
    @DisplayName("UC5.3.3 - Không nhảy vào ô đã có quân (capture blocked)")
    void testCaptureBlocked() {
        Board b = new Board(new Piece[8][8]);
        b.setPiece(5, 0, new Piece(true));   // quân trắng
        b.setPiece(4, 1, new Piece(false));  // quân đen
        b.setPiece(3, 2, new Piece(true));   // quân trắng chặn ô đích
        
        GameController gc = new GameController(b, true);
        List<Move> moves = gc.getValidMoves(5, 0);
        
        // Không thể capture vì (3,2) có quân
        boolean hasCapture = false;
        for (Move m : moves) {
            if (m.isCapture()) hasCapture = true;
        }
        assertFalse(hasCapture, 
                "Không thể ăn khi ô đích có quân");
    }

    // ========================================================================
    // UC2.1 + UC2.2 - Chế độ chơi PvP và PvE
    // ========================================================================

    @Test
    @DisplayName("UC2.1.1 - PvP: Có thể chơi 2 người liên tục")
    void testPvPGameFlow() {
        // Mô phỏng PvP: 2 người chơi xen kẽ
        assertTrue(controller.isWhiteTurn(), "Lượt trắng");

        // Người trắng đi
        controller.makeMove(new Move(5, 0, 4, 1));
        assertFalse(controller.isWhiteTurn(), "Lượt đen sau White đi");

        // Người đen đi
        controller.makeMove(new Move(2, 1, 3, 0));
        assertTrue(controller.isWhiteTurn(), "Lượt trắng sau Black đi");
    }

    @Test
    @DisplayName("UC2.1.2 - PvP: Đổi lượt sau mỗi nước đi")
    void testPvPTurnSwitching() {
        boolean startTurn = controller.isWhiteTurn();
        
        controller.makeMove(new Move(5, 0, 4, 1));
        assertNotEquals(startTurn, controller.isWhiteTurn(),
                "Lượt phải đổi sau mỗi nước đi");
        
        controller.makeMove(new Move(2, 1, 3, 2));
        assertEquals(startTurn, controller.isWhiteTurn(),
                "Lượt phải quay lại sau 2 nước đi");
    }

    @Test
    @DisplayName("UC2.2.1 - PvE: AI có thể tính nước đi (getAllMoves)")
    void testPvEGetAllMoves() {
        // Trong chế độ PvE, AI dùng getAllMoves để tìm nước đi
        List<Move> whiteMoves = controller.getAllMoves(true);
        assertFalse(whiteMoves.isEmpty(), 
                "AI phải tìm được nước đi cho trắng");
        
        List<Move> blackMoves = controller.getAllMoves(false);
        assertFalse(blackMoves.isEmpty(), 
                "AI phải tìm được nước đi cho đen");
    }

    // ========================================================================
    // UC1.13 - Xóa quân bị ăn
    // ========================================================================

    @Test
    @DisplayName("UC1.13.1 - applyMove xóa quân bị ăn khỏi bàn cờ")
    void testRemoveCapturedPiece() {
        Board b = new Board(new Piece[8][8]);
        b.setPiece(5, 0, new Piece(true));
        b.setPiece(4, 1, new Piece(false));

        Move capture = new Move(5, 0, 3, 2);
        capture.addCapture(4, 1);
        GameController.applyMove(b, capture);

        assertNull(b.getPiece(5, 0), "Ô nguồn trống");
        assertNull(b.getPiece(4, 1), "Ô bị ăn trống");
        assertNotNull(b.getPiece(3, 2), "Ô đích có quân");
    }

    @Test
    @DisplayName("UC1.13.2 - Xóa nhiều quân trong chuỗi ăn")
    void testRemoveMultipleCaptured() {
        Board b = new Board(new Piece[8][8]);
        b.setPiece(5, 0, new Piece(true));
        b.setPiece(4, 1, new Piece(false));
        b.setPiece(2, 3, new Piece(false));

        // Tạo double jump move
        Move doubleCapture = new Move(5, 0, 1, 4);
        doubleCapture.addCapture(4, 1);
        doubleCapture.addCapture(2, 3);
        doubleCapture.addStep(3, 2);
        doubleCapture.addStep(1, 4);

        GameController.applyMove(b, doubleCapture);

        assertNull(b.getPiece(4, 1), "Quân đen 1 bị xóa");
        assertNull(b.getPiece(2, 3), "Quân đen 2 bị xóa");
        assertNotNull(b.getPiece(1, 4), "Quân trắng ở ô đích");
    }

    // ========================================================================
    // UC1.1 - Khởi tạo ván chơi
    // ========================================================================

    @Test
    @DisplayName("UC1.1.1 - Board khởi tạo đúng 24 quân")
    void testBoardInitialization() {
        assertEquals(12, board.countWhitePieces(), "12 quân trắng");
        assertEquals(12, board.countBlackPieces(), "12 quân đen");
    }

    @Test
    @DisplayName("UC1.1.2 - Reset game khởi tạo lại bàn cờ")
    void testResetGameRestoresBoard() {
        controller.makeMove(new Move(5, 0, 4, 1));
        controller.resetGame(null);

        assertEquals(12, board.countWhitePieces(), 
                "Reset khôi phục 12 quân trắng");
        assertEquals(12, board.countBlackPieces(),
                "Reset khôi phục 12 quân đen");
    }
}
