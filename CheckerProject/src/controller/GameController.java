package controller;

import model.Board;
import model.FirstTurnMode;
import model.Move;
import model.Piece;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * GameController - Điều phối trò chơi Cờ Đam
 * Người thực hiện: Đoàn Ngọc Ánh (Module 1 - Kế thừa)
 * Ngày cập nhật: 07/06/2026
 *
 * UC1.1 - Khởi tạo ván chơi: khởi tạo GameController với Board và FirstTurnMode
 * UC1.9 - Xác định người đi trước: resolveFirstTurn(), setFirstTurn(), resetGame()
 * UC1.2 - Di chuyển quân cờ: applyMove(), makeMove()
 * UC1.10 - Chọn quân & hiển thị nước đi hợp lệ: getValidMoves()
 * UC1.11 - Đi chéo 1 ô: ALL_DIRS, lọc hướng di chuyển
 * UC1.12 - Nhảy qua quân đối phương: findCaptureMoves()
 * UC1.13 - Xóa quân bị ăn: clearCell trong applyMove()
 * UC1.14 - Kiểm tra chuỗi ăn tiếp theo: findCaptureMoves() đệ quy
 *
 * US Checkers rules:
 * - piece (non-king) chỉ nhảy 2 ô chéo (forward theo màu)
 * - king có thể nhảy 4 hướng (nhưng vẫn nhảy 2 ô)
 * - bắt buộc ăn: nếu có một hay nhiều capture, chỉ phép capture.
 */
public class GameController {
    private  Board board;
    private boolean whiteTurn;

    // ============================================================
    // UC1.9 - Xác định người đi trước - Đoàn Ngọc Ánh
    // ============================================================

    /** Mặc định White đi trước */
    public GameController(Board board) {
        this.board = board;
        this.whiteTurn = true; // White bắt đầu (mặc định)
    }

    /** Khởi tạo với lượt chỉ định */
    public GameController(Board board, boolean whiteTurn) {
		this.board = board;
		this.whiteTurn = whiteTurn;
	}

    // UC1.1.6 - Hệ thống khởi tạo GameController với Board và FirstTurnMode
    // UC1.9.5 - Hệ thống khởi tạo bàn cờ với lượt đã xác định
    /*
     * Khởi tạo GameController với chế độ chọn người đi trước
     * @param board Bàn cờ
     * @param mode  Chế độ: WHITE, BLACK hoặc RANDOM
     */
    public GameController(Board board, FirstTurnMode mode) {
        this.board = board;
        // UC1.9.4: Xác định whiteTurn dựa vào mode
        this.whiteTurn = resolveFirstTurn(mode);
    }

    // UC1.9.4 - Hệ thống xác định người đi trước dựa trên lựa chọn
    // Chuyển FirstTurnMode thành boolean whiteTurn
    // - WHITE → true (White đi trước)
    // - BLACK → false (Black đi trước)
    // - RANDOM → new Random().nextBoolean() (ngẫu nhiên)
    // - null → true (fallback: White đi trước)
    public static boolean resolveFirstTurn(FirstTurnMode mode) {
        if (mode == null) return true; // UC1.9.4.3: Null safety - mặc định White
        switch (mode) {
            case WHITE:
                return true;  // White đi trước
            case BLACK:
                return false; // Black đi trước
            case RANDOM:
                // UC1.9.4.2: Random true/false - 50% cơ hội cho mỗi bên
                return new Random().nextBoolean();
            default:
                return true; // Mặc định White đi trước
        }
    }

    // UC1.9.5 - Thiết lập lại lượt đi dựa trên chế độ (dùng khi restart)
    public void setFirstTurn(FirstTurnMode mode) {
        this.whiteTurn = resolveFirstTurn(mode);
    }

    // UC1.9.5 - Reset bàn cờ và thiết lập lại lượt đi đầu tiên (dùng khi restart)
    public void resetGame(FirstTurnMode mode) {
        this.board.initialize(); // Gọi UC1.7 + UC1.8: Khởi tạo lại bàn cờ
        if (mode != null) {
            setFirstTurn(mode);  // Thiết lập lượt đi đầu
        }
    }

	public Board getBoard() { return board; }
    public boolean isWhiteTurn() { return whiteTurn; }

    // UC1.11.1 - 4 hướng chéo dùng để duyệt nước đi: (-1,-1) (-1,1) (1,-1) (1,1)
    // UC1.11.2 - Với quân thường: chỉ 2 hướng tiến; Với vua: cả 4 hướng
    private static final int[][] ALL_DIRS = new int[][] {
            {-1,-1}, {-1,1}, {1,-1}, {1,1}
    };

    // UC1.10.3 - Hệ thống gọi getValidMoves() để tính danh sách Move hợp lệ
    // - Nếu piece null hoặc không phải lượt → list rỗng
    // - Nếu có capture sequences → trả về chỉ capture sequences (ưu tiên ăn quân)
    public List<Move> getValidMoves(int r, int c) {
        List<Move> result = new ArrayList<>();
        Piece p = board.getPiece(r,c);
        // UC1.10.2: Kiểm tra quân tồn tại và thuộc đúng lượt
        if (p == null) return result;
        if (p.isWhite != whiteTurn) return result; // không phải lượt piece này

        // UC1.12.1: Tìm tất cả nước ăn quân bằng đệ quy
        List<Move> captureMoves = new ArrayList<>();
        findCaptureMoves(board.copy(), r, c, new ArrayList<>(), new ArrayList<>(), captureMoves);

        // UC1.10.4: Nếu có nước ăn, chỉ trả về nước ăn (bắt buộc ăn)
        if (!captureMoves.isEmpty()) {
            return captureMoves; // bắt buộc ăn → chỉ trả capture
        }

        // UC1.11: Nếu không có capture → tạo normal moves (đi chéo 1 ô)
        for (int[] d : ALL_DIRS) {
            // UC1.11.2: Quân thường chỉ đi tiến, vua đi được cả 4 hướng
            if (!p.isKing) {
                int forward = p.isWhite ? -1 : 1;
                if (d[0] != forward) continue; // non-king chỉ forward
            }
            
            // UC1.11.3: Tính ô đích và kiểm tra trong biên
            int nr = r + d[0], nc = c + d[1];
            if (!board.inBounds(nr,nc)) continue;
            // UC1.11.4: Nếu ô đích trống → tạo Move và thêm vào danh sách
            if (board.getPiece(nr,nc) == null) {
                Move m = new Move(r,c,nr,nc);
                result.add(m);
            }
        }
        return result;
    }

//Hỗ trợ Use case : UC1.18 - Hết nước đi → thua
    //UC1.6 - Kiểm tra trạng thái
    public List<Move> getAllMoves(boolean forWhite) {
        List<Move> all = new ArrayList<>();
        boolean oldTurn = whiteTurn;
        whiteTurn = forWhite;
        for (int r=0;r<8;r++) {
            for (int c=0;c<8;c++) {
                Piece p = board.getPiece(r,c);
                if (p != null && p.isWhite == forWhite) {
                    all.addAll(getValidMoves(r,c));
                }
            }
        }
        whiteTurn = oldTurn;
        return all;
    }

    /*
     * UC1.6 - Kiểm tra trạng thái – xác định người thắng
     * UC1.18 - Hết nước đi → thua
     * Người thực hiện: Nguyễn Khánh Duy
     * Ngày cập nhật: 04/06/2026
     * Nội dung:
     * - Kiểm tra hết quân (UC1.17): bên nào về 0 quân → thua ngay, không phụ thuộc lượt
     * - Kiểm tra hết nước đi (UC1.18): chỉ check bên đang đến lượt (whiteTurn),
     *   tránh xét thua nhầm bên chưa đến lượt
     * @param board Bàn cờ cần kiểm tra
     * @return Winner.WHITE / Winner.BLACK / Winner.NONE
     */
    public Winner checkWinner(Board board) {
        // UC1.17 - Hết quân → thua (không phụ thuộc lượt)
        if (board.countWhitePieces() == 0) return Winner.BLACK;
        if (board.countBlackPieces() == 0) return Winner.WHITE;

        // UC1.18 - Hết nước đi → thua (chỉ check bên đang đến lượt)
        // Code cũ check cả 2 bên song song → có thể xét thua nhầm bên chưa đến lượt
        if (whiteTurn && getAllMoves(true).isEmpty())  return Winner.BLACK;
        if (!whiteTurn && getAllMoves(false).isEmpty()) return Winner.WHITE;

        return Winner.NONE;
    }

    /*
     * UC1.17 - Hết quân → thua
     * UC1.6 - Kiểm tra trạng thái
     * Người thực hiện: Nguyễn Khánh Duy
     * Ngày cập nhật: 04/06/2026
     * Nội dung:
     * - Dùng countWhitePieces() / countBlackPieces() từ Board thay vì tự duyệt lại
     *   → tránh lặp logic, đảm bảo nhất quán với checkWinner()
     * @param isWhite true = kiểm tra White, false = kiểm tra Black
     * @return true nếu còn ít nhất 1 quân, false nếu hết quân
     */
    public boolean hasPieces(boolean isWhite) {
        if (isWhite) {
            return board.countWhitePieces() > 0;
        } else {
            return board.countBlackPieces() > 0;
        }
    }

    /*
     * UC1.17 - Hết quân → thua
     * UC1.18 - Hết nước đi → thua
     * UC1.6 - Kiểm tra trạng thái
     * Người thực hiện: Nguyễn Khánh Duy
     * Ngày cập nhật: 04/06/2026
     * Nội dung:
     * - Delegate sang checkWinner() để tránh lặp logic
     * - checkWinner() đã xử lý đủ: hết quân (UC1.17) + hết nước đi (UC1.18)
     * - Dùng bởi AI (miniMax, alphaBeta) để dừng đệ quy khi đến node lá
     * @return true nếu game over, false nếu vẫn đang chơi
     */
    public boolean isOver() {
        return checkWinner(this.board) != Winner.NONE;
    }

    /**
     * Áp dụng Move m lên board b.
     * - truyền Board b (có thể là copy của board thật để simulate)
     * - áp dụng path và clear các captured positions
     * NOTE: chúng ta copy piece khi đặt ở ô đích để tránh tham chiếu chéo khi simulate.
     */

    // ============================================================
    // UC1.2 - Di chuyển quân cờ - Đoàn Ngọc Ánh
    // ============================================================
    // UC1.2.7: Thực hiện di chuyển quân trên board
    public static void applyMove(Board b, Move m) {
        if (m == null) return;
        Point from = m.from();
        Point to = m.to();
        if (from == null || to == null) return;

        int fr = from.y, fc = from.x;
        int tr = to.y, tc = to.x;
        Piece p = b.getPiece(fr, fc);
        if (p == null) return;

        // UC1.13.1: Di chuyển quân từ ô nguồn sang ô đích (copy để an toàn khi simulate)
        b.setPiece(tr, tc, p.copy());
        // UC1.13.2: Xóa quân khỏi ô nguồn
        b.clearCell(fr, fc);

        // UC1.13.3: Xóa tất cả quân bị ăn trong danh sách captures
        for (Point cap : m.captures) {
            b.clearCell(cap.y, cap.x);
        }

        // promotion
        //UC1.5 - Phong cấp – vua
        Piece placed = b.getPiece(tr, tc);
        if (placed != null && !placed.isKing) {
            //UC1.15 - Quân đến hàng cuối → phong vua
            if (placed.isWhite && tr == 0) placed.isKing = true;
            if (!placed.isWhite && tr == 7) placed.isKing = true;
        }
    }

    // UC1.2.9: Áp dụng move và chuyển lượt sau khi kết thúc nước đi
    // (Được gọi sau khi hoàn tất chuỗi ăn hoặc nước đi thường)
    public void makeMove(Move m) {
        applyMove(this.board, m);
        // Chuyển lượt sang đối phương (không gọi khi đang trong chuỗi ăn liên tiếp)
        whiteTurn = !whiteTurn;
    }


    // ============================================================
    // UC1.12 - Nhảy qua quân đối phương - Đoàn Ngọc Ánh
    // UC1.14 - Kiểm tra chuỗi ăn tiếp theo (đệ quy)
    // ============================================================
    /**
     * Tìm tất cả nước ăn quân bằng đệ quy.
     * - b: board hiện tại (đã clone)
     * - curR,curC: vị trí hiện tại
     * - pathSoFar: list Point path đã đi
     * - capsSoFar: list Point các vị trí bị ăn
     * - outMoves: collect kết quả (Move)
     */
    private void findCaptureMoves(Board b, int curR, int curC,
                                  List<Point> pathSoFar, List<Point> capsSoFar,
                                  List<Move> outMoves) {
        Piece p = b.getPiece(curR, curC);
        if (p == null) return;

        List<Point> path = new ArrayList<>(pathSoFar);
        if (path.isEmpty()) path.add(new Point(curC, curR)); // start

        boolean extended = false;

        // UC1.14.2: Duyệt 4 hướng chéo để tìm nước ăn tiếp
        for (int[] d : ALL_DIRS) {
            if (!p.isKing) {
                int forward = p.isWhite ? -1 : 1;
                if (d[0] != forward) continue;
            }
           // UC1.12.1: Xác định quân đối phương ở ô kề
            int midR = curR + d[0], midC = curC + d[1];
            // UC1.12.2: Tính ô đích sau khi nhảy
            int landR = curR + 2*d[0], landC = curC + 2*d[1];

            // UC1.12.2: Kiểm tra ô trong biên
            if (!b.inBounds(midR, midC) || !b.inBounds(landR, landC)) continue;
            // UC1.12.1: Lấy quân tại ô kề (mid)
            Piece mid = b.getPiece(midR, midC);
            // UC1.12.3: Kiểm tra ô đích trống
            Piece land = b.getPiece(landR, landC); 

            // UC1.12: Kiểm tra ô kề có quân đối phương và ô đích trống
            if (mid != null && mid.isWhite != p.isWhite && land == null) {
            	// UC1.12.4: Thực hiện nước đi ăn quân trên bản sao
                // UC1.13.3: Xóa quân bị ăn (mid) khỏi bàn cờ
                // UC1.14.2: Ghi nhận và kiểm tra chuỗi ăn tiếp theo
                Board nb = b.copy();
                nb.clearCell(midR, midC);
                Piece moved = nb.getPiece(curR, curC);
                nb.clearCell(curR, curC);
                nb.setPiece(landR, landC, moved);

                List<Point> nPath = new ArrayList<>(path);
                nPath.add(new Point(landC, landR));
                List<Point> nCaps = new ArrayList<>(capsSoFar);
                nCaps.add(new Point(midC, midR)); // UC1.12.4: Ghi nhận quân bị ăn

                // UC1.14.1: Đệ quy tìm chuỗi ăn tiếp theo từ vị trí mới
                findCaptureMoves(nb, landR, landC, nPath, nCaps, outMoves);
                extended = true;
            }
        }

        // UC1.14.3: Nếu không mở rộng được và đã có captures → kết thúc chuỗi
        if (!extended && !capsSoFar.isEmpty()) {
            Move m = new Move();
            for (Point pnt : path) m.path.add(new Point(pnt.x, pnt.y));
            for (Point cap : capsSoFar) m.captures.add(new Point(cap.x, cap.y));
            outMoves.add(m);
        }
    }
}
