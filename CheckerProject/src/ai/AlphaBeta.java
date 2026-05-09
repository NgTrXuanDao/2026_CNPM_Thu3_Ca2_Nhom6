
package ai;

import controller.GameController;
import model.Board;
import model.Move;
import model.Piece;

public class AlphaBeta {
    public static long nodeCount = 0L;
    // UC6.1 - Tính toán nước đi
    public Move findBestMove(Node rootState, int depth) {
        nodeCount = 0L;
        int bestScore = -9999999;
        Move bestMove = null;
        GameController controller = new GameController(rootState.board, rootState.isWhite);

        for(Move move : controller.getAllMoves(rootState.isWhite)) {
            Board cp = rootState.board.copy();
            new GameController(cp, rootState.isWhite);
            GameController.applyMove(cp, move);
            Node child = new Node(cp, !rootState.isWhite);
            int score = this.alphaBeta(false, child, depth - 1, -9999999, 9999999);
            if (score >= 9000000) {
                return move;
            }

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        return bestMove;
    }
// UC6.4 - Alpha-Beta Pruning – Hard
    public int alphaBeta(boolean maxmin, Node state, int depth, int alpha, int beta) {
        GameController gameController = new GameController(state.board, state.isWhite);
        if (depth == 0) {
            return this.heuricstic(state);
        } else if (maxmin) {
            int temp = -9999999;

            for(Move move : gameController.getAllMoves(state.isWhite)) {
                Board cpBoard = state.board.copy();
                new GameController(cpBoard, state.isWhite);
                GameController.applyMove(cpBoard, move);
                Node stateChild = new Node(cpBoard, !state.isWhite);
                int value = this.alphaBeta(false, stateChild, depth - 1, alpha, beta);
                if (value > temp) {
                    temp = value;
                }

                alpha = Math.max(alpha, temp);
                if (alpha >= beta) {
                    break;
                }
            }

            return temp;
        } else {
            int temp = 9999999;

            for(Move move : gameController.getAllMoves(state.isWhite)) {
                Board cpBoard = state.board.copy();
                new GameController(cpBoard, state.isWhite);
                GameController.applyMove(cpBoard, move);
                Node stateChild = new Node(cpBoard, !state.isWhite);
                int value = this.alphaBeta(true, stateChild, depth - 1, alpha, beta);
                if (value < temp) {
                    temp = value;
                }

                beta = Math.min(beta, temp);
                if (alpha >= beta) {
                    break;
                }
            }

            return temp;
        }
    }
// UC6.2 - Đánh giá bàn cờ
    private int heuricstic(Node state) {
        int myRecor = 0;
        int myCount = 0;
        int enemyCount = 0;
// UC6.5 - Đếm số quân & vua
        for(int i = 0; i < 8; ++i) {
            for(int j = 0; j < 8; ++j) {
                Piece p = state.board.getPiece(i, j);
                if (p != null) {
                    if (p.isWhite != state.isWhite) {
                        ++enemyCount;
                    } else {
                        ++myCount;
                    }
                }
            }
        }

        if (enemyCount == 0) {
            return 10000000;
        } else if (myCount == 0) {
            return -1000000;
        } else {
            if (enemyCount == 1) {
                myRecor += 20000;
            }

            boolean midgame = enemyCount <= 8;
            boolean endgame = enemyCount <= 5;
            if (enemyCount <= 1) {
                myRecor += 50000;
            }
// UC6.6 - Đánh giá vị trí quân
            for(int i = 0; i < 8; ++i) {
                for(int j = 0; j < 8; ++j) {
                    Piece p = state.board.getPiece(i, j);
                    if (p != null) {
                        boolean colorAi = p.isWhite == state.isWhite;
                        int vking = endgame ? 160 : 80;
                        int vpiece = endgame ? 60 : 30;
                        int temp = p.isKing ? vking : vpiece;
                        myRecor += colorAi ? temp : -temp;
                        if (!p.isKing && !midgame) {
                            int progress = p.isWhite ? i : 7 - i;
                            myRecor += colorAi ? progress : -progress;
                        }
                    }
                }
            }

            GameController controller = new GameController(state.board, state.isWhite);
            int khaNangDiChuyen = controller.getAllMoves(state.isWhite).size();
            int doQuanTrong = endgame ? 1 : 3;
            myRecor += khaNangDiChuyen * doQuanTrong;
            return myRecor;
        }
    }
}
