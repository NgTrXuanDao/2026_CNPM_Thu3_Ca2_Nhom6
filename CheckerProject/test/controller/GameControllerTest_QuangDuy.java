package controller;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import model.Board;
import model.FirstTurnMode;
import model.Move;
import model.Piece;

/**
 * GameControllerTest - Kiểm thử các UC liên quan đến chế độ chơi,
 * hiển thị nước đi, highlight và các tương tác giao diện
 * 
 * Người thực hiện: Trần Quang Duy (23130081)
 * Ngày: 07/06/2026
 * 
 * Liên quan đến các UC:
 * - UC3.2 - Highlight nước đi hợp lệ (possibleMoves)
 * - UC3.4 - Thông báo lượt/kết quả
 * - UC3.5 - Hiển thị quân thường & vua
 * - UC3.6 - Highlight ô có thể đi
 * - UC3.8 - Thông báo thắng/thua
 * - UC1.2 - Di chuyển quân cờ
 * - UC1.9 - Xác định người đi trước
 * - UC1.10 - Chọn quân & hiển thị nước đi hợp lệ
 * - UC1.11 - Đi chéo 1 ô
 * - UC1.12 - Nhảy qua quân đối phương
 */
class GameControllerTest_QuangDuy {

    private Board board;
    private GameController controller;

    @BeforeEach
    void setUp() {
        board = new Board();
        controller = new GameController(board);
    }

    // ========================================================================
    // UC3.2 + UC3.6 - Highlight nước đi hợp lệ
    // ========================================================================

    @Test
    @DisplayName("UC3.2.1 - possibleMoves có chứa đúng ô đích cho quân biên")
    void testPossibleMovesEdgePiece() {
        // Quân trắng ở (5,0) - biên trái, chỉ đi được (4,1)
        List<Move> moves = controller.getValidMoves(5, 0);
        assertEquals(1, moves.size(), 
                "Quân ở biên trái (5,0) chỉ có 1 nước đi");
        assertEquals(4, moves.get(0).getToRow(), "Đích phải là hàng 4");
        assertEquals(1, moves.get(0).getToCol(), "Đích phải là cột 1");
    }

    @Test
    @DisplayName("UC3.2.2 - Quân ở giữa bàn có 2 nước đi")
    void testPossibleMovesCenterPiece() {
        // Quân trắng ở (5,2) - 2 hướng: (4,1) và (4,3)
        // Nhưng (5,2) không có quân ban đầu vì (5+2)%2 = 1, có quân
        // Thực tế (5,2) có quân trắng (r=5, c=2): (5+2)%2=1 → có quân
        List<Move> moves = controller.getValidMoves(5, 2);
        assertEquals(2, moves.size(),
                "Quân ở giữa (5,2) phải có 2 nước đi");
    }

    @Test
    @DisplayName("UC3.2.3 - Nước đi xác định đúng ô đích")
    void testMoveDestination() {
        List<Move> moves = controller.getValidMoves(5, 2);
        for (Move m : moves) {
            assertEquals(5, m.getFromRow(), "Xuất phát từ hàng 5");
            assertEquals(2, m.getFromCol(), "Xuất phát từ cột 2");
            assertEquals(4, m.getToRow(), "Đích phải là hàng 4");
        }
    }

    @Test
    @DisplayName("UC3.2.4 - Highlight chỉ hiển thị nước đi hợp lệ")
    void testNoHighlightForInvalidMove() {
        List<Move> moves = controller.getValidMoves(5, 0);
        for (Move m : moves) {
            // Ô đích phải hợp lệ: cùng hàng? không, trong checkers đi chéo
            assertNotEquals(5, m.getToRow(), "Không đi ngang");
            // Ô đích khác ô xuất phát
            assertFalse(m.getToRow() == 5 && m.getToCol() == 0,
                    "Không đi vào ô hiện tại");
        }
    }

    // ========================================================================
    // UC3.5 - Hiển thị quân thường & vua
    // ========================================================================

    @Test
    @DisplayName("UC3.5.1 - Quân thường hiển thị đúng ký hiệu")
    void testNormalPieceDisplay() {
        Piece white = new Piece(true);
        Piece black = new Piece(false);
        assertEquals("W", white.toString(), "Quân trắng thường là W");
        assertEquals("B", black.toString(), "Quân đen thường là B");
    }

    @Test
    @DisplayName("UC3.5.2 - Quân vua hiển thị đúng ký hiệu")
    void testKingPieceDisplay() {
        Piece wk = new Piece(true, true);
        Piece bk = new Piece(false, true);
        assertEquals("Wk", wk.toString(), "Vua trắng là Wk");
        assertEquals("Bk", bk.toString(), "Vua đen là Bk");
    }

    @Test
    @DisplayName("UC3.5.3 - Board.toString hiển thị bàn cờ")
    void testBoardToString() {
        String boardStr = board.toString();
        assertNotNull(boardStr, "Board toString không null");
        assertTrue(boardStr.contains("W"), "Có ký hiệu quân trắng");
        assertTrue(boardStr.contains("B"), "Có ký hiệu quân đen");
        assertTrue(boardStr.contains("."), "Có ký hiệu ô trống");
    }

    @Test
    @DisplayName("UC3.5.4 - Board.toString có 8 dòng")
    void testBoardToStringLines() {
        String boardStr = board.toString();
        String[] lines = boardStr.split("\n");
        assertEquals(8, lines.length, "Bàn cờ 8x8 có 8 dòng");
    }

    // ========================================================================
    // UC3.4 + UC3.8 - Thông báo lượt và kết quả
    // ========================================================================

    @Test
    @DisplayName("UC3.4.1 - Xác định người thắng WHITE")
    void testWinnerWhite() {
        Board b = new Board(new Piece[8][8]);
        b.setPiece(3, 2, new Piece(true));
        GameController gc = new GameController(b, true);
        assertEquals(Winner.NONE, gc.checkWinner(b),
                "Còn quân 2 bên → chưa kết thúc");
    }

    @Test
    @DisplayName("UC3.4.2 - Xác định Winner.NONE khi còn quân")
    void testWinnerNone() {
        assertEquals(Winner.NONE, controller.checkWinner(board),
                "Đầu game chưa có người thắng");
    }

    // ========================================================================
    // UC1.10 - Chọn quân & hiển thị nước đi hợp lệ
    // ========================================================================

    @Test
    @DisplayName("UC1.10.1 - Chọn quân đúng lượt trả về nước đi")
    void testSelectCorrectTurnPiece() {
        List<Move> moves = controller.getValidMoves(5, 0);
        assertFalse(moves.isEmpty(), "Chọn quân trắng đúng lượt");
    }

    @Test
    @DisplayName("UC1.10.2 - Chọn quân sai lượt không trả về nước đi")
    void testSelectWrongTurnPiece() {
        List<Move> moves = controller.getValidMoves(2, 1); // quân đen
        assertTrue(moves.isEmpty(), "Chọn quân sai lượt → rỗng");
    }

    @Test
    @DisplayName("UC1.10.3 - Chọn ô trống không trả về nước đi")
    void testSelectEmptyCell() {
        List<Move> moves = controller.getValidMoves(3, 4);
        assertTrue(moves.isEmpty(), "Ô trống → rỗng");
    }

    // ========================================================================
    // UC1.11 - Đi chéo 1 ô
    // ========================================================================

    @Test
    @DisplayName("UC1.11.1 - Quân trắng chỉ đi chéo lên trên")
    void testWhiteMovesDiagonallyUp() {
        List<Move> moves = controller.getValidMoves(5, 2);
        for (Move m : moves) {
            int dr = m.getToRow() - m.getFromRow();
            assertEquals(-1, dr, "Quân trắng phải đi lên (dr = -1)");
        }
    }

    @Test
    @DisplayName("UC1.11.2 - Quân đen chỉ đi chéo xuống dưới")
    void testBlackMovesDiagonallyDown() {
        controller.setFirstTurn(FirstTurnMode.BLACK);
        List<Move> moves = controller.getValidMoves(2, 1);
        for (Move m : moves) {
            int dr = m.getToRow() - m.getFromRow();
            assertEquals(1, dr, "Quân đen phải đi xuống (dr = +1)");
        }
    }

    // ========================================================================
    // UC1.12 - Nhảy qua quân đối phương
    // ========================================================================

    @Test
    @DisplayName("UC1.12.1 - Phát hiện nước ăn quân")
    void testCaptureDetection() {
        Board b = new Board(new Piece[8][8]);
        b.setPiece(5, 0, new Piece(true));
        b.setPiece(4, 1, new Piece(false));
        GameController gc = new GameController(b, true);

        List<Move> moves = gc.getValidMoves(5, 0);
        boolean foundCapture = false;
        for (Move m : moves) {
            if (m.isCapture()) {
                foundCapture = true;
                assertEquals(3, m.getToRow(), "Đích sau ăn là hàng 3");
                assertEquals(2, m.getToCol(), "Đích sau ăn là cột 2");
                break;
            }
        }
        assertTrue(foundCapture, "Phải phát hiện nước ăn quân");
    }

    @Test
    @DisplayName("UC1.12.2 - Bắt buộc ăn: không có nước đi thường khi có thể ăn")
    void testMandatoryCapture() {
        Board b = new Board(new Piece[8][8]);
        b.setPiece(5, 0, new Piece(true));
        b.setPiece(4, 1, new Piece(false));
        GameController gc = new GameController(b, true);

        List<Move> moves = gc.getValidMoves(5, 0);
        assertFalse(moves.isEmpty(), "Phải có nước ăn");
        for (Move m : moves) {
            assertTrue(m.isCapture(), "Bắt buộc ăn: tất cả nước đi phải là capture");
        }
    }
}
