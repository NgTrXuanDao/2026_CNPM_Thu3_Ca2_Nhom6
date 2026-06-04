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
 * GameController: nắm Board, lượt (whiteTurn), bộ đếm nước đi không ăn (drawCounter).
 * - getValidMoves(r,c): trả về tất cả moves hợp lệ cho piece tại (r,c), ưu tiên capture (multi-jump).
 * - getAllMoves(forWhite): tất cả moves cho 1 bên (dùng AI).
 * - applyMove(Board, Move): áp dụng move lên board (dùng cho simulation và thực thi).
 * - makeMove(Move): áp dụng move lên controller.board và đổi lượt.
 * - isDrawByNoCapture(): UC1.6 – kiểm tra hoà do 40 nước liên tiếp không ăn quân.
 *
 * US Checkers rules:
 * - piece (non-king) chỉ nhảy 2 ô chéo (forward theo màu)
 * - king có thể nhảy 4 hướng (nhưng vẫn nhảy 2 ô)
 * - bắt buộc ăn: nếu có một hay nhiều capture, chỉ phép capture.
 */
public class GameController {
    private Board board;
    private boolean whiteTurn;

    /*
     * UC1.6 - Kiểm tra trạng thái (hòa cờ – 40-move rule)
     * Người thực hiện: Nguyễn Khánh Duy
     * Ngày cập nhật: 04/06/2026
     * Nội dung:
     * - Đếm số nước đi liên tiếp không có ăn quân (no-capture moves).
     * - Nếu đạt 40 nước (cả 2 bên cộng lại = 80 half-moves) → hòa cờ.
     * - Reset về 0 mỗi khi có nước ăn quân xảy ra.
     * - Được kiểm tra trong makeMove() và isOver()/checkWinner().
     */
    private int noCaptureCount = 0;

    /** Ngưỡng hòa: 40 nước liên tiếp không ăn quân (mỗi bên 20 nước) */
    private static final int DRAW_LIMIT = 40;

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
     * Chuyển FirstTurnMode thành boolean whiteTurn
     * @param mode Chế độ (có thể null -> fallback White)
     * @return true = White, false = Black
     */
    public static boolean resolveFirstTurn(FirstTurnMode mode) {
        if (mode == null) return true; // Null safety: mặc định White
        switch (mode) {
            case WHITE:  return true;           // White đi trước
            case BLACK:  return false;          // Black đi trước
            case RANDOM: return new Random().nextBoolean(); // 50/50
            default:     return true;
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
        this.board.initialize(); // UC1.1 - Khởi tạo lại bàn cờ
        this.noCaptureCount = 0; // UC1.6 - Reset bộ đếm hòa về 0
        if (mode != null) {
            setFirstTurn(mode);  // UC1.9 - Thiết lập lượt đi đầu
        }
    }

    public Board getBoard() { return board; }
    public boolean isWhiteTurn() { return whiteTurn; }

    /*
     * UC1.6 - Kiểm tra trạng thái (hòa cờ)
     * Trả về số nước đi không ăn quân hiện tại (dùng để debug / hiển thị)
     */
    public int getNoCaptureCount() { return noCaptureCount; }

    // 4 hướng chéo
    // UC1.11 - Đi chéo 1 ô – không lùi trừ vua
    private static final int[][] ALL_DIRS = new int[][] {
            {-1,-1}, {-1,1}, {1,-1}, {1,1}
    };

    /**
     * Trả về tất cả valid moves cho piece ở (r,c).
     * Nếu piece null hoặc không phải lượt -> list rỗng.
     * Nếu có capture sequences -> trả về chỉ capture sequences (mỗi Move có path & captures).
     */
    // UC1.10 - Chọn quân & hiển thị nước đi hợp lệ
    public List<Move> getValidMoves(int r, int c) {
        List<Move> result = new ArrayList<>();
        Piece p = board.getPiece(r, c);
        if (p == null) return result;
        if (p.isWhite != whiteTurn) return result; // không phải lượt piece này

        // 1) Tìm tất cả nước có thể ăn bằng đệ quy. Sử dụng board.copy() để simulate
        // UC1.3 - Ăn quân | UC1.4 - Ăn liên tiếp | UC1.14 - Kiểm tra chuỗi ăn tiếp theo
        List<Move> captureMoves = new ArrayList<>();
        findCaptureMoves(board.copy(), r, c, new ArrayList<>(), new ArrayList<>(), captureMoves);

        if (!captureMoves.isEmpty()) {
            // UC5.2 - Bắt buộc ăn quân nếu có thể
            // UC5.4 - Chặn nước đi thường khi có thể ăn
            return captureMoves; // bắt buộc ăn -> chỉ trả capture
        }

        // 2) Nếu không có capture -> tạo normal moves
        for (int[] d : ALL_DIRS) {
            // UC1.11 - Đi chéo 1 ô – không lùi trừ vua: lọc hướng cho quân thường
            if (!p.isKing) {
                int forward = p.isWhite ? -1 : 1;
                if (d[0] != forward) continue; // non-king chỉ tiến
            }
            // UC1.16 - Vua đi lùi & di chuyển xa hơn: vua dùng ALL_DIRS không lọc hướng

            int nr = r + d[0], nc = c + d[1];
            if (!board.inBounds(nr, nc)) continue;
            if (board.getPiece(nr, nc) == null) {
                Move m = new Move(r, c, nr, nc);
                result.add(m);
            }
        }
        return result;
    }

    /*
     * UC1.6 - Kiểm tra trạng thái
     * UC1.18 - Hết nước đi → thua
     * Lấy toàn bộ nước đi hợp lệ của một bên (dùng cho AI và kiểm tra thua/hòa)
     * @param forWhite true = lấy nước của White, false = lấy nước của Black
     */
    public List<Move> getAllMoves(boolean forWhite) {
        List<Move> all = new ArrayList<>();
        boolean oldTurn = whiteTurn;
        whiteTurn = forWhite;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPiece(r, c);
                if (p != null && p.isWhite == forWhite) {
                    all.addAll(getValidMoves(r, c));
                }
            }
        }
        whiteTurn = oldTurn;
        return all;
    }

    /*
     * UC1.6 - Kiểm tra trạng thái – xác định người thắng
     * UC1.17 - Hết quân → thua: bên nào hết quân thì thua
     * UC1.18 - Hết nước đi → thua: bên đang đến lượt không còn nước đi hợp lệ → thua
     * Người thực hiện: Nguyễn Khánh Duy
     * Ngày cập nhật: 04/06/2026
     * Nội dung:
     * - UC1.6: Kiểm tra hòa trước (40-move rule, ưu tiên cao nhất)
     * - UC1.17: Kiểm tra hết quân – bên nào về 0 quân thì thua ngay
     * - UC1.18: Kiểm tra hết nước đi – dùng whiteTurn để biết bên NÀO đang đến lượt,
     *           chỉ bên đó bị thua nếu không còn nước đi (không check cả 2 bên song song,
     *           tránh trường hợp bên kia cũng "tưởng thua" khi chưa đến lượt)
     * @param board Bàn cờ cần kiểm tra (thường là controller.getBoard())
     * @return Winner.WHITE / Winner.BLACK / Winner.DRAW / Winner.NONE
     */
    public Winner checkWinner(Board board) {
        // UC1.6 - Ưu tiên kiểm tra hòa cờ trước (40-move no-capture rule)
        if (isDrawByNoCapture()) {
            return Winner.DRAW;
        }

        // UC1.17 - Hết quân → thua (không phụ thuộc lượt, mất quân là thua ngay)
        if (board.countWhitePieces() == 0) {
            return Winner.BLACK; // White hết quân → Black thắng
        }
        if (board.countBlackPieces() == 0) {
            return Winner.WHITE; // Black hết quân → White thắng
        }

        /*
         * UC1.18 - Hết nước đi → thua
         * Chỉ kiểm tra bên đang đến lượt (whiteTurn):
         * - Nếu White đang đến lượt mà không có nước đi → White thua
         * - Nếu Black đang đến lượt mà không có nước đi → Black thua
         * Không check cả 2 bên cùng lúc vì bên chưa đến lượt chưa cần đi,
         * không thể bị xét thua do "hết nước đi" khi chưa phải lượt của họ.
         */
        if (whiteTurn && getAllMoves(true).isEmpty()) {
            return Winner.BLACK; // White đến lượt nhưng không có nước đi → Black thắng
        }
        if (!whiteTurn && getAllMoves(false).isEmpty()) {
            return Winner.WHITE; // Black đến lượt nhưng không có nước đi → White thắng
        }

        return Winner.NONE; // Ván đấu vẫn đang tiếp tục
    }

    /*
     * UC1.17 - Hết quân → thua
     * UC1.6 - Kiểm tra trạng thái: kiểm tra một bên còn quân trên bàn cờ không
     * Người thực hiện: Nguyễn Khánh Duy
     * Ngày cập nhật: 04/06/2026
     * Nội dung:
     * - Dùng countWhitePieces() / countBlackPieces() từ Board thay vì tự duyệt lại
     *   → tránh lặp logic, đảm bảo nhất quán với checkWinner()
     * @param isWhite true = kiểm tra White, false = kiểm tra Black
     * @return true nếu còn ít nhất 1 quân, false nếu hết quân
     */
    public boolean hasPieces(boolean isWhite) {
        // Dùng method từ Board để đếm số quân, không cần duyệt lại bàn cờ
        if (isWhite) {
            return board.countWhitePieces() > 0;
        } else {
            return board.countBlackPieces() > 0;
        }
    }

    /*
     * UC1.17 - Hết quân → thua
     * UC1.18 - Hết nước đi → thua
     * UC1.6 - Kiểm tra trạng thái: ván đấu đã kết thúc chưa (thua hoặc hòa)
     * Người thực hiện: Nguyễn Khánh Duy
     * Ngày cập nhật: 04/06/2026
     * Nội dung:
     * - Gọi checkWinner() để tận dụng logic đầy đủ (hòa + hết quân + hết nước đi)
     * - Trả về true nếu kết quả không phải NONE (tức game đã có kết quả)
     * - Dùng bởi AI (miniMax, alphaBeta) để dừng đệ quy sớm khi đến node lá
     * @return true nếu game over, false nếu vẫn đang chơi
     */
    public boolean isOver() {
        // UC1.6 - Delegate sang checkWinner() để tránh logic trùng lặp.
        // checkWinner() đã xử lý đủ: hòa (UC1.6) + hết quân (UC1.17) + hết nước đi (UC1.18)
        return checkWinner(this.board) != Winner.NONE;
    }

    /*
     * UC1.6 - Kiểm tra trạng thái (hòa cờ – 40-move rule)
     * Người thực hiện: Nguyễn Khánh Duy
     * Ngày cập nhật: 04/06/2026
     * Nội dung:
     * - Luật hòa: nếu 40 nước liên tiếp không có ăn quân thì ván cờ hòa.
     * - Điều này tránh trường hợp 2 vua rượt nhau vô tận không có điểm dừng.
     * @return true nếu đủ điều kiện hòa, false nếu chưa
     */
    public boolean isDrawByNoCapture() {
        return noCaptureCount >= DRAW_LIMIT;
    }

    /**
     * Áp dụng Move m lên board b (static – dùng cả cho AI simulation).
     * UC1.2 - Di chuyển quân cờ: dịch chuyển piece từ ô nguồn sang ô đích
     * UC1.13 - Xóa quân bị ăn: clearCell tất cả ô trong captures
     * UC1.5 / UC1.15 - Phong cấp – vua: kiểm tra hàng cuối sau khi di chuyển
     */
    public static void applyMove(Board b, Move m) {
        if (m == null) return;
        Point from = m.from();
        Point to   = m.to();
        if (from == null || to == null) return;

        int fr = from.y, fc = from.x;
        int tr = to.y,   tc = to.x;
        Piece p = b.getPiece(fr, fc);
        if (p == null) return;

        // UC1.2 - Di chuyển quân cờ: đặt bản sao piece sang ô đích, xóa ô nguồn
        b.setPiece(tr, tc, p.copy());
        b.clearCell(fr, fc);

        // UC1.13 - Xóa quân bị ăn: xóa tất cả quân đối phương bị nhảy qua
        for (Point cap : m.captures) {
            b.clearCell(cap.y, cap.x);
        }

        // UC1.5 - Phong cấp – vua | UC1.15 - Quân đến hàng cuối → phong vua
        Piece placed = b.getPiece(tr, tc);
        if (placed != null && !placed.isKing) {
            if (placed.isWhite  && tr == 0) placed.isKing = true; // White lên hàng 0 → King
            if (!placed.isWhite && tr == 7) placed.isKing = true; // Black xuống hàng 7 → King
        }
    }

    /*
     * UC1.2 - Di chuyển quân cờ: áp dụng move lên bàn cờ thật và đổi lượt
     * UC1.6 - Kiểm tra trạng thái: cập nhật bộ đếm hòa sau mỗi nước đi
     * @param m Nước đi cần thực thi
     */
    public void makeMove(Move m) {
        applyMove(this.board, m);

        /*
         * UC1.6 - Kiểm tra trạng thái (hòa cờ – cập nhật bộ đếm)
         * - Nếu nước đi có ăn quân (capture): reset bộ đếm về 0
         * - Nếu không ăn quân: tăng bộ đếm lên 1
         * - Khi đủ DRAW_LIMIT nước liên tiếp không ăn → hòa (isDrawByNoCapture() = true)
         */
        if (m.isCapture()) {
            noCaptureCount = 0; // có ăn quân → reset đếm hòa
        } else {
            noCaptureCount++;   // không ăn quân → tiến gần hòa hơn
        }

        whiteTurn = !whiteTurn; // UC1.2 - Đổi lượt sau mỗi nước đi
    }

    /**
     * Tìm đệ quy tất cả nước ăn quân có thể từ vị trí (curR, curC).
     * UC1.3 - Ăn quân: tìm ô enemy kề và ô đáp trống để nhảy qua
     * UC1.4 - Ăn liên tiếp: sau mỗi lần ăn, tiếp tục tìm ăn tiếp từ ô mới
     * UC1.12 - Nhảy qua quân đối phương: xác định mid (bị ăn) và land (đáp)
     * UC1.13 - Xóa quân bị ăn: thêm midR/midC vào capsSoFar để clearCell sau
     * UC1.14 - Kiểm tra chuỗi ăn tiếp theo: gọi đệ quy từ landR/landC
     * UC1.16 - Vua đi lùi & xa hơn: king không lọc hướng → ăn được cả 4 hướng
     */
    private void findCaptureMoves(Board b, int curR, int curC,
                                  List<Point> pathSoFar, List<Point> capsSoFar,
                                  List<Move> outMoves) {
        Piece p = b.getPiece(curR, curC);
        if (p == null) return;

        List<Point> path = new ArrayList<>(pathSoFar);
        if (path.isEmpty()) path.add(new Point(curC, curR)); // thêm điểm xuất phát

        boolean extended = false;

        for (int[] d : ALL_DIRS) {
            // UC1.11 - Đi chéo 1 ô – không lùi trừ vua: quân thường chỉ ăn tiến
            // UC1.16 - Vua đi lùi & xa hơn: vua (isKing) ăn được cả 4 hướng
            if (!p.isKing) {
                int forward = p.isWhite ? -1 : 1;
                if (d[0] != forward) continue;
            }

            // UC1.12 - Nhảy qua quân đối phương
            int midR  = curR + d[0],     midC  = curC + d[1];   // vị trí quân bị ăn
            int landR = curR + 2 * d[0], landC = curC + 2 * d[1]; // vị trí đáp xuống

            // Kiểm tra mid và land nằm trong bàn cờ
            if (!b.inBounds(midR, midC) || !b.inBounds(landR, landC)) continue;

            Piece mid  = b.getPiece(midR,  midC);
            Piece land = b.getPiece(landR, landC);

            // Điều kiện ăn: mid là quân đối phương, land trống
            if (mid != null && mid.isWhite != p.isWhite && land == null) {
                // UC1.13 - Xóa quân bị ăn: simulate trên bản sao bàn cờ
                // UC1.4 - Ăn liên tiếp: chuẩn bị board mới để đệ quy tiếp
                Board nb = b.copy();
                nb.clearCell(midR, midC);             // xóa quân bị ăn
                Piece moved = nb.getPiece(curR, curC);
                nb.clearCell(curR, curC);             // xóa ô xuất phát
                nb.setPiece(landR, landC, moved);     // đặt quân vào ô đáp

                List<Point> nPath = new ArrayList<>(path);
                nPath.add(new Point(landC, landR));   // ghi nhận bước đi
                List<Point> nCaps = new ArrayList<>(capsSoFar);
                nCaps.add(new Point(midC, midR));     // ghi nhận quân bị ăn

                // UC1.14 - Kiểm tra chuỗi ăn tiếp theo: đệ quy từ ô mới
                findCaptureMoves(nb, landR, landC, nPath, nCaps, outMoves);
                extended = true;
            }
        }

        // Không có extension nữa và đã có ít nhất 1 lần ăn → kết thúc chuỗi, lưu Move
        if (!extended && !capsSoFar.isEmpty()) {
            Move m = new Move();
            for (Point pnt : path)       m.path.add(new Point(pnt.x, pnt.y));
            for (Point cap : capsSoFar)  m.captures.add(new Point(cap.x, cap.y));
            outMoves.add(m);
        }
    }
}
