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
 * GameControllerTest - Kiểm thử các UC liên quan đến kiểm tra trạng thái,
 * kết thúc game và xác định người thắng
 * 
 * Người thực hiện: Nguyễn Khánh Duy (23130076)
 * Ngày: 07/06/2026
 * 
 * Liên quan đến các UC:
 * - UC1.6 - Kiểm tra trạng thái (checkWinner, isOver)
 * - UC1.17 - Hết quân → thua (hasPieces, countPieces)
 * - UC1.18 - Hết nước đi → thua (getAllMoves check)
 * - UC1.2 - Di chuyển quân cờ (làm nền cho trạng thái)
 * - UC1.10 - Chọn quân & hiển thị nước đi hợp lệ (getAllMoves)
 * - UC1.9 - Xác định người đi trước
 */
class GameControllerTest_KhanhDuy {

    private Board board;
    private GameController controller;

    @BeforeEach
    void setUp() {
        board = new Board();
        controller = new GameController(board);
    }

    // ========================================================================
    // UC1.6 - Kiểm tra trạng thái
    // ========================================================================

    @Test
    @DisplayName("UC1.6.1 - checkWinner trả về NONE khi game đang chơi")
    void testCheckWinnerNone() {
        assertEquals(Winner.NONE, controller.checkWinner(board),
                "Đầu game chưa có ai thắng");
    }

    @Test
    @DisplayName("UC1.6.2 - isOver trả về false khi game đang chơi")
    void testIsOverFalse() {
        assertFalse(controller.isOver(), "Đầu game chưa kết thúc");
    }

    @Test
    @DisplayName("UC1.6.3 - checkWinner sau 1 nước đi vẫn NONE")
    void testCheckWinnerAfterOneMove() {
        controller.makeMove(new Move(5, 0, 4, 1));
        assertEquals(Winner.NONE, controller.checkWinner(board),
                "Sau 1 nước vẫn chưa kết thúc");
    }

    // ========================================================================
    // UC1.17 - Hết quân → thua
    // ========================================================================

    @Test
    @DisplayName("UC1.17.1 - hasPieces(true) trả về true khi còn quân trắng")
    void testHasPiecesWhite() {
        assertTrue(controller.hasPieces(true), "Đầu game còn quân trắng");
    }

    @Test
    @DisplayName("UC1.17.2 - hasPieces(false) trả về true khi còn quân đen")
    void testHasPiecesBlack() {
        assertTrue(controller.hasPieces(false), "Đầu game còn quân đen");
    }

    @Test
    @DisplayName("UC1.17.3 - checkWinner = WHITE khi hết quân đen")
    void testCheckWinnerWhiteWhenBlackGone() {
        // Xóa hết quân đen
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 8; c++) {
                if ((r + c) % 2 == 1) board.clearCell(r, c);
            }
        }
        assertEquals(Winner.WHITE, controller.checkWinner(board),
                "Hết quân đen → Trắng thắng");
    }

    @Test
    @DisplayName("UC1.17.4 - checkWinner = BLACK khi hết quân trắng")
    void testCheckWinnerBlackWhenWhiteGone() {
        // Xóa hết quân trắng
        for (int r = 5; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if ((r + c) % 2 == 1) board.clearCell(r, c);
            }
        }
        assertEquals(Winner.BLACK, controller.checkWinner(board),
                "Hết quân trắng → Đen thắng");
    }

    @Test
    @DisplayName("UC1.17.5 - hasPieces(false) trả về false khi hết quân đen")
    void testHasPiecesBlackFalse() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 8; c++) {
                if ((r + c) % 2 == 1) board.clearCell(r, c);
            }
        }
        assertFalse(controller.hasPieces(false), "Hết quân đen");
    }

    // ========================================================================
    // UC1.18 - Hết nước đi → thua
    // ========================================================================

    @Test
    @DisplayName("UC1.18.1 - getAllMoves(true) trả về nước đi cho quân trắng")
    void testGetAllMovesWhite() {
        List<Move> whiteMoves = controller.getAllMoves(true);
        assertFalse(whiteMoves.isEmpty(), "Quân trắng phải có nước đi đầu game");
    }

    @Test
    @DisplayName("UC1.18.2 - getAllMoves(false) trả về nước đi cho quân đen")
    void testGetAllMovesBlack() {
        List<Move> blackMoves = controller.getAllMoves(false);
        assertFalse(blackMoves.isEmpty(), "Quân đen phải có nước đi đầu game");
    }

    @Test
    @DisplayName("UC1.18.3 - getAllMoves trả về rỗng khi không còn quân")
    void testGetAllMovesEmptyWhenNoPieces() {
        // Xóa hết quân trắng
        for (int r = 5; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                board.clearCell(r, c);
            }
        }
        // Chú ý: getAllMoves chỉ trả về nước đi của bên được chỉ định
        List<Move> whiteMoves = controller.getAllMoves(true);
        assertTrue(whiteMoves.isEmpty(), "Hết quân trắng → không có nước đi");
    }

    @Test
    @DisplayName("UC1.18.4 - isOver trả về true khi hết quân đen")
    void testIsOverWhenBlackGone() {
        // Xóa hết quân đen
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 8; c++) {
                if ((r + c) % 2 == 1) board.clearCell(r, c);
            }
        }
        assertTrue(controller.isOver(), "Hết quân đen → game over");
    }

    @Test
    @DisplayName("UC1.18.5 - isOver trả về true khi hết quân trắng")
    void testIsOverWhenWhiteGone() {
        for (int r = 5; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                board.clearCell(r, c);
            }
        }
        assertTrue(controller.isOver(), "Hết quân trắng → game over");
    }

    // ========================================================================
    // Kiểm thử tích hợp - Kết thúc game
    // ========================================================================

    @Test
    @DisplayName("Tích hợp - Ăn hết quân đối phương dẫn đến thắng")
    void testIntegrationEatAllPieces() {
        // Tạo board chỉ có 1 quân trắng và 1 quân đen có thể ăn
        Board b = new Board(new Piece[8][8]);
        b.setPiece(5, 0, new Piece(true));   // quân trắng
        b.setPiece(4, 1, new Piece(false));  // quân đen để ăn

        GameController gc = new GameController(b, true);

        // Ăn quân đen
        Move captureMove = new Move(5, 0, 3, 2);
        captureMove.addCapture(4, 1);
        gc.makeMove(captureMove);

        // Quân đen hết → Trắng thắng
        Winner winner = gc.checkWinner(b);
        assertEquals(Winner.WHITE, winner,
                "Hết quân đen sau khi bị ăn → Trắng thắng");
        assertTrue(gc.isOver(), "Game phải kết thúc");
    }

    @Test
    @DisplayName("Tích hợp - checkWinner không đổi sai lượt")
    void testIntegrationCheckWinnerCorrectTurn() {
        // Xóa quân đen nhưng đang là lượt trắng
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 8; c++) {
                if ((r + c) % 2 == 1) board.clearCell(r, c);
            }
        }
        // checkWinner không phụ thuộc lượt cho trường hợp hết quân
        assertEquals(Winner.WHITE, controller.checkWinner(board),
                "Hết quân đen → Trắng thắng (không phụ thuộc lượt)");
    }
}
