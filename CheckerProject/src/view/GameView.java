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
 * UC1.9 - Xac dinh nguoi di truoc - Đoàn Ngọc Ánh
 * Nguoi thuc hien: Đoàn Ngọc Ánh
 * Ngay cap nhat: 02/06/2026 → 07/06/2026
 * Noi dung:
 * - UC1.9.6: Hien thi thong tin luot di hien tai (White/Black turn) qua getTurnText()
 * - Ve truc tiep thong tin turn trong paintComponent (khong dung child component)
 * - UC1.2: Hỗ trợ xử lý click chọn quân và di chuyển
 * - UC1.10: Hỗ trợ hiển thị highlight nước đi hợp lệ
 */

/*
 * UC 3.1 den 3.8 - Hien thi, UI/UX & Hieu ung Animation
 * Nguoi thuc hien: Tran Quang Duy
 * Ngay cap nhat: 05/06/2026
 * Noi dung:
 * - Triển khai Master Loop 60 FPS bằng javax.swing.Timer cho toàn bộ UI Component.
 * - UC3.1: Nâng cấp bảng màu chế độ cờ gỗ Mahogany cao cấp.
 * - UC3.3: Hiệu ứng mạch đập (Pulse Breathing Alpha) cho ô chọn và ô gợi ý nước đi.
 * - UC3.5: Khử răng cưa hình ảnh quân cờ kết hợp kỹ thuật vẽ bóng đổ Drop-Shadow tạo khối 3D.
 * - UC3.7: Thuật toán chuyển động nội suy Cubic Ease-Out cho quân cờ di chuyển và Alpha Fade-out cho quân bị ăn.
 * - UC3.8: Trì hoãn hiển thị Dialog thắng cuộc chờ kết thúc hoạt ảnh động để tránh xung đột luồng.
 */

public class GameView extends JPanel {

    private GameController controller;
    private BufferedImage whiteImg, blackImg, whiteKingImg, blackKingImg;

    private int selectedRow = -1, selectedCol = -1;
    private List<Move> possibleMoves = new ArrayList<>();

    private final int CELL = 70;

    public int currentChoice = 4;
    private boolean aiThinking = false;

    // UC1.9: Chieu cao panel thong tin luot
    private static final int INFO_PANEL_HEIGHT = 40;

    private boolean isPaused = false;
    // vị trí cho nút tạm dừng và thoát
    private Rectangle pauseBtnRect = new Rectangle(10, 8, 80, 24);
    private Rectangle exitBtnRect = new Rectangle(8 * 70 - 90, 8, 80, 24);

    // Bien trang thai animation
    private Timer uiMasterTimer;
    private float pulseAlpha = 0.4f;
    private boolean pulseGrowing = true;

    private boolean isAnimating = false;
    private Move activeAnimatingMove = null;
    private double slideProgress = 0.0;
    private double captureFadeAlpha = 1.0;
    private Runnable postAnimCallback = null;

    public GameView(GameController controller) {
        this.controller = controller;
        loadImages();

        setPreferredSize(new Dimension(8 * CELL, 8 * CELL + INFO_PANEL_HEIGHT));

        // Khởi tạo bộ dựng hoạt ảnh 60 FPS (16ms)
        initAnimationEngine();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // CHỐNG SPAM CLICK: Khóa toàn bộ tương tác khi quân cờ đang chạy hoặc AI đang tính
                if (isAnimating || aiThinking || isPaused) {
                    if (isPaused && e.getY() < INFO_PANEL_HEIGHT && pauseBtnRect.contains(e.getX(), e.getY())) {
                        isPaused = false;
                        repaint();
                    }
                    return;
                }

                int mouseX = e.getX();
                int mouseY = e.getY();

                if (mouseY < INFO_PANEL_HEIGHT) {
                    if (pauseBtnRect.contains(mouseX, mouseY)) {
                        isPaused = !isPaused;
                        repaint();
                        return;
                    }
                    if (exitBtnRect.contains(mouseX, mouseY)) {
                        int confirm = JOptionPane.showConfirmDialog(
                                GameView.this,
                                "Bạn có chắc chắn muốn thoát game không?",
                                "THOÁT GAME",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE
                        );
                        if (confirm == JOptionPane.YES_OPTION) {
                            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(GameView.this);
                            if (topFrame != null) {
                                topFrame.dispose();
                            }
                        }
                        return;
                    }
                    return;
                }

                int c = e.getX() / CELL;
                int r = (e.getY() - INFO_PANEL_HEIGHT) / CELL;

                if (r >= 0 && r < 8 && c >= 0 && c < 8) {
                    if (currentChoice == 1) {
                        handleClick1(r, c);
                    } else if (currentChoice == 2) {
                        handleClick(r, c);
                    } else if (currentChoice == 3) {
                        handleClick3(r, c);
                    } else if (currentChoice == 4) {
                        handleClick4(r, c);
                    }
                }
            }
        });
    }

    // Master UI loop engine
    private void initAnimationEngine() {
        uiMasterTimer = new Timer(16, e -> {
            // 1. Xử lý nhịp đập mờ ảo (Pulse) cho phần Highlight ô cờ
            if (pulseGrowing) {
                pulseAlpha += 0.025f;
                if (pulseAlpha >= 0.75f) { pulseAlpha = 0.75f; pulseGrowing = false; }
            } else {
                pulseAlpha -= 0.025f;
                if (pulseAlpha <= 0.25f) { pulseAlpha = 0.25f; pulseGrowing = true; }
            }

            // 2. Xử lý tịnh tiến Slide và hiệu ứng biến mất của quân cờ
            if (isAnimating) {
                if (slideProgress < 1.0) {
                    slideProgress += 0.08; // Tốc độ trượt di chuyển quân
                    if (slideProgress >= 1.0) {
                        slideProgress = 1.0;
                    }
                } else if (captureFadeAlpha > 0.0) {
                    captureFadeAlpha -= 0.12; // Tốc độ mờ dần khi ăn quân đối phương
                    if (captureFadeAlpha <= 0.0) {
                        captureFadeAlpha = 0.0;
                        isAnimating = false;
                        // Hoàn tất hoạt ảnh -> Thực thi đổi trạng thái trên Board
                        if (postAnimCallback != null) {
                            SwingUtilities.invokeLater(postAnimCallback);
                        }
                    }
                }
            }
            repaint();
        });
        uiMasterTimer.start();
    }

    //Hàm đánh chặn nước đi để kích hoạt hoạt ảnh trước khi lưu vào Board
    private void executeMoveWithAnimation(Move chosen, Runnable onCompleteAction) {
        if (chosen == null) return;
        this.activeAnimatingMove = chosen;
        this.slideProgress = 0.0;
        this.captureFadeAlpha = 1.0;
        this.isAnimating = true;
        this.postAnimCallback = () -> {
            controller.makeMove(chosen);
            if (onCompleteAction != null) {
                onCompleteAction.run();
            }
        };
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
    // mode : dễ
    private void handleClick(int r, int c) {
        if (aiThinking || isAnimating) return;
        Piece p = controller.getBoard().getPiece(r, c);

        if (selectedRow == -1 || (p != null && p.isWhite == controller.isWhiteTurn())) {
            if (p != null && p.isWhite == controller.isWhiteTurn()) {
                selectedRow = r; selectedCol = c;
                possibleMoves = controller.getValidMoves(r, c);
            }
            repaint();
            return;
        }

        Move chosen = findMove(r, c);
        if (chosen == null) { repaint(); return; }

        // Chạy hiệu ứng trượt quân của Người chơi
        executeMoveWithAnimation(chosen, () -> {
            Winner winner = controller.checkWinner(controller.getBoard());
            if (winner != Winner.NONE) { showWinDialog(winner); }
            selectedRow = selectedCol = -1;
            possibleMoves.clear();

            if (controller.isOver()) return;
            aiThinking = true;

            new Thread(() -> {
                Node state = new Node(controller.getBoard().copy(), controller.isWhiteTurn());
                AlphaBeta ai = new AlphaBeta();
                Move aiMove = ai.findBestMove(state, 5);

                try { Thread.sleep(1200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

                if (aiMove != null) {
                    SwingUtilities.invokeLater(() -> {
                        // Chạy hiệu ứng trượt quân của AI dữ liệu máy tính
                        executeMoveWithAnimation(aiMove, () -> {
                            Winner winner1 = controller.checkWinner(controller.getBoard());
                            if (winner1 != Winner.NONE) showWinDialog(winner1);
                            aiThinking = false;
                        });
                    });
                } else {
                    SwingUtilities.invokeLater(() -> aiThinking = false);
                }
            }).start();
        });
    }

    // mode : khó
    private void handleClick1(int r, int c) {
        if (aiThinking || isAnimating) return;
        Piece p = controller.getBoard().getPiece(r, c);

        if (selectedRow == -1 || (p != null && p.isWhite == controller.isWhiteTurn())) {
            if (p != null && p.isWhite == controller.isWhiteTurn()) {
                selectedRow = r; selectedCol = c;
                possibleMoves = controller.getValidMoves(r, c);
            }
            repaint();
            return;
        }

        Move chosen = findMove(r, c);
        if (chosen == null) { repaint(); return; }

        executeMoveWithAnimation(chosen, () -> {
            Winner winner = controller.checkWinner(controller.getBoard());
            if (winner != Winner.NONE) showWinDialog(winner);
            selectedRow = selectedCol = -1;
            possibleMoves.clear();

            if (controller.isOver()) return;
            aiThinking = true;

            Node state = new Node(controller.getBoard().copy(), controller.isWhiteTurn());
            AlphaBeta ab = new AlphaBeta();
            Move aiMove = ab.findBestMove(state, 6);

            if (aiMove != null) {
                executeMoveWithAnimation(aiMove, () -> {
                    Winner winner1 = controller.checkWinner(controller.getBoard());
                    if (winner1 != Winner.NONE) showWinDialog(winner1);
                    aiThinking = false;
                });
            } else {
                aiThinking = false;
            }
        });
    }

    // mode : trung bình
    private void handleClick3(int r, int c) {
        if (aiThinking || isAnimating) return;
        Piece p = controller.getBoard().getPiece(r, c);

        if (selectedRow == -1 || (p != null && p.isWhite == controller.isWhiteTurn())) {
            if (p != null && p.isWhite == controller.isWhiteTurn()) {
                selectedRow = r; selectedCol = c;
                possibleMoves = controller.getValidMoves(r, c);
            }
            repaint();
            return;
        }

        Move chosen = findMove(r, c);
        if (chosen == null) { repaint(); return; }

        executeMoveWithAnimation(chosen, () -> {
            Winner winner = controller.checkWinner(controller.getBoard());
            if (winner != Winner.NONE) showWinDialog(winner);
            selectedRow = selectedCol = -1;
            possibleMoves.clear();

            if (controller.isOver()) return;
            aiThinking = true;

            new Thread(() -> {
                Node state = new Node(controller.getBoard().copy(), controller.isWhiteTurn());
                AlphaBeta ai = new AlphaBeta();
                Move aiMove = ai.findBestMove(state, 3);

                try { Thread.sleep(1200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

                if (aiMove != null) {
                    SwingUtilities.invokeLater(() -> {
                        executeMoveWithAnimation(aiMove, () -> {
                            Winner winner1 = controller.checkWinner(controller.getBoard());
                            if (winner1 != Winner.NONE) showWinDialog(winner1);
                            aiThinking = false;
                        });
                    });
                } else {
                    SwingUtilities.invokeLater(() -> aiThinking = false);
                }
            }).start();
        });
    }

    // UC1.2.1 - Người chơi nhấp chuột vào ô chứa quân (chế độ PvP)
    // UC2.1: PVP
    private void handleClick4(int r, int c) {
        if (aiThinking || isAnimating) return;
        Piece p = controller.getBoard().getPiece(r, c);

        if (selectedRow == -1 || (p != null && p.isWhite == controller.isWhiteTurn())) {
            if (p != null && p.isWhite == controller.isWhiteTurn()) {
                selectedRow = r; selectedCol = c;
                possibleMoves = controller.getValidMoves(r, c);
            }
            repaint();
            return;
        }

        Move chosen = findMove(r, c);
        if (chosen == null) { repaint(); return; }

        executeMoveWithAnimation(chosen, () -> {
            Winner winner = controller.checkWinner(controller.getBoard());
            if (winner != Winner.NONE) showWinDialog(winner);
            selectedRow = selectedCol = -1;
            possibleMoves.clear();
        });
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

    // UC1.9.6 - Lấy chuỗi hiển thị thông tin lượt đi hiện tại
    private String getTurnText() {
        if (isPaused) {
            return "GAME DANG TAM DUNG";
        }
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
        // UC1.9.6: Vẽ bàn cờ (dịch xuống dưới phần thông tin lượt)
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.translate(0, INFO_PANEL_HEIGHT);
        drawBoard(g2d);
        drawPieces(g2d);
        drawHighlights(g2d);
        g2d.dispose();
    }

    // UC1.9.6 - Vẽ thông tin lượt đi hiện tại ở phía trên cùng bàn cờ
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

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Ve nut Tam dung / Tiep tuc
        g2d.setColor(isPaused ? new Color(200, 150, 50) : new Color(70, 70, 70));
        g2d.fillRoundRect(pauseBtnRect.x, pauseBtnRect.y, pauseBtnRect.width, pauseBtnRect.height, 8, 8);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        String pText = isPaused ? "Tiep tuc" : "Tam dung";
        int px = pauseBtnRect.x + (pauseBtnRect.width - g2d.getFontMetrics().stringWidth(pText)) / 2;
        int py = pauseBtnRect.y + (pauseBtnRect.height + g2d.getFontMetrics().getAscent()) / 2 - 2;
        g2d.drawString(pText, px, py);

        // Ve nut Thoat
        g2d.setColor(new Color(150, 50, 50));
        g2d.fillRoundRect(exitBtnRect.x, exitBtnRect.y, exitBtnRect.width, exitBtnRect.height, 8, 8);
        g2d.setColor(Color.WHITE);
        String eText = "Thoat";
        int ex = exitBtnRect.x + (exitBtnRect.width - g2d.getFontMetrics().stringWidth(eText)) / 2;
        int ey = exitBtnRect.y + (exitBtnRect.height + g2d.getFontMetrics().getAscent()) / 2 - 2;
        g2d.drawString(eText, ex, ey);
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
    // UC3.2 - Highlight nước đi hợp lệ
    // UC3.3 - Hiệu ứng nhấp nháy / mạch đập (Pulse Alpha) của ô chọn
    // UC3.6 - Highlight ô có thể đi
    // ===============================================================================
    private void drawHighlights(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();

        // 1. [UC3.2 & UC3.6] - Highlight các ô đích có thể di chuyển đến (Xanh lục mềm mại tỏa sáng Glow)
        for (Move m : possibleMoves) {
            g2d.setColor(new Color(46, 204, 113, (int)(pulseAlpha * 180)));
            g2d.fillRoundRect(m.getToCol() * CELL + 6, m.getToRow() * CELL + 6, CELL - 12, CELL - 12, 10, 10);
            g2d.setColor(new Color(46, 204, 113, 220));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(m.getToCol() * CELL + 6, m.getToRow() * CELL + 6, CELL - 12, CELL - 12, 10, 10);
        }

        // 2. [UC3.3] - Highlight ô cờ hiện tại đang được nhấn chọn (Màu vàng dạ quang mạch đập)
        if (selectedRow != -1) {
            g2d.setColor(new Color(241, 196, 15, (int)(pulseAlpha * 255)));
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRect(selectedCol * CELL + 2, selectedRow * CELL + 2, CELL - 4, CELL - 4);
        }
        g2d.dispose();
    }

    // ===============================================================================
    // UC3.5 & UC3.7 - Vẽ quân cờ (Vua/Thường) & Xử lý Hoạt ảnh Di chuyển / Ăn quân mờ dần
    // ===============================================================================
    private void drawPieces(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        Board board = controller.getBoard();

        int skipRow = -1, skipCol = -1;
        List<Point> capturedPoints = new ArrayList<>();

        if (isAnimating && activeAnimatingMove != null) {
            skipRow = activeAnimatingMove.from().y;
            skipCol = activeAnimatingMove.from().x;
            capturedPoints = activeAnimatingMove.captures;
        }

        // UC3.5: Vẽ các quân cờ tĩnh kết hợp bóng đổ Drop-Shadow tạo khối 3D
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (r == skipRow && c == skipCol) continue;

                Piece p = board.getPiece(r, c);
                if (p == null) continue;

                BufferedImage img = p.isWhite ? (p.isKing ? whiteKingImg : whiteImg)
                        : (p.isKing ? blackKingImg : blackImg);

                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

                // UC3.7: Kiểm tra nếu ô này chứa quân bị ăn -> Thực hiện hoạt ảnh mờ dần Fade out
                if (isAnimating && capturedPoints.contains(new Point(c, r))) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) captureFadeAlpha));
                }

                g2d.setColor(new Color(0, 0, 0, 60));
                g2d.fillOval(c * CELL + 8, r * CELL + 11, CELL - 16, CELL - 16);

                g2d.drawImage(img, c * CELL + 6, r * CELL + 6, CELL - 12, CELL - 12, null);
            }
        }

        // UC3.7: Thực hiện nội suy tọa độ trượt (Cubic Ease-Out) cho quân cờ đang chạy
        if (isAnimating && activeAnimatingMove != null) {
            int fr = activeAnimatingMove.from().y;
            int fc = activeAnimatingMove.from().x;
            int tr = activeAnimatingMove.to().y;
            int tc = activeAnimatingMove.to().x;

            Piece movingPiece = board.getPiece(fr, fc);
            if (movingPiece != null) {
                BufferedImage img = movingPiece.isWhite ? (movingPiece.isKing ? whiteKingImg : whiteImg)
                        : (movingPiece.isKing ? blackKingImg : blackImg);

                double easeProgress = 1.0 - Math.pow(1.0 - slideProgress, 3);

                int curX = (int) (fc * CELL + (tc - fc) * CELL * easeProgress);
                int curY = (int) (fr * CELL + (tr - fr) * CELL * easeProgress);

                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                g2d.setColor(new Color(0, 0, 0, 80));
                g2d.fillOval(curX + 8, curY + 11, CELL - 16, CELL - 16);

                g2d.drawImage(img, curX + 6, curY + 6, CELL - 12, CELL - 12, null);
            }
        }
        g2d.dispose();
    }

    // ===============================================================================
    // UC3.8 - Thông báo kết quả trận đấu
    // ===============================================================================
    public void showWinDialog(Winner winner) {
        String message = (winner == Winner.WHITE) ? "Quan TRANG da gianh chien thang!" : "Quan DEN da gianh chien thang!";
        JOptionPane.showMessageDialog(
                this,
                message,
                "KET THUC TRAN DAU",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}