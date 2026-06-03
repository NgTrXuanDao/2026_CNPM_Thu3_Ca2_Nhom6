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

/*
 * UC1.9 - Xac dinh nguoi di truoc
 * Nguoi thuc hien: Doan Ngoc Anh
 * Ngay cap nhat: 02/06/2026
 * Noi dung:
 * - Hien thi thong tin luot di hien tai (White/Black turn)
 * - Ve truc tiep thong tin turn trong paintComponent (khong dung child component)
 */

public class GameView extends JPanel {

	private GameController controller;
	private BufferedImage whiteImg, blackImg, whiteKingImg, blackKingImg;

	private int selectedRow = -1, selectedCol = -1;
	private List<Move> possibleMoves = new ArrayList<>();

	private final int CELL = 70;

	private boolean aiThinking = false;

	// UC1.9: Chieu cao panel thong tin luot
	private static final int INFO_PANEL_HEIGHT = 40;

	public GameView(GameController controller) {
		this.controller = controller;
		loadImages();

		setPreferredSize(new Dimension(8 * CELL, 8 * CELL + INFO_PANEL_HEIGHT));

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int c = e.getX() / CELL;
				// Tru INFO_PANEL_HEIGHT boi vi thong tin luot duoc ve o phia tren
				int r = (e.getY() - INFO_PANEL_HEIGHT) / CELL;

				if (r >= 0 && r < 8 && c >= 0 && c < 8) {
					handleClick(r, c);
				}
			}
		});
	}

	// Load Anh
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

		new Thread(() -> {
			Node state = new Node(controller.getBoard().copy(), controller.isWhiteTurn());
			AlphaBeta ai = new AlphaBeta();
			Move aiMove = ai.findBestMove(state, 6);

			try {
				Thread.sleep(2000); // Độ trễ 2 giây
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}

			if (aiMove != null) {
				SwingUtilities.invokeLater(() -> {
					controller.makeMove(aiMove);
					Winner winner1 = controller.checkWinner(controller.getBoard());
					if (winner1 != Winner.NONE) {
						showWinDialog(winner1);
					}
					aiThinking = false;
					repaint();
				});
			} else {
				SwingUtilities.invokeLater(() -> {
					aiThinking = false;
					repaint();
				});
			}
		}).start();
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

		Move aiMove = ab.findBestMove(state, 6);

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

	/*
	 * UC1.9 - Xac dinh nguoi di truoc
	 * Lay chuoi hien thi thong tin luot di hien tai
	 */
	private String getTurnText() {
		if (controller.isWhiteTurn()) {
			return "Luot di: Trang (White)";
		} else {
			return "Luot di: Den (Black)";
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		// UC1.9: Ve thong tin luot o phia tren
		drawTurnInfo(g);
		// Ve ban co (dich xuong duoi phan thong tin luot)
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.translate(0, INFO_PANEL_HEIGHT);
		drawBoard(g2d);
		drawPieces(g2d);
		drawHighlights(g2d);
		g2d.dispose();
	}

	/*
	 * UC1.9 - Xac dinh nguoi di truoc
	 * Ve thong tin luot di hien tai o phia tren cung
	 */
	private void drawTurnInfo(Graphics g) {
		// Background cho phan thong tin
		g.setColor(new Color(50, 50, 50));
		g.fillRect(0, 0, getWidth(), INFO_PANEL_HEIGHT);

		// Van ban thong tin luot
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 14));
		String text = getTurnText();
		FontMetrics fm = g.getFontMetrics();
		int x = (getWidth() - fm.stringWidth(text)) / 2;
		int y = (INFO_PANEL_HEIGHT + fm.getAscent()) / 2 - 2;
		g.drawString(text, x, y);
	}

	// ===============================================================================
	// UC3.1 - Hien thi ban co
	// ===============================================================================
	private void drawBoard(Graphics g) {
		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				boolean isDark = (r + c) % 2 == 1;
				g.setColor(isDark ? new Color(110, 80, 50) : new Color(240, 220, 170));
				g.fillRect(c * CELL, r * CELL, CELL, CELL);
			}
		}
	}

	// ===============================================================================
	// UC3.5 - Hien thi quan thuong & vua
	// ===============================================================================
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

	// ===============================================================================
	// UC3.2 - Highlight nuoc di hop le
	// UC3.6 - Highlight o co the di
	// ===============================================================================
	private void drawHighlights(Graphics g) {

		// highlight nuoc di
		g.setColor(new Color(0, 255, 0, 120));
		for (Move m : possibleMoves) {
			g.fillRect(m.getToCol() * CELL, m.getToRow() * CELL, CELL, CELL);
		}

		// highlight o chon
		if (selectedRow != -1) {
			g.setColor(Color.YELLOW);
			g.drawRect(selectedCol * CELL, selectedRow * CELL, CELL, CELL);
			g.drawRect(selectedCol * CELL + 1, selectedRow * CELL + 1, CELL - 2, CELL - 2);
		}
	}
	
	// ===============================================================================
	// UC3.4 - Thong bao luot / ket qua
	// UC3.8 - Thong bao thang / thua / hoa
	// ===============================================================================
	public void showWinDialog(Winner winner) {
	    String message = (winner == Winner.WHITE)
	            ? " Trang thang!"
	            : " Den thang!";

	    JOptionPane.showMessageDialog(
	            this,
	            message,
	            "KET THUC TRAN DAU",
	            JOptionPane.INFORMATION_MESSAGE
	    );
	    System.out.println("end");
	}

}
