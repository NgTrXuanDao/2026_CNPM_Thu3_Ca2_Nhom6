package ai;

import controller.GameController;
import model.Board;
import model.Move;
import model.Piece;

/**
 * UC6.1 – Tính toán nước đi (findBestMove)
 * UC6.3 – Minimax – Medium (miniMax)
 * UC6.2 – Đánh giá bàn cờ (heuristic)
 * UC6.5 – Đếm số quân & vua
 * UC6.6 – Đánh giá vị trí quân
 * Người thực hiện: Nguyễn Trần Xuân Đào
 * Ngày thực hiện chỉnh sửa: 04/06/2024
 * Mô tả: Cài đặt thuật toán Minimax thuần (không cắt tỉa). Được giữ lại để so sánh hiệu suất với AlphaBeta. Trong game thực, AlphaBeta được dùng cho tất cả chế độ; MiniMax dùng trong thí nghiệm benchmarking (alphaBetaVsMiniMax).
 */

public class MiniMax {

    /** Đếm tổng số node đã duyệt (so sánh với AlphaBeta để benchmark) */
    public static long nodeCount = 0L;

    // ─── UC6.1 – FIND BEST MOVE ───────────────────────────────────────────────

    /**
     * UC6.1 – Tìm nước đi tốt nhất cho AI theo thuật toán Minimax.
     * Duyệt tất cả nước của AI ở tầng 1, gọi miniMax() cho mỗi nhánh con.
     *
     * @param rootState Trạng thái gốc: {Board, isWhite (= bên AI)}
     * @param depth     Độ sâu duyệt (thường depth=6 khi benchmark)
     * @return          Move tốt nhất, hoặc null nếu không còn nước đi
     *
     * LUỒNG XỬ LÝ (cho Sequence Diagram):
     *   1. System.gc() + đo memory/time (dùng để benchmark)
     *   2. Tạo GameController(rootState.board, rootState.isWhite)
     *   3. Gọi controller.getAllMoves(rootState.isWhite) → List<Move>
     *   4. Với mỗi Move m:
     *      a. Board cp = rootState.board.copy()
     *      b. GameController.applyMove(cp, m)
     *      c. Node child = new Node(cp, !rootState.isWhite)
     *      d. int score = miniMax(false, child, depth - 1)  ← đệ quy
     *      e. Cập nhật bestMove nếu score > diemCaoNhat
     *   5. Log thời gian + memory + nodeCount
     *   6. Return bestMove
     *
     * ĐƯỢC GỌI BỞI: GameView (trong alphaBetaVsMiniMax – bên Đen dùng MiniMax)
     */
    public Move findBestMove(Node rootState, int depth) {
        System.gc();
        long memBefore   = usedMemory();
        long startTime   = System.nanoTime();
        nodeCount        = 0L;
        int diemCaoNhat  = -9_999_999;
        Move bestMove    = null;

        GameController controller = new GameController(rootState.board, rootState.isWhite);

        for (Move move : controller.getAllMoves(rootState.isWhite)) {
            Board cp = rootState.board.copy();
            GameController.applyMove(cp, move);
            Node child = new Node(cp, !rootState.isWhite);
            int score  = miniMax(false, child, depth - 1);

            if (score > diemCaoNhat) {
                diemCaoNhat = score;
                bestMove    = move;
            }
        }

        // Log hiệu suất (dùng khi benchmark so sánh với AlphaBeta)
        long endTime   = System.nanoTime();
        long memAfter  = usedMemory();
        System.out.printf("[MiniMax] depth=%d | time=%dms | mem=%dKB | nodes=%d%n",
                depth, (endTime - startTime) / 1_000_000L,
                (memAfter - memBefore) / 1024L, nodeCount);

        return bestMove;
    }

    /** Tính RAM đang dùng = totalMemory - freeMemory */
    private static long usedMemory() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    // ─── UC6.3 – MINIMAX ──────────────────────────────────────────────────────

    /**
     * UC6.3 – Đệ quy Minimax thuần (không cắt tỉa).
     *
     * @param maxmin true  = maximizing (lượt AI – tìm điểm cao nhất)
     *               false = minimizing (lượt đối thủ – tìm điểm thấp nhất)
     * @param state  Trạng thái node hiện tại {Board, isWhite}
     * @param depth  Số tầng còn lại (0 = leaf → heuristic)
     * @return       Điểm số heuristic tốt nhất trong subtree này
     *
     * LUỒNG XỬ LÝ (cho Sequence Diagram):
     *   nodeCount++  (đếm node)
     *   BASE CASE: depth==0 OR controller.isOver() → return heuristic(state)
     *
     *   MAXIMIZING (maxmin = true):
     *     temp = -∞
     *     for each Move m in getAllMoves(state.isWhite):
     *       cp = state.board.copy()
     *       applyMove(cp, m)
     *       child = Node(cp, !state.isWhite)
     *       value = miniMax(false, child, depth-1)   ← đệ quy
     *       temp = max(temp, value)
     *     return temp
     *
     *   MINIMIZING (maxmin = false):
     *     temp = +∞
     *     for each Move m in getAllMoves(state.isWhite):
     *       value = miniMax(true, child, depth-1)    ← đệ quy
     *       temp = min(temp, value)
     *     return temp
     *
     * ĐƯỢC GỌI BỞI: findBestMove() (gọi lần đầu) và chính nó (đệ quy)
     * KHÁC VỚI AlphaBeta: KHÔNG có tham số alpha, beta → KHÔNG cắt tỉa
     */
    public int miniMax(boolean maxmin, Node state, int depth) {
        nodeCount++;
        GameController gc = new GameController(state.board, state.isWhite);

        // ── BASE CASE ──
        if (depth == 0 || gc.isOver()) return heuristic(state);

        if (maxmin) {
            // ── MAXIMIZING ──
            int temp = -9_999_999;
            for (Move move : gc.getAllMoves(state.isWhite)) {
                Board cp = state.board.copy();
                GameController.applyMove(cp, move);
                Node child = new Node(cp, !state.isWhite);
                int value  = miniMax(false, child, depth - 1);
                if (value > temp) temp = value;
            }
            return temp;
        } else {
            // ── MINIMIZING ──
            int temp = 9_999_999;
            for (Move move : gc.getAllMoves(state.isWhite)) {
                Board cp = state.board.copy();
                GameController.applyMove(cp, move);
                Node child = new Node(cp, !state.isWhite);
                int value  = miniMax(true, child, depth - 1);
                if (value < temp) temp = value;
            }
            return temp;
        }
    }

    // ─── UC6.2 – HEURISTIC ────────────────────────────────────────────────────

    /**
     * UC6.2 – Hàm đánh giá bàn cờ tại node lá.
     * Tương tự AlphaBeta.heuristic() nhưng đơn giản hơn một chút
     * (không đếm myCount, dùng bonus end khác).
     *
     * @param state Node lá cần đánh giá
     * @return      int – điểm heuristic từ góc nhìn AI (state.isWhite)
     *              +100_000 = AI thắng (đối thủ hết quân)
     *
     * LUỒNG XỬ LÝ (cho Sequence Diagram):
     *   1. UC6.5: Đếm quân địch (enemyCount) bằng cách duyệt Board 8×8
     *   2. enemyCount == 0 → return 100_000 (AI thắng)
     *   3. Xác định phase: midgame, endgame, end
     *   4. UC6.6: Duyệt Board → tính điểm vị trí từng quân:
     *        colorAi=true → cộng, false → trừ
     *        Bonus "end": nếu chỉ còn rất ít quân địch → cộng mạnh
     *   5. Cộng bonus cơ động (mobility)
     *   6. Return tổng điểm
     *
     * ĐƯỢC GỌI BỞI: miniMax() khi depth == 0 hoặc isOver() == true
     */
    private int heuristic(Node state) {
        int myRecor    = 0;
        int enemyCount = 0;

        // ── UC6.5: Đếm quân địch ──
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece p = state.board.getPiece(i, j);
                if (p != null && p.isWhite != state.isWhite) enemyCount++;
            }
        }

        // ── Điều kiện AI thắng ──
        if (enemyCount == 0) return 100_000;

        // ── Xác định phase ──
        boolean midgame = enemyCount <= 8;
        boolean endgame = enemyCount <= 5;
        boolean end     = enemyCount <= 1;

        // ── UC6.6: Đánh giá vị trí từng quân ──
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece p = state.board.getPiece(i, j);
                if (p == null) continue;

                boolean colorAi = (p.isWhite == state.isWhite);
                int vking  = endgame ? 160 : 80;
                int vpiece = endgame ? 60  : 30;
                int temp   = p.isKing ? vking : vpiece;

                myRecor += colorAi ? temp : -temp;

                // Bonus cuối ván (ít quân địch → ưu thế áp đảo)
                myRecor += end ? 10_000 : -10;

                // Bonus tiến quân (quân thường, phase đầu)
                if (!p.isKing && !midgame) {
                    int progress = p.isWhite ? i : (7 - i);
                    myRecor += colorAi ? progress : -progress;
                }
            }
        }

        // ── Bonus cơ động ──
        GameController ctrl = new GameController(state.board, state.isWhite);
        int mobility        = ctrl.getAllMoves(state.isWhite).size();
        int doQuanTrong     = endgame ? 1 : 3;
        myRecor += mobility * doQuanTrong;

        return myRecor;
    }
}