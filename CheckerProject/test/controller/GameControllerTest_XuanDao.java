package controller;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import model.Board;
import model.FirstTurnMode;
import model.GameState;
import model.Move;
import model.Piece;

/**
 * GameControllerTest - Kiểm thử các UC liên quan đến Phong cấp Vua, 
 * Board Copy, GameState và luồng chơi tổng thể
 * 
 * Người thực hiện: Nguyễn Trần Xuân Đào (23130044)
 * Ngày: 07/06/2026
 * 
 * Liên quan đến các UC:
 * - UC1.5 - Phong cấp - vua
 * - UC1.15 - Hàng cuối → phong vua
 * - UC6.1 - Tính toán nước đi (Board.copy phục vụ AI)
 * - UC6.2 - Đánh giá bàn cờ
 * - UC6.5 - Đếm số quân & vua
 * - UC7.1 - Lưu trạng thái game (GameState)
 * - UC7.2 - Tải trạng thái game (GameState.toBoard)
 * - UC1.1 - Khởi tạo ván chơi
 * - UC1.2 - Di chuyển quân cờ
 */
class GameControllerTest_XuanDao {

    private Board board;
    private GameController controller;

    @BeforeEach
    void setUp() {
        board = new Board();
        controller = new GameController(board);
    }

    // ========================================================================
    // UC1.5 + UC1.15 - Phong cấp vua
    // ========================================================================

    @Test
    @DisplayName("UC1.5.1 + UC1.15.1 - Quân trắng đến hàng 0 thành vua")
    void testWhitePromotionToKing() {
        Board b = new Board(new Piece[8][8]);
        b.setPiece(1, 0, new Piece(true));

        Move move = new Move(1, 0, 0, 1);
        GameController.applyMove(b, move);

        Piece placed = b.getPiece(0, 1);
        assertNotNull(placed, "Phải có quân ở ô đích");
        assertTrue(placed.isKing, "Quân trắng đến hàng 0 phải thành vua");
        assertTrue(placed.isWhite, "Quân phải là trắng");
    }

    @Test
    @DisplayName("UC1.5.2 + UC1.15.2 - Quân đen đến hàng 7 thành vua")
    void testBlackPromotionToKing() {
        Board b = new Board(new Piece[8][8]);
        b.setPiece(6, 1, new Piece(false));

        Move move = new Move(6, 1, 7, 0);
        GameController.applyMove(b, move);

        Piece placed = b.getPiece(7, 0);
        assertNotNull(placed, "Phải có quân ở ô đích");
        assertTrue(placed.isKing, "Quân đen đến hàng 7 phải thành vua");
        assertFalse(placed.isWhite, "Quân phải là đen");
    }

    @Test
    @DisplayName("UC1.5.3 - Vua có thể đi 4 hướng sau khi phong cấp")
    void testKingCanMoveAllDirections() {
        Board b = new Board(new Piece[8][8]);
        b.setPiece(4, 3, new Piece(true, true)); // vua trắng
        GameController gc = new GameController(b, true);

        List<Move> moves = gc.getValidMoves(4, 3);
        assertFalse(moves.isEmpty(), "Vua phải có nước đi");

        boolean hasUp = false, hasDown = false, hasLeft = false, hasRight = false;
        for (Move m : moves) {
            if (m.getToRow() < 4) hasUp = true;
            if (m.getToRow() > 4) hasDown = true;
            if (m.getToCol() < 3) hasLeft = true;
            if (m.getToCol() > 3) hasRight = true;
        }
        assertTrue(hasUp, "Vua phải có thể đi lên");
        assertTrue(hasDown, "Vua phải có thể đi xuống");
        assertTrue(hasLeft, "Vua phải có thể đi trái");
        assertTrue(hasRight, "Vua phải có thể đi phải");
    }

    @Test
    @DisplayName("UC1.5.4 - Vua không được tạo từ constructor mặc định")
    void testDefaultPieceNotKing() {
        Piece white = new Piece(true);
        Piece black = new Piece(false);
        assertFalse(white.isKing, "Quân trắng mới không là vua");
        assertFalse(black.isKing, "Quân đen mới không là vua");
    }

    // ========================================================================
    // UC6.1 + UC6.2 - Board.copy phục vụ AI & đánh giá bàn cờ
    // ========================================================================

    @Test
    @DisplayName("UC6.1.1 - Board.copy tạo bản sao độc lập (deep copy)")
    void testBoardCopyIsIndependent() {
        Board original = new Board();
        Board copy = original.copy();

        // Thay đổi bản gốc không ảnh hưởng bản sao
        original.clearCell(5, 0);
        assertNull(original.getPiece(5, 0), "Ô gốc đã xóa");
        assertNotNull(copy.getPiece(5, 0), "Ô sao không bị ảnh hưởng");

        // Thay đổi bản sao không ảnh hưởng bản gốc
        copy.clearCell(2, 1);
        assertNull(copy.getPiece(2, 1), "Ô sao đã xóa");
        assertNotNull(original.getPiece(2, 1), "Ô gốc không bị ảnh hưởng");
    }

    @Test
    @DisplayName("UC6.1.2 - Board.copy giữ đúng số lượng quân")
    void testBoardCopyKeepsPieceCount() {
        Board original = new Board();
        Board copy = original.copy();

        assertEquals(original.countWhitePieces(), copy.countWhitePieces(),
                "Số quân trắng phải bằng nhau");
        assertEquals(original.countBlackPieces(), copy.countBlackPieces(),
                "Số quân đen phải bằng nhau");
    }

    // ========================================================================
    // UC6.5 - Đếm số quân & vua
    // ========================================================================

    @Test
    @DisplayName("UC6.5.1 - countWhitePieces đếm đúng 12 quân ban đầu")
    void testCountWhitePiecesInitial() {
        assertEquals(12, board.countWhitePieces(), "Ban đầu có 12 quân trắng");
    }

    @Test
    @DisplayName("UC6.5.2 - countBlackPieces đếm đúng 12 quân ban đầu")
    void testCountBlackPiecesInitial() {
        assertEquals(12, board.countBlackPieces(), "Ban đầu có 12 quân đen");
    }

    @Test
    @DisplayName("UC6.5.3 - Đếm quân sau khi di chuyển")
    void testCountPiecesAfterMove() {
        controller.makeMove(new Move(5, 0, 4, 1));
        assertEquals(12, controller.getBoard().countWhitePieces(),
                "Vẫn 12 quân trắng sau di chuyển thường");
        assertEquals(12, controller.getBoard().countBlackPieces(),
                "Vẫn 12 quân đen sau di chuyển thường");
    }

    // ========================================================================
    // UC7.1 + UC7.2 - GameState (Lưu & Tải trạng thái)
    // ========================================================================

    @Test
    @DisplayName("UC7.1.1 - GameState lưu đúng số lượng quân")
    void testGameStatePreservesPieceCount() {
        GameState state = new GameState(true, board, List.of());
        Board restored = state.toBoard();

        assertEquals(12, restored.countWhitePieces(), 
                "Số quân trắng sau lưu/tải");
        assertEquals(12, restored.countBlackPieces(),
                "Số quân đen sau lưu/tải");
    }

    @Test
    @DisplayName("UC7.1.2 - GameState lưu đúng lượt đi")
    void testGameStatePreservesTurn() {
        GameState state = new GameState(true, board, List.of());
        assertTrue(state.whiteTurn, "Lượt trắng được lưu");

        state = new GameState(false, board, List.of());
        assertFalse(state.whiteTurn, "Lượt đen được lưu");
    }

    @Test
    @DisplayName("UC7.2.1 - GameState.toBoard khôi phục đúng quân sau di chuyển")
    void testGameStateRestoreAfterMove() {
        controller.makeMove(new Move(5, 0, 4, 1));
        GameState state = new GameState(controller.isWhiteTurn(), 
                controller.getBoard(), List.of());
        Board restored = state.toBoard();

        assertNull(restored.getPiece(5, 0), "Ô nguồn trống sau restore");
        assertNotNull(restored.getPiece(4, 1), "Ô đích có quân sau restore");
        assertEquals(12, restored.countWhitePieces());
    }

    @Test
    @DisplayName("UC7.1.3 - GameState.isVersionCompatible kiểm tra version")
    void testGameStateVersion() {
        GameState state = new GameState(true, board, List.of());
        assertTrue(state.isVersionCompatible(), 
                "Version hiện tại phải tương thích");
    }

    @Test
    @DisplayName("UC7.1.4 - GameState lưu lịch sử nước đi")
    void testGameStateMoveHistory() {
        GameState state = new GameState(true, board, 
                List.of("1. W: (5,0)→(4,1)", "2. B: (2,1)→(3,0)"));
        assertNotNull(state.moveHistory);
        assertEquals(2, state.moveHistory.size());
    }

    // ========================================================================
    // Kiểm thử tích hợp - Luồng chơi có phong cấp
    // ========================================================================

    @Test
    @DisplayName("Tích hợp - Di chuyển quân trắng đến hàng cuối và phong vua")
    void testIntegrationPromotionFlow() {
        Board b = new Board(new Piece[8][8]);
        b.setPiece(1, 0, new Piece(true));    // quân trắng
        b.setPiece(2, 1, new Piece(false));   // quân đen

        GameController gc = new GameController(b, true);

        // Di chuyển lên đến hàng cuối
        gc.makeMove(new Move(1, 0, 0, 1));

        Piece king = b.getPiece(0, 1);
        assertNotNull(king, "Phải có quân ở (0,1)");
        assertTrue(king.isKing, "Quân trắng ở hàng 0 phải là vua");
        assertTrue(king.isWhite, "Quân phải là trắng");
    }

    @Test
    @DisplayName("Tích hợp - Vua đen có thể đi lùi")
    void testIntegrationKingBackwardMove() {
        Board b = new Board(new Piece[8][8]);
        b.setPiece(3, 2, new Piece(false, true)); // vua đen

        GameController gc = new GameController(b, false);

        // Vua đen có thể đi lên (hàng 2) - đi lùi so với hướng tiến mặc định
        List<Move> moves = gc.getValidMoves(3, 2);
        boolean canMoveUp = false;
        for (Move m : moves) {
            if (m.getToRow() < 3) {
                canMoveUp = true;
                break;
            }
        }
        assertTrue(canMoveUp, "Vua đen phải có thể đi lên (lùi)");
    }
}
