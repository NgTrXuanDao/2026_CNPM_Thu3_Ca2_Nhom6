package ai;

import controller.GameController;
import model.Board;
import model.Move;
import model.Piece;

/**
 * UC6.1 – Tính toán nước đi (findBestMove)
 * UC6.4 – Alpha-Beta Pruning – Hard (alphaBeta)
 * UC6.2 – Đánh giá bàn cờ (heuristic)
 * UC6.5 – Đếm số quân & vua (vòng lặp đếm trong heuristic)
 * UC6.6 – Đánh giá vị trí quân (vòng lặp tính điểm vị trí)
 * Người thực hiện: Nguyễn Trần Xuân Đào
 * Ngày thực hiện chỉnh sửa: 04/06/2024
 * Mô tả: Cài đặt thuật toán Alpha-Beta Pruning – phiên bản AI mạnh nhất. Được dùng cho chế độ HARD (depth=6), EASY (depth=5), MEDIUM (depth=3). Cắt tỉa Alpha-Beta giúp loại bỏ các nhánh không cần thiết, giảm số node duyệt so với Minimax thuần ~10x ở depth cao.
 */
public class AlphaBeta {

    /** Đếm tổng số node đã duyệt (dùng để log hiệu suất) */
    public static long nodeCount = 0L;

    // ─── UC6.1 – FIND BEST MOVE ───────────────────────────────────────────────

    /**
     * UC6.1 – Tìm nước đi tốt nhất cho AI tại trạng thái rootState.
     * Đây là entry point của thuật toán – được gọi từ GameView.
     *
     * @param rootState Trạng thái gốc: gồm board hiện tại (Board) + lượt AI (isWhite).
     *                  Bất biến – không bị thay đổi trong quá trình duyệt.
     * @param depth     Độ sâu duyệt cây: HARD=6, MEDIUM=3, EASY=5.
     *                  Độ sâu lớn hơn → mạnh hơn nhưng chậm hơn.
     * @return          Move tốt nhất tìm được, hoặc null nếu không có nước đi.
     *
     * LUỒNG XỬ LÝ (cho Sequence Diagram):
     *   1. Reset nodeCount = 0 (chuẩn bị log)
     *   2. Tạo GameController(rootState.board, rootState.isWhite)
     *   3. Gọi controller.getAllMoves(rootState.isWhite) → List<Move>
     *   4. Với mỗi Move m trong danh sách:
     *      a. Board cp = rootState.board.copy()          ← deep copy
     *      b. GameController.applyMove(cp, m)            ← apply trên copy
     *      c. Node child = new Node(cp, !rootState.isWhite)
     *      d. int score = alphaBeta(false, child, depth-1, -∞, +∞)
     *      e. Nếu score >= WIN_THRESHOLD → trả ngay (nước thắng tức thì)
     *      f. Cập nhật bestScore, bestMove nếu score > bestScore
     *   5. Trả về bestMove
     *
     * ĐƯỢC GỌI BỞI: GameView.makeAIMove() (chạy trong Thread riêng)
     * TRẢ VỀ: Move object (null nếu AI không có nước đi nào)
     */
    public Move findBestMove(Node rootState, int depth) {
        nodeCount = 0L;
        int bestScore  = Integer.MIN_VALUE;
        Move bestMove  = null;
        GameController controller = new GameController(rootState.board, rootState.isWhite);

        for (Move move : controller.getAllMoves(rootState.isWhite)) {
            // Deep copy board để không làm ảnh hưởng trạng thái gốc
            Board cp = rootState.board.copy();
            GameController.applyMove(cp, move);
            Node child = new Node(cp, !rootState.isWhite);

            // Gọi alphaBeta cho node con (lượt của đối thủ → minimizing)
            int score = alphaBeta(false, child, depth - 1, -9_999_999, 9_999_999);

            // Nếu tìm được nước thắng ngay (score rất cao) → return sớm
            if (score >= 9_000_000) return move;

            if (score > bestScore) {
                bestScore = score;
                bestMove  = move;
            }
        }

        return bestMove;
    }

    // ─── UC6.4 – ALPHA-BETA PRUNING ───────────────────────────────────────────

    /**
     * UC6.4 – Đệ quy Alpha-Beta Pruning (minimax với cắt tỉa).
     *
     * @param maxmin true  = maximizing player (lượt AI – muốn điểm cao nhất)
     *               false = minimizing player (lượt đối thủ – muốn điểm thấp nhất)
     * @param state  Trạng thái hiện tại tại node này (Board + isWhite)
     * @param depth  Số tầng còn lại cần duyệt (0 = node lá → gọi heuristic)
     * @param alpha  Giá trị tốt nhất MAX tìm được đến hiện tại (khởi đầu = -∞)
     * @param beta   Giá trị tốt nhất MIN tìm được đến hiện tại (khởi đầu = +∞)
     * @return       Điểm số heuristic của node này (hoặc tốt nhất trong subtree)
     *
     * LUỒNG XỬ LÝ (cho Sequence Diagram):
     *   BASE CASE: depth == 0 → return heuristic(state)
     *
     *   MAXIMIZING (maxmin = true):
     *     temp = -∞
     *     for each Move m:
     *       cp = state.board.copy()
     *       applyMove(cp, m)
     *       child = new Node(cp, !state.isWhite)
     *       value = alphaBeta(false, child, depth-1, alpha, beta)  ← đệ quy
     *       temp = max(temp, value)
     *       alpha = max(alpha, temp)
     *       if alpha >= beta → BREAK (β-cutoff: MIN sẽ không chọn nhánh này)
     *     return temp
     *
     *   MINIMIZING (maxmin = false):
     *     temp = +∞
     *     for each Move m:
     *       value = alphaBeta(true, child, depth-1, alpha, beta)   ← đệ quy
     *       temp = min(temp, value)
     *       beta = min(beta, temp)
     *       if alpha >= beta → BREAK (α-cutoff: MAX sẽ không chọn nhánh này)
     *     return temp
     *
     * ĐƯỢC GỌI BỞI: findBestMove() (tầng 0) và chính nó (đệ quy)
     * TRẢ VỀ: int – điểm số đánh giá cho node này
     */
    public int alphaBeta(boolean maxmin, Node state, int depth, int alpha, int beta) {
        nodeCount++;
        GameController gc = new GameController(state.board, state.isWhite);

        // ── BASE CASE: leaf node ──
        if (depth == 0) return heuristic(state);

        if (maxmin) {
            // ── MAXIMIZING (lượt AI) ──
            int temp = -9_999_999;
            for (Move move : gc.getAllMoves(state.isWhite)) {
                Board cp = state.board.copy();
                GameController.applyMove(cp, move);
                Node child = new Node(cp, !state.isWhite);
                int value  = alphaBeta(false, child, depth - 1, alpha, beta);
                if (value > temp) temp = value;
                alpha = Math.max(alpha, temp);
                if (alpha >= beta) break; // β-cutoff: cắt tỉa
            }
            return temp;
        } else {
            // ── MINIMIZING (lượt đối thủ) ──
            int temp = 9_999_999;
            for (Move move : gc.getAllMoves(state.isWhite)) {
                Board cp = state.board.copy();
                GameController.applyMove(cp, move);
                Node child = new Node(cp, !state.isWhite);
                int value  = alphaBeta(true, child, depth - 1, alpha, beta);
                if (value < temp) temp = value;
                beta = Math.min(beta, temp);
                if (alpha >= beta) break; // α-cutoff: cắt tỉa
            }
            return temp;
        }
    }

    // ─── UC6.2 – EVALUATE BOARD (HEURISTIC) ──────────────────────────────────

    /**
     * UC6.2 – Hàm đánh giá thế trận tại một node lá.
     * Trả về điểm số từ góc nhìn của AI (state.isWhite):
     *   Điểm dương → AI đang có lợi thế
     *   Điểm âm    → Đối thủ đang có lợi thế
     *
     * @param state Trạng thái node lá cần đánh giá (Board + isWhite AI)
     * @return      int – điểm heuristic, phạm vi thông thường [-500_000, +500_000]
     *              Giá trị đặc biệt: +10_000_000 = AI thắng, -1_000_000 = AI thua
     *
     * LUỒNG XỬ LÝ (cho Sequence Diagram):
     *   1. UC6.5: Đếm quân AI (myCount) và đối thủ (enemyCount) bằng cách duyệt board
     *   2. Điều kiện kết thúc: enemyCount==0 → +10M, myCount==0 → -1M
     *   3. Bonus nếu đối thủ sắp thua (enemyCount<=1: +50_000, ==1: +20_000)
     *   4. Xác định phase: midgame (enemy<=8), endgame (enemy<=5)
     *   5. UC6.6: Duyệt board → tính điểm vị trí từng quân:
     *        - Vua: vking = endgame ? 160 : 80
     *        - Quân thường: vpiece = endgame ? 60 : 30
     *        - colorAi=true → cộng điểm, false → trừ điểm
     *        - Quân thường midgame: cộng bonus tiến quân (progress)
     *   6. Cộng điểm cơ động: getAllMoves().size() * doQuanTrong
     *
     * ĐƯỢC GỌI BỞI: alphaBeta() khi depth == 0
     * PHỤ THUỘC: Board.getPiece(), GameController.getAllMoves()
     */
    private int heuristic(Node state) {
        int myRecor    = 0;
        int myCount    = 0;
        int enemyCount = 0;

        // ── UC6.5: Đếm số quân & vua ──
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece p = state.board.getPiece(i, j);
                if (p != null) {
                    if (p.isWhite != state.isWhite) enemyCount++;
                    else                             myCount++;
                }
            }
        }

        // ── Điều kiện kết thúc ──
        if (enemyCount == 0) return 10_000_000;  // AI thắng
        if (myCount    == 0) return -1_000_000;  // AI thua

        // ── Bonus gần thắng ──
        if (enemyCount == 1) myRecor += 20_000;
        if (enemyCount <= 1) myRecor += 50_000;

        // ── Xác định phase ──
        boolean midgame = enemyCount <= 8;
        boolean endgame = enemyCount <= 5;

        // ── UC6.6: Đánh giá vị trí từng quân ──
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece p = state.board.getPiece(i, j);
                if (p == null) continue;

                boolean colorAi = (p.isWhite == state.isWhite);
                int vking  = endgame ? 160 : 80;
                int vpiece = endgame ? 60  : 30;
                int temp   = p.isKing ? vking : vpiece;

                // Cộng nếu là quân mình, trừ nếu là quân địch
                myRecor += colorAi ? temp : -temp;

                // Bonus tiến quân (chỉ quân thường, phase đầu)
                if (!p.isKing && !midgame) {
                    int progress = p.isWhite ? i : (7 - i);
                    myRecor += colorAi ? progress : -progress;
                }
            }
        }

        // ── Bonus cơ động: càng nhiều nước đi càng tốt ──
        GameController ctrl    = new GameController(state.board, state.isWhite);
        int mobility           = ctrl.getAllMoves(state.isWhite).size();
        int doQuanTrong        = endgame ? 1 : 3;
        myRecor += mobility * doQuanTrong;

        return myRecor;
    }
}