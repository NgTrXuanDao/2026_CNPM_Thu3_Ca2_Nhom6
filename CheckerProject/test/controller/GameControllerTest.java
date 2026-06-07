package controller;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Point;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import model.Board;
import model.FirstTurnMode;
import model.Move;
import model.Piece;

/**
 * GameControllerTest - Kiểm thử lớp GameController
 * Người thực hiện: Đoàn Ngọc Ánh
 * Ngày: 07/06/2026
 * 
 * Liên quan đến các UC:
 * - UC1.1 - Khởi tạo ván chơi
 * - UC1.2 - Di chuyển quân cờ
 * - UC1.9 - Xác định người đi trước
 * - UC1.10 - Chọn quân & hiển thị nước đi hợp lệ
 * - UC1.11 - Đi chéo 1 ô
 * - UC1.12 - Nhảy qua quân đối phương
 * - UC1.13 - Xóa quân bị ăn
 * - UC1.14 - Kiểm tra chuỗi ăn tiếp theo
 */
class GameControllerTest {

    private Board board;
    private GameController controller;

    @BeforeEach
    void setUp() {
        board = new Board();
        controller = new GameController(board);
    }

    // ========================================================================
    // UC1.1 - Khởi tạo ván chơi
    // ========================================================================

    @Test
    @DisplayName("UC1.1.1 - GameController(Board) khởi tạo với whiteTurn = true")
    void testConstructorDefault() {
        GameController gc = new GameController(new Board());
        assertTrue(gc.isWhiteTurn(), "Mặc định White đi trước");
        assertNotNull(gc.getBoard(), "Board không được null");
    }

    @Test
    @DisplayName("UC1.1.2 - GameController(Board, boolean) khởi tạo với lượt chỉ định")
    void testConstructorWithTurn() {
        GameController gcWhite = new GameController(new Board(), true);
        assertTrue(gcWhite.isWhiteTurn());

        GameController gcBlack = new GameController(new Board(), false);
        assertFalse(gcBlack.isWhiteTurn());
    }

    @Test
    @DisplayName("UC1.1.3 - GameController(Board, FirstTurnMode.WHITE) khởi tạo White đi trước")
    void testConstructorWithModeWhite() {
        GameController gc = new GameController(new Board(), FirstTurnMode.WHITE);
        assertTrue(gc.isWhiteTurn(), "WHITE -> White đi trước");
    }

    @Test
    @DisplayName("UC1.1.4 - GameController(Board, FirstTurnMode.BLACK) khởi tạo Black đi trước")
    void testConstructorWithModeBlack() {
        GameController gc = new GameController(new Board(), FirstTurnMode.BLACK);
        assertFalse(gc.isWhiteTurn(), "BLACK -> Black đi trước");
    }

    // ========================================================================
    // UC1.9 - Xác định người đi trước
    // ========================================================================

    @Test
    @DisplayName("UC1.9.1 - resolveFirstTurn(WHITE) = true")
    void testResolveFirstTurnWhite() {
        assertTrue(GameController.resolveFirstTurn(FirstTurnMode.WHITE));
    }

    @Test
    @DisplayName("UC1.9.2 - resolveFirstTurn(BLACK) = false")
    void testResolveFirstTurnBlack() {
        assertFalse(GameController.resolveFirstTurn(FirstTurnMode.BLACK));
    }

    @Test
    @DisplayName("UC1.9.3 - resolveFirstTurn(null) = true (fallback)")
    void testResolveFirstTurnNull() {
        assertTrue(GameController.resolveFirstTurn(null), 
                "Null safety -> White đi trước");
    }

    @Test
    @DisplayName("UC1.9.4 - resolveFirstTurn(RANDOM) trả về cả true và false qua nhiều lần")
    void testResolveFirstTurnRandom() {
        boolean hasTrue = false;
        boolean hasFalse = false;
        // Chạy 50 lần để đảm bảo cả 2 giá trị đều xuất hiện
        for (int i = 0; i < 50; i++) {
            boolean result = GameController.resolveFirstTurn(FirstTurnMode.RANDOM);
            if (result) hasTrue = true;
            else hasFalse = true;
            if (hasTrue && hasFalse) break;
        }
        assertTrue(hasTrue && hasFalse, 
                "RANDOM phải trả về cả true và false sau 50 lần");
    }

    @Test
    @DisplayName("UC1.9.5 - setFirstTurn thay đổi lượt")
    void testSetFirstTurn() {
        assertTrue(controller.isWhiteTurn(), "Mặc định White đi trước");
        
        controller.setFirstTurn(FirstTurnMode.BLACK);
        assertFalse(controller.isWhiteTurn(), "Sau setFirstTurn(BLACK) -> Black đi trước");
        
        controller.setFirstTurn(FirstTurnMode.WHITE);
        assertTrue(controller.isWhiteTurn(), "Sau setFirstTurn(WHITE) -> White đi trước");
    }

    @Test
    @DisplayName("UC1.9.6 - resetGame khởi tạo lại bàn cờ và lượt đi")
    void testResetGame() {
        // Thay đổi trạng thái
        controller.makeMove(new Move(5, 0, 4, 1));
        assertFalse(controller.isWhiteTurn(), "Sau 1 nước đi, đổi lượt");
        
        // Reset
        controller.resetGame(FirstTurnMode.WHITE);
        assertTrue(controller.isWhiteTurn(), "Reset(WHITE) -> White đi trước");
        assertEquals(12, controller.getBoard().countWhitePieces(), 
                "Reset phải khôi phục quân trắng");
        assertEquals(12, controller.getBoard().countBlackPieces(), 
                "Reset phải khôi phục quân đen");
    }

    @Test
    @DisplayName("UC1.9.7 - resetGame với mode null giữ nguyên lượt cũ")
    void testResetGameNullMode() {
        controller.setFirstTurn(FirstTurnMode.BLACK);
        controller.resetGame(null);
        assertFalse(controller.isWhiteTurn(), 
                "resetGame(null) phải giữ nguyên lượt");
        assertEquals(12, controller.getBoard().countWhitePieces());
    }

    // ========================================================================
    // UC1.10 - Chọn quân & hiển thị nước đi hợp lệ
    // ========================================================================

    @Test
    @DisplayName("UC1.10.1 - getValidMoves ô không có quân -> list rỗng")
    void testGetValidMovesNoPiece() {
        // Ô (3,3) không có quân
        List<Move> moves = controller.getValidMoves(3, 3);
        assertTrue(moves.isEmpty(), "Ô không quân phải trả về list rỗng");
    }

    @Test
    @DisplayName("UC1.10.2 - getValidMoves sai lượt -> list rỗng")
    void testGetValidMovesWrongTurn() {
        // Quân trắng ở (5,0), nhưng controller đang là White turn
        List<Move> moves = controller.getValidMoves(2, 1); // quân đen
        assertTrue(moves.isEmpty(), "Sai lượt phải trả về list rỗng");
    }

    @Test
    @DisplayName("UC1.10.3 - getValidMoves đúng lượt -> trả về nước đi")
    void testGetValidMovesCorrectTurn() {
        // Quân trắng ở (5,0) đúng lượt White
        List<Move> moves = controller.getValidMoves(5, 0);
        assertFalse(moves.isEmpty(), "Quân đúng lượt phải có nước đi");
        
        // Quân trắng ở (5,0) chỉ có thể đi đến (4,1)
        boolean found = false;
        for (Move m : moves) {
            if (m.getToRow() == 4 && m.getToCol() == 1) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Phải có nước đi đến (4,1)");
    }

    // ========================================================================
    // UC1.11 - Đi chéo 1 ô
    // ========================================================================

    @Test
    @DisplayName("UC1.11.1 - Quân trắng thường chỉ đi lên (hàng giảm)")
    void testWhitePieceMovesForward() {
        // Quân trắng ở (5,0): chỉ đi lên (4,1)
        List<Move> moves = controller.getValidMoves(5, 0);
        for (Move m : moves) {
            assertTrue(m.getToRow() < 5, 
                    "Quân trắng phải đi lên (hàng giảm)");
        }
    }

    @Test
    @DisplayName("UC1.11.2 - Quân đen chỉ đi xuống (hàng tăng)")
    void testBlackPieceMovesForward() {
        // Chỉnh lượt về đen để test quân đen
        controller.setFirstTurn(FirstTurnMode.BLACK);
        
        // Quân đen ở (2,1): chỉ đi xuống (3,0) hoặc (3,2)
        List<Move> moves = controller.getValidMoves(2, 1);
        for (Move m : moves) {
            assertTrue(m.getToRow() > 2, 
                    "Quân đen phải đi xuống (hàng tăng)");
        }
    }

    @Test
    @DisplayName("UC1.11.3 - Vua đi được cả 4 hướng")
    void testKingMovesAllDirections() {
        // Tạo board với vua trắng ở giữa
        Board testBoard = new Board(new Piece[8][8]);
        testBoard.setPiece(4, 3, new Piece(true, true)); // vua trắng
        GameController gc = new GameController(testBoard, true);
        
        List<Move> moves = gc.getValidMoves(4, 3);
        
        // Vua ở (4,3) có thể đi 4 hướng (nếu trống)
        assertFalse(moves.isEmpty(), "Vua phải có nước đi");
        
        // Kiểm tra có cả hướng lên và xuống
        boolean hasUp = false, hasDown = false;
        for (Move m : moves) {
            if (m.getToRow() < 4) hasUp = true;
            if (m.getToRow() > 4) hasDown = true;
        }
        assertTrue(hasUp, "Vua phải có thể đi lên");
        assertTrue(hasDown, "Vua phải có thể đi xuống");
    }

    // ========================================================================
    // UC1.2 - Di chuyển quân cờ
    // ========================================================================

    @Test
    @DisplayName("UC1.2.1 - applyMove di chuyển quân từ ô nguồn đến ô đích")
    void testApplyMove() {
        Board b = new Board();
        Move move = new Move(5, 0, 4, 1);
        
        assertNotNull(b.getPiece(5, 0), "Ô nguồn phải có quân trước");
        assertNull(b.getPiece(4, 1), "Ô đích phải trống trước");
        
        GameController.applyMove(b, move);
        
        assertNull(b.getPiece(5, 0), "Ô nguồn phải trống sau khi di chuyển");
        assertNotNull(b.getPiece(4, 1), "Ô đích phải có quân sau khi di chuyển");
        assertTrue(b.getPiece(4, 1).isWhite, "Quân ở ô đích phải là trắng");
    }

    @Test
    @DisplayName("UC1.2.2 - applyMove không làm gì với null move")
    void testApplyMoveNullMove() {
        Board b = new Board();
        assertDoesNotThrow(() -> GameController.applyMove(b, null));
    }

    @Test
    @DisplayName("UC1.2.3 - makeMove di chuyển và đổi lượt")
    void testMakeMove() {
        assertTrue(controller.isWhiteTurn(), "Ban đầu White đi trước");
        
        controller.makeMove(new Move(5, 0, 4, 1));
        
        assertFalse(controller.isWhiteTurn(), "Sau 1 nước đi, đổi lượt");
        assertNull(controller.getBoard().getPiece(5, 0), "Ô nguồn trống");
        assertNotNull(controller.getBoard().getPiece(4, 1), "Ô đích có quân");
    }

    @Test
    @DisplayName("UC1.2.4 - applyMove phong cấp vua khi đến hàng cuối")
    void testApplyMovePromotion() {
        Board b = new Board();
        // Đặt quân trắng ngay trước hàng cuối
        b.clearCell(5, 0);
        b.setPiece(1, 0, new Piece(true));
        
        Move move = new Move(1, 0, 0, 1);
        GameController.applyMove(b, move);
        
        Piece placed = b.getPiece(0, 1);
        assertNotNull(placed, "Phải có quân ở ô đích");
        assertTrue(placed.isKing, "Quân đến hàng cuối phải thành vua");
    }

    // ========================================================================
    // UC1.12 - Nhảy qua quân đối phương
    // ========================================================================

    @Test
    @DisplayName("UC1.12.1 - getValidMoves phát hiện nước ăn quân")
    void testGetValidMovesWithCapture() {
        // Tạo bàn cờ với tình huống ăn quân
        Board testBoard = new Board(new Piece[8][8]);
        testBoard.setPiece(5, 0, new Piece(true));  // quân trắng
        testBoard.setPiece(4, 1, new Piece(false)); // quân đen kề
        // Ô đích trống
        GameController gc = new GameController(testBoard, true);
        
        List<Move> moves = gc.getValidMoves(5, 0);
        
        // Phải có nước ăn đến (3,2)
        boolean hasCapture = false;
        for (Move m : moves) {
            if (m.isCapture() && m.getToRow() == 3 && m.getToCol() == 2) {
                hasCapture = true;
                break;
            }
        }
        assertTrue(hasCapture, "Phải có nước ăn đến (3,2)");
    }

    @Test
    @DisplayName("UC1.12.2 - Bắt buộc ăn: nếu có capture, không trả về normal move")
    void testCapturePriority() {
        // Tạo bàn cờ vừa có normal move vừa có capture
        Board testBoard = new Board(new Piece[8][8]);
        testBoard.setPiece(5, 0, new Piece(true));  // quân trắng
        testBoard.setPiece(4, 1, new Piece(false)); // quân đen để ăn
        // Ô (4,1) có quân đen, nhưng (5,0) vẫn có thể đi (4,1) bình thường không?
        // Không vì (4,1) có quân, nên normal move không hợp lệ
        // Thêm một ô trống khác: (6,1)??? Không, quân trắng phải đi lên
        // Chỉnh lại: thêm quân trắng thứ 2 có thể đi normal
        testBoard.setPiece(6, 1, new Piece(true));  // quân trắng thứ 2
        
        GameController gc = new GameController(testBoard, true);
        
        // Quân trắng ở (5,0) có thể ăn quân đen ở (4,1)
        List<Move> moves1 = gc.getValidMoves(5, 0);
        assertFalse(moves1.isEmpty(), "Phải có nước ăn");
        assertTrue(moves1.get(0).isCapture(), "Nước đi phải là capture");
        
        // Quân trắng ở (6,1) có thể đi normal
        List<Move> moves2 = gc.getValidMoves(6, 1);
        assertFalse(moves2.isEmpty(), "Phải có nước đi");
    }

    @Test
    @DisplayName("UC1.12.3 - Không ăn quân cùng màu (có normal move nhưng không capture)")
    void testNoFriendlyCapture() {
        // Setup: quân trắng có thể đi normal đến ô trống, nhưng không được ăn đồng minh
        Board testBoard = new Board(new Piece[8][8]);
        testBoard.setPiece(6, 1, new Piece(true));   // quân trắng
        testBoard.setPiece(5, 0, new Piece(true));   // đồng minh kề (cùng màu) - không được ăn
        // Ô (5,2) trống -> quân trắng ở (6,1) có thể đi normal đến (5,2)
        GameController gc = new GameController(testBoard, true);
        
        List<Move> moves = gc.getValidMoves(6, 1);
        assertFalse(moves.isEmpty(), "Quân trắng phải có nước đi normal");
        
        // Quân trắng ở (6,1) có normal move đến (5,2) và (5,0)
        // (5,0) có đồng minh -> không normal
        // (5,2) trống -> có normal move
        // Không có capture vì (5,0) là đồng minh, không thể ăn
        for (Move m : moves) {
            assertFalse(m.isCapture(), "Không thể ăn quân cùng màu");
            assertTrue(m.getToRow() == 5, "Nước đi phải đến hàng 5");
        }
    }

    @Test
    @DisplayName("UC1.12.4 - applyMove với capture")
    void testApplyMoveWithCapture() {
        Board b = new Board();
        // Xóa 2 hàng đầu và đặt quân trắng + đen
        b.clearCell(5, 0);
        b.clearCell(5, 2);
        b.clearCell(5, 4);
        b.clearCell(5, 6);
        // Xóa hết quân đen hàng 0-2
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 8; c++) {
                if ((r + c) % 2 == 1) b.clearCell(r, c);
            }
        }
        
        b.setPiece(5, 0, new Piece(true));   // quân trắng tấn công
        b.setPiece(4, 1, new Piece(false));  // quân đen bị ăn
        
        Move captureMove = new Move(5, 0, 3, 2);
        captureMove.addCapture(4, 1);
        
        GameController.applyMove(b, captureMove);
        
        assertNull(b.getPiece(5, 0), "Ô nguồn phải trống");
        assertNull(b.getPiece(4, 1), "Ô bị ăn phải trống");
        assertNotNull(b.getPiece(3, 2), "Ô đích phải có quân");
        assertTrue(b.getPiece(3, 2).isWhite, "Quân ở ô đích là trắng");
    }

    // ========================================================================
    // UC1.13 - Xóa quân bị ăn
    // ========================================================================

    @Test
    @DisplayName("UC1.13.1 - applyMove xóa quân ở ô nguồn")
    void testApplyMoveClearsSource() {
        Board b = new Board();
        Move move = new Move(5, 0, 4, 1);
        GameController.applyMove(b, move);
        assertNull(b.getPiece(5, 0), "Ô nguồn phải được xóa");
    }

    @Test
    @DisplayName("UC1.13.2 - applyMove xóa quân bị ăn")
    void testApplyMoveClearsCaptured() {
        Board b = makeCaptureBoard();
        // Đã tạo sẵn tình huống ăn quân
        
        List<Move> moves = controller.getValidMoves(5, 0);
        // Kiểm tra có capture
        // Nhưng controller đang dùng board riêng, ta tạo mới
        Move captureMove = new Move(5, 0, 3, 2);
        captureMove.addCapture(4, 1);
        
        GameController.applyMove(b, captureMove);
        
        assertNull(b.getPiece(4, 1), "Quân bị ăn phải được xóa");
    }

    private Board makeCaptureBoard() {
        Board b = new Board();
        // Xóa hết và tạo tình huống ăn
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                b.clearCell(r, c);
            }
        }
        b.setPiece(5, 0, new Piece(true));   // quân trắng
        b.setPiece(4, 1, new Piece(false));  // quân đen để ăn
        return b;
    }

    @Test
    @DisplayName("UC1.13.3 - clearCell xóa quân khỏi ô applyMove")
    void testClearCellInApplyMove() {
        Board b = new Board();
        Move move = new Move(5, 0, 4, 1);
        GameController.applyMove(b, move);
        
        // clearCell đã được gọi bên trong applyMove
        assertNull(b.getPiece(5, 0));
    }

    // ========================================================================
    // UC1.14 - Kiểm tra chuỗi ăn tiếp theo
    // ========================================================================

    @Test
    @DisplayName("UC1.14.1 - Ăn liên tiếp 2 quân (double jump)")
    void testDoubleJump() {
        // Tạo tình huống ăn 2 quân liên tiếp
        Board b = new Board(new Piece[8][8]);
        // Xếp quân: trắng ở (5,0) -> ăn đen (4,1) -> đáp (3,2) -> ăn đen (2,3) -> đáp (1,4)
        b.setPiece(5, 0, new Piece(true));   // quân trắng
        b.setPiece(4, 1, new Piece(false));  // quân đen 1
        b.setPiece(2, 3, new Piece(false));  // quân đen 2
        
        GameController gc = new GameController(b, true);
        List<Move> moves = gc.getValidMoves(5, 0);
        
        // Cần ít nhất 1 nước capture
        assertFalse(moves.isEmpty(), "Phải có nước ăn");
        
        // Kiểm tra có nước ăn 2 quân
        boolean hasDoubleJump = false;
        for (Move m : moves) {
            if (m.captures.size() == 2) {
                hasDoubleJump = true;
                assertEquals(3, m.path.size(), 
                        "Double jump path phải có 3 điểm");
                break;
            }
        }
        assertTrue(hasDoubleJump, "Phải có nước ăn 2 quân liên tiếp");
    }

    @Test
    @DisplayName("UC1.14.2 - Ăn liên tiếp 1 quân (single jump)")
    void testSingleJump() {
        Board b = new Board(new Piece[8][8]);
        b.setPiece(5, 0, new Piece(true));   // quân trắng
        b.setPiece(4, 1, new Piece(false));  // quân đen (chỉ ăn được 1)
        
        GameController gc = new GameController(b, true);
        List<Move> moves = gc.getValidMoves(5, 0);
        
        assertFalse(moves.isEmpty(), "Phải có nước ăn");
        
        boolean hasSingleJump = false;
        for (Move m : moves) {
            if (m.captures.size() == 1) {
                hasSingleJump = true;
                assertEquals(2, m.path.size(), 
                        "Single jump path phải có 2 điểm");
                break;
            }
        }
        assertTrue(hasSingleJump, "Phải có nước ăn 1 quân");
    }

    @Test
    @DisplayName("UC1.14.3 - Không ăn được khi không có quân đối phương")
    void testNoCaptureWhenNoEnemy() {
        Board b = new Board(new Piece[8][8]);
        b.setPiece(5, 0, new Piece(true));   // quân trắng
        // Không có quân đen
        
        GameController gc = new GameController(b, true);
        List<Move> moves = gc.getValidMoves(5, 0);
        
        for (Move m : moves) {
            assertFalse(m.isCapture(), "Không có quân đen để ăn");
        }
    }

    // ========================================================================
    // UC1.15 - Phong cấp vua (liên quan UC1.2)
    // ========================================================================

    @Test
    @DisplayName("UC1.15.1 - Quân trắng đến hàng 0 thành vua")
    void testWhitePromotion() {
        Board b = new Board(new Piece[8][8]);
        b.setPiece(1, 0, new Piece(true));  // quân trắng trước hàng cuối
        
        Move move = new Move(1, 0, 0, 1);
        GameController.applyMove(b, move);
        
        Piece p = b.getPiece(0, 1);
        assertTrue(p.isKing, "Quân trắng đến hàng 0 phải thành vua");
    }

    @Test
    @DisplayName("UC1.15.2 - Quân đen đến hàng 7 thành vua")
    void testBlackPromotion() {
        Board b = new Board(new Piece[8][8]);
        b.setPiece(6, 1, new Piece(false)); // quân đen trước hàng cuối
        
        Move move = new Move(6, 1, 7, 0);
        GameController.applyMove(b, move);
        
        Piece p = b.getPiece(7, 0);
        assertTrue(p.isKing, "Quân đen đến hàng 7 phải thành vua");
    }

    // ========================================================================
    // getAllMoves tests
    // ========================================================================

    @Test
    @DisplayName("UC1.6.1 - getAllMoves(true) trả về tất cả nước đi của quân trắng")
    void testGetAllMovesWhite() {
        List<Move> allMoves = controller.getAllMoves(true);
        assertFalse(allMoves.isEmpty(), "Quân trắng phải có nước đi");
        // Ban đầu 12 quân trắng, mỗi quân có 1-2 nước đi
        // Quân ở biên chỉ có 1 nước, quân trong có 2 nước
        assertTrue(allMoves.size() >= 7, 
                "Phải có ít nhất 7 nước đi cho quân trắng");
    }

    @Test
    @DisplayName("UC1.6.2 - Hàm getBoard trả về board hiện tại")
    void testGetBoard() {
        assertSame(board, controller.getBoard(), 
                "getBoard phải trả về board hiện tại");
    }

    // ========================================================================
    // Test tổng hợp - Luồng chính của game
    // ========================================================================

    @Test
    @DisplayName("UC1.1 + UC1.2 + UC1.10 - Luồng chơi cơ bản")
    void testBasicGameFlow() {
        // 1. Khởi tạo game (UC1.1)
        Board b = new Board();
        GameController gc = new GameController(b, true);
        
        // 2. Chọn quân và xem nước đi (UC1.10)
        List<Move> moves = gc.getValidMoves(5, 0);
        assertFalse(moves.isEmpty());
        
        // 3. Di chuyển (UC1.2)
        Move firstMove = moves.get(0);
        gc.makeMove(firstMove);
        assertFalse(gc.isWhiteTurn());
        
        // 4. Lượt đen
        List<Move> blackMoves = gc.getAllMoves(false);
        assertFalse(blackMoves.isEmpty());
        
        // 5. Di chuyển quân đen
        gc.makeMove(blackMoves.get(0));
        assertTrue(gc.isWhiteTurn());
    }
}
