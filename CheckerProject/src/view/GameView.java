package view;

import controller.GameController;
import controller.Winner;
import model.Board;
import model.Move;
import model.Piece;

import javax.swing.*;

import ai.AlphaBeta;
import ai.MiniMax;
import ai.Node;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class GameView extends JPanel {

	private GameController controller;
	private BufferedImage whiteImg, blackImg, whiteKingImg, blackKingImg;

	private int selectedRow = -1, selectedCol = -1;
	private List<Move> possibleMoves = new ArrayList<>();

	private final int CELL = 70;

	private boolean aiThinking = false;

	public GameView(GameController controller) {
		this.controller = controller;
		loadImages();

		setPreferredSize(new Dimension(8 * CELL, 8 * CELL));

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int c = e.getX() / CELL;
				int r = e.getY() / CELL;

				 handleClick(r, c);
				//ABVsAB();
			}
		});
	}

	// Load Ảnh
	private void loadImages() {
		try {
			whiteImg = ImageIO.read(getClass().getResource("/img/white.png"));
			blackImg = ImageIO.read(getClass().getResource("/img/black.png"));
			whiteKingImg = ImageIO.read(getClass().getResource("/img/whiteking.png"));
			blackKingImg = ImageIO.read(getClass().getResource("/img/blackking.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void handleClick(int r, int c) {
		AlphaBeta ab = new AlphaBeta();
		if (aiThinking)
			return;

		Piece p = controller.getBoard().getPiece(r, c);

		if (selectedRow == -1) {

			if (p != null && p.isWhite == controller.isWhiteTurn()) {
				selectedRow = r;
				selectedCol = c;
				possibleMoves = controller.getValidMoves(r, c);
			}

			repaint();
			return;
		}

		if (p != null && p.isWhite == controller.isWhiteTurn()) {
			selectedRow = r;
			selectedCol = c;
			possibleMoves = controller.getValidMoves(r, c);
			repaint();
			return;
		}

		Move chosen = findMove(r, c);

		if (chosen == null) {
			repaint();
			return;
		}

		
		controller.makeMove(chosen);
		
		
		Winner winner = controller.checkWinner(controller.getBoard());
        if (winner != Winner.NONE) {
            showWinDialog(winner);
        }
		selectedRow = selectedCol = -1;
		possibleMoves.clear();

		repaint();

		if (controller.isOver())
			return;

		aiThinking = true;

		Node state = new Node(controller.getBoard().copy(), controller.isWhiteTurn());

		Move aiMove = ab.findBestMove(state, 4);

//		Node testState = new Node(controller.getBoard().copy(), controller.isWhiteTurn());
//
//		MiniMax mm = new MiniMax();
//		// AlphaBeta ab = new AlphaBeta();
//
//		mm.findBestMove(testState, 1);
//		ab.findBestMove(testState, 1);
//
//		mm.findBestMove(testState, 2);
//		ab.findBestMove(testState, 2);
//
//		mm.findBestMove(testState, 3);
//		ab.findBestMove(testState, 3);
//
//		mm.findBestMove(testState, 4);
//		ab.findBestMove(testState, 4);
//
//		mm.findBestMove(testState, 5);
//		ab.findBestMove(testState, 5);
//
//		mm.findBestMove(testState, 6);
//		ab.findBestMove(testState, 6);

		if (aiMove != null) {
			controller.makeMove(aiMove);
			Winner winner1 = controller.checkWinner(controller.getBoard());
	        if (winner1 != Winner.NONE) {
	            showWinDialog(winner);
	        }
		}

		aiThinking = false;
		repaint();
	}
	private void handleClick1(int r, int c) {
		AlphaBeta ab = new AlphaBeta();
		if (aiThinking)
			return;

		Piece p = controller.getBoard().getPiece(r, c);

		if (selectedRow == -1) {

			if (p != null && p.isWhite == controller.isWhiteTurn()) {
				selectedRow = r;
				selectedCol = c;
				possibleMoves = controller.getValidMoves(r, c);
			}

			repaint();
			return;
		}

		if (p != null && p.isWhite == controller.isWhiteTurn()) {
			selectedRow = r;
			selectedCol = c;
			possibleMoves = controller.getValidMoves(r, c);
			repaint();
			return;
		}

		Move chosen = findMove(r, c);

		if (chosen == null) {
			repaint();
			return;
		}

		
		controller.makeMove(chosen);
		
		
		Winner winner = controller.checkWinner(controller.getBoard());
        if (winner != Winner.NONE) {
            showWinDialog(winner);
        }
		selectedRow = selectedCol = -1;
		possibleMoves.clear();

		repaint();

		if (controller.isOver())
			return;

		aiThinking = true;

		Node state = new Node(controller.getBoard().copy(), controller.isWhiteTurn());

		Move aiMove = ab.findBestMove(state, 4);

		if (aiMove != null) {
			controller.makeMove(aiMove);
			Winner winner1 = controller.checkWinner(controller.getBoard());
	        if (winner1 != Winner.NONE) {
	            showWinDialog(winner);
	        }
		}

		aiThinking = false;
		repaint();
	}

	private void alphaBetaVsMiniMax() {
		MiniMax mm = new MiniMax();
		AlphaBeta ab = new AlphaBeta();

		if (aiThinking)
			return;
		if (controller.isOver())
			return;

		aiThinking = true;

		Node state = new Node(controller.getBoard().copy(), controller.isWhiteTurn());

		Move aiMove;

		// Quy ước: Trắng = AlphaBeta, Đen = MiniMax
		if (controller.isWhiteTurn()) {
			aiMove = ab.findBestMove(state, 6);
		} else {
			aiMove = mm.findBestMove(state, 6);
		}

		if (aiMove != null) {
			controller.makeMove(aiMove);
		}

		aiThinking = false;
		repaint();
	}

	private void ABVsAB() {
		AlphaBeta ab0 = new AlphaBeta();
		AlphaBeta ab1 = new AlphaBeta();

		if (aiThinking || controller.isOver())
			return;

		aiThinking = true;

		new Thread(() -> {

			Node state = new Node(controller.getBoard().copy(), controller.isWhiteTurn());

			Move aiMove;
			if (controller.isWhiteTurn()) {
				aiMove = ab0.findBestMove(state, 6);
			} else {
				aiMove = ab1.findBestMove(state, 4);
			}

			if (aiMove != null) {
				SwingUtilities.invokeLater(() -> {
					controller.makeMove(aiMove);
					aiThinking = false;
					repaint();
				});
			} else {
				aiThinking = false;
			}

		}).start();
	}

	private Move findMove(int r, int c) {
		for (Move m : possibleMoves) {
			if (m.getToRow() == r && m.getToCol() == c)
				return m;
		}
		return null;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawBoard(g);
		drawPieces(g);
		drawHighlights(g);
	}

	private void drawBoard(Graphics g) {
		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				boolean isDark = (r + c) % 2 == 1;
				g.setColor(isDark ? new Color(110, 80, 50) : new Color(240, 220, 170));
				g.fillRect(c * CELL, r * CELL, CELL, CELL);
			}
		}
	}

	private void drawPieces(Graphics g) {
		Board board = controller.getBoard();
		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				Piece p = board.getPiece(r, c);
				if (p == null)
					continue;

				BufferedImage img = p.isWhite ? (p.isKing ? whiteKingImg : whiteImg)
						: (p.isKing ? blackKingImg : blackImg);

				g.drawImage(img, c * CELL + 5, r * CELL + 5, CELL - 10, CELL - 10, null);
			}
		}
	}

	private void drawHighlights(Graphics g) {

		// highlight nước đi
		g.setColor(new Color(0, 255, 0, 120));
		for (Move m : possibleMoves) {
			g.fillRect(m.getToCol() * CELL, m.getToRow() * CELL, CELL, CELL);
		}

		// highlight ô chọn
		if (selectedRow != -1) {
			g.setColor(Color.YELLOW);
			g.drawRect(selectedCol * CELL, selectedRow * CELL, CELL, CELL);
			g.drawRect(selectedCol * CELL + 1, selectedRow * CELL + 1, CELL - 2, CELL - 2);
		}
	}
	public void showWinDialog(Winner winner) {
	    String message = (winner == Winner.WHITE)
	            ? " Trắng thắng!"
	            : " Đen thắng!";

	    JOptionPane.showMessageDialog(
	            this,
	            message,
	            "KẾT THÚC TRẬN ĐẤU",
	            JOptionPane.INFORMATION_MESSAGE
	    );
	    System.out.println("end");
	}

}
