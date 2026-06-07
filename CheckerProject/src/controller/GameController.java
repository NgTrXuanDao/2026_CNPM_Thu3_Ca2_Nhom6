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
 * GameController: nắm Board, lượt (whiteTurn).
 * - getValidMoves(r,c): trả về tất cả moves hợp lệ cho piece tại (r,c), ưu tiên capture (multi-jump).
 * - getAllMoves(forWhite): tất cả moves cho 1 bên (dùng AI).
 * - applyMove(Board, Move): áp dụng move lên board (dùng cho simulation và thực thi).
 * - makeMove(Move): áp dụng move lên controller.board và đổi lượt.
 *
 * US Checkers rules:
 * - piece (non-king) chỉ nhảy 2 ô chéo (forward theo màu)
 * - king có thể nhảy 4 hướng (nhưng vẫn nhảy 2 ô)
 * - bắt buộc ăn: nếu có một hay nhiều capture, chỉ phép capture.
 */
public class GameController {
    private  Board board;
    private boolean whiteTurn;

    /*
     * UC1.9 - Xác định người đi trước
     * Người thực hiện: Nhóm 6
     * Ngày cập nhật: 02/06/2026
     * Nội dung:
     * - Bỏ hardcode whiteTurn = true
     * - Thêm constructor nhận FirstTurnMode
     * - Thêm method setFirstTurnFromMode() để xử lý logic chọn lượt
     */

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

    /*
     * UC1.9 - Xác định người đi trước
     * Khởi tạo GameController với chế độ chọn người đi trước
     * @param board Bàn cờ
     * @param mode  Chế độ: WHITE, BLACK hoặc RANDOM
     */
    public GameController(Board board, FirstTurnMode mode) {
        this.board = board;
        // Xác định whiteTurn dựa vào mode
        this.whiteTurn = resolveFirstTurn(mode);
    }

    /*
     * UC1.9 - Xác định người đi trước
     * Giải quyết chế độ FirstTurnMode thành giá trị boolean whiteTurn
     * @param mode Chế độ chọn người đi trước
     * @return true nếu White đi trước, false nếu Black đi trước
     */
    /*
     * UC1.9 - Xác định người đi trước
     * Chuyển FirstTurnMode thành boolean whiteTurn
     * @param mode Chế độ (có thể null -> fallback White)
     * @return true = White, false = Black
     */
    public static boolean resolveFirstTurn(FirstTurnMode mode) {
        if (mode == null) return true; // Null safety: mặc định White
        switch (mode) {
            case WHITE:
                return true;  // White đi trước
            case BLACK:
                return false; // Black đi trước
            case RANDOM:
                // Random true/false - 50% cơ hội cho mỗi bên
                return new Random().nextBoolean();
            default:
                return true; // Mặc định White đi trước
        }
    }

    /*
     * UC1.9 - Xác định người đi trước
     * Thiết lập lại lượt đi dựa trên chế độ (dùng khi restart)
     * @param mode Chế độ người đi trước
     */
    public void setFirstTurn(FirstTurnMode mode) {
        this.whiteTurn = resolveFirstTurn(mode);
    }

    /*
     * UC1.9 - Xác định người đi trước
     * Reset bàn cờ và thiết lập lại lượt đi đầu tiên
     * @param mode Chế độ người đi trước (có thể null để giữ nguyên chế độ cũ)
     */
    public void resetGame(FirstTurnMode mode) {
        this.board.initialize(); // Khởi tạo lại bàn cờ
        if (mode != null) {
            setFirstTurn(mode);  // Thiết lập lượt đi đầu
        }
    }

	public Board getBoard() { return board; }
    public boolean isWhiteTurn() { return whiteTurn; }

    // 4 hướng chéo
    //UC1.11 - Đi chéo 1 ô – không lùi trừ vua
    private static final int[][] ALL_DIRS = new int[][] {
            {-1,-1}, {-1,1}, {1,-1}, {1,1}
    };

    /**
     * Trả về tất cả valid moves cho piece ở (r,c).
     * Nếu piece null hoặc không phải lượt -> list rỗng.
     * Nếu có capture sequences -> trả về chỉ capture sequences (mỗi Move có path & captures).
     */
    //UC1.10 - Chọn quân & hiển thị nước đi hợp lệ
    public List<Move> getValidMoves(int r, int c) {
        List<Move> result = new ArrayList<>();
        Piece p = board.getPiece(r,c);
        if (p == null) return result;
        if (p.isWhite != whiteTurn) return result; // không phải lượt piece này

        // 1) tìm tất cả Các nước có thể ăn bằng đệ quy. Sử dụng board.copy() để simulate
        List<Move> captureMoves = new ArrayList<>();
        findCaptureMoves(board.copy(), r, c, new ArrayList<>(), new ArrayList<>(), captureMoves);

        if (!captureMoves.isEmpty()) {
            return captureMoves; // bắt buộc ăn -> chỉ trả capture
        }

        // 2) nếu không có capture -> tạo normal moves
        //UC1.16 - Vua đi lùi & di chuyển xa hơn
        for (int[] d : ALL_DIRS) {
            if (!p.isKing) {
                int forward = p.isWhite ? -1 : 1;
                if (d[0] != forward) continue; // non-king chỉ forward
            }
            
            int nr = r + d[0], nc = c + d[1];
            if (!board.inBounds(nr,nc)) continue;
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

    //UC1.2 - Di chuyển quân cờ
    public static void applyMove(Board b, Move m) {
        if (m == null) return;
        Point from = m.from();
        Point to = m.to();
        //System.out.println(from.x+","+from.y+"-"+to.x+","+to.y);
        if (from == null || to == null) return;

        int fr = from.y, fc = from.x;
        int tr = to.y, tc = to.x;
        Piece p = b.getPiece(fr, fc);
        if (p == null) return;

        // place a copy at destination (safer cho simulation)
        //UC1.2 - Di chuyển quân cờ
        b.setPiece(tr, tc, p.copy());
        b.clearCell(fr, fc);

        // remove captured pieces
        //UC1.13 - Xóa quân bị ăn
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

    //UC1.2 - Di chuyển quân cờ
    public void makeMove(Move m) {
        applyMove(this.board, m);
        
        whiteTurn = !whiteTurn;
    }


    /**
     * - b: board hiện tại (đã được clone khi gọi ban đầu)
     * - curR,curC: vị trí hiện tại
     * - pathSoFar: list Point (col,row) path đã đi (start có thể được thêm)
     * - capsSoFar: list Point các vị trí bị ăn
     * - outMoves: collect kết quả (Move)
     *
     * Logic:
     *  - duyệt 4 hướng; với non-king thì chỉ forward
     *  - nếu cạnh có enemy và ô landing trống => simulate capture trên clone (nb)
     *  - sau simulate: gọi đệ quy tìm tiếp từ landing
     *  - nếu không có extension và capsSoFar không rỗng -> tạo Move từ pathSoFar và capsSoFar
     */
    //UC1.3 - Ăn quân
    private void findCaptureMoves(Board b, int curR, int curC,
                                  List<Point> pathSoFar, List<Point> capsSoFar,
                                  List<Move> outMoves) {
        Piece p = b.getPiece(curR, curC);
        if (p == null) return;

        List<Point> path = new ArrayList<>(pathSoFar);
        if (path.isEmpty()) path.add(new Point(curC, curR)); // start

        boolean extended = false;

        for (int[] d : ALL_DIRS) {
            if (!p.isKing) {
                int forward = p.isWhite ? -1 : 1;
                if (d[0] != forward) continue;
            }
           // UC1.12 - Nhảy qua quân đối phương
            // Vị trí quân cờ bị ăn 
            int midR = curR + d[0], midC = curC + d[1];
            // Vị trí đặt sau ăn
            int landR = curR + 2*d[0], landC = curC + 2*d[1];

            // Kiểm tra mid và land có hợp lệ trong bàn cờ không
            if (!b.inBounds(midR, midC) || !b.inBounds(landR, landC)) continue;
            // Lấy quân cờ
            Piece mid = b.getPiece(midR, midC);
            Piece land = b.getPiece(landR, landC); 

            // Kiểm tra quân trước mặt, và đăngf sau quân bị ăn không có quân nào
            // Kiểm tra có khác quân
            //Đệ quy lấy tất cả nước đi có thể
            if (mid != null && mid.isWhite != p.isWhite && land == null) {
            	//Thực hiện nước đi ăn quân
                //UC1.13 - Xóa quân bị ăn
                //UC1.4 - Ăn liên tiếp
                //UC1.14 - Kiểm tra chuỗi ăn tiếp theo
                Board nb = b.copy();
                nb.clearCell(midR, midC);
                Piece moved = nb.getPiece(curR, curC);
                nb.clearCell(curR, curC);
                nb.setPiece(landR, landC, moved);

                List<Point> nPath = new ArrayList<>(path);
                nPath.add(new Point(landC, landR));
                List<Point> nCaps = new ArrayList<>(capsSoFar);
                nCaps.add(new Point(midC, midR));

                // Đệ quy
                findCaptureMoves(nb, landR, landC, nPath, nCaps, outMoves);
                extended = true;
            }
        }

        // nếu không có extension và đã có captures -> kết thúc chuỗi
        if (!extended && !capsSoFar.isEmpty()) {
            Move m = new Move();
            for (Point pnt : path) m.path.add(new Point(pnt.x, pnt.y));
            for (Point cap : capsSoFar) m.captures.add(new Point(cap.x, cap.y));
            outMoves.add(m);
        }
    }
}
