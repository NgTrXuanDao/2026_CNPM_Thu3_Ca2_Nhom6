
package ai;

import controller.GameController;
import model.Board;
import model.Move;
import model.Piece;

public class MiniMax {
	public static long nodeCount = 0L;
// UC6.1 - Tính toán nước đi
	public Move findBestMove(Node rootState, int depth) {
		System.gc();
		long memBefore = usedMemory();
		long startTime = System.nanoTime();
		int diemCaoNhat = -9999999;
		Move bestMove = null;
		GameController controller = new GameController(rootState.board, rootState.isWhite);

		for(Move move : controller.getAllMoves(rootState.isWhite)) {
			Board cp = rootState.board.copy();
			new GameController(cp, rootState.isWhite);
			GameController.applyMove(cp, move);
			Node child = new Node(cp, !rootState.isWhite);
			int score = this.miniMax(false, child, depth - 1);
			if (score > diemCaoNhat) {
				diemCaoNhat = score;
				bestMove = move;
			}
		}

		long endTime = System.nanoTime();
		long memAfter = usedMemory();
		System.out.println("MiniMax depth " + depth + " | time = " + (endTime - startTime) / 1000000L + " ms | memory = " + (memAfter - memBefore) / 1024L + " KB | nodes = " + nodeCount);
		return bestMove;
	}

	private static long usedMemory() {
		Runtime rt = Runtime.getRuntime();
		return rt.totalMemory() - rt.freeMemory();
	}

	// UC6.3 - Minimax – Medium
	public int miniMax(boolean maxmin, Node state, int depth) {
		++nodeCount;
		GameController gameController = new GameController(state.board, state.isWhite);
		if (depth != 0 && !gameController.isOver()) {
			if (maxmin) {
				int temp = -9999999;

				for(Move move : gameController.getAllMoves(state.isWhite)) {
					Board cpBoard = state.board.copy();
					new GameController(cpBoard, state.isWhite);
					GameController.applyMove(cpBoard, move);
					Node stateChild = new Node(cpBoard, !state.isWhite);
					int value = this.miniMax(false, stateChild, depth - 1);
					if (value > temp) {
						temp = value;
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
					int value = this.miniMax(true, stateChild, depth - 1);
					if (value < temp) {
						temp = value;
					}
				}

				return temp;
			}
		} else {
			return this.heuricstic(state);
		}
	}
// UC6.2 - Đánh giá bàn cờ
	private int heuricstic(Node state) {
		int myRecor = 0;
		int enemyCount = 0;
// UC6.5 - Đếm số quân & vua
		for(int i = 0; i < 8; ++i) {
			for(int j = 0; j < 8; ++j) {
				Piece p = state.board.getPiece(i, j);
				if (p != null && p.isWhite != state.isWhite) {
					++enemyCount;
				}
			}
		}

		if (enemyCount == 0) {
			return 100000;
		} else {
			boolean midgame = enemyCount <= 8;
			boolean endgame = enemyCount <= 5;
			boolean end = enemyCount <= 1;
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
						myRecor += end ? 10000 : -10;
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
