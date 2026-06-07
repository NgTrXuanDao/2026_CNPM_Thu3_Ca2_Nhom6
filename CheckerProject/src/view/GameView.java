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

/*
 * UC 3.1 den 3.8 - Hien thi, UI/UX & Hieu ung Animation
 * Nguoi thuc hien: Tran Quang Duy (MSSV: 23130081)
 * Ngay cap nhat: 05/06/2026 - Cap nhat lan 2: 06/06/2026
 * Noi dung:
 * - Triển khai Master Loop 60 FPS bằng javax.swing.Timer cho toàn bộ UI Component.
 * - UC3.1: Nâng cấp bảng màu chế độ cờ gỗ Mahogany cao cấp.
 * - UC3.2: Highlight nước đi hợp lệ bằng màu xanh lục glow.
 * - UC3.3: Hiệu ứng mạch đập (Pulse Breathing Alpha) cho ô chọn và ô gợi ý nước đi.
 * - UC3.4: Thông báo lượt chơi (White/Black turn) trên thanh trạng thái.
 * - UC3.5: Khử răng cưa hình ảnh quân cờ kết hợp kỹ thuật vẽ bóng đổ Drop-Shadow tạo khối 3D.
 * - UC3.6: Highlight ô có thể đi (cùng logic với UC3.2, gộp trong drawHighlights()).
 * - UC3.7: Thuật toán chuyển động nội suy Cubic Ease-Out cho quân cờ di chuyển và Alpha Fade-out cho quân bị ăn.
 * - UC3.8: Trì hoãn hiển thị Dialog thắng cuộc chờ kết thúc hoạt ảnh động để tránh xung đột luồng.
 *
 * KIEN TRUC:
 * - Master Loop: javax.swing.Timer(16ms ~ 60FPS) điều khiển pulseAlpha, slideProgress, captureFadeAlpha
 * - State Machine: IDLE → SLIDING → CAPTURE_FADING → COMPLETE
 * - Anti-spam: Double Lock (isAnimating + aiThinking) chặn mọi click khi đang bận
 * - Thread Safety: AI chạy trên Thread riêng, giao tiếp UI qua SwingUtilities.invokeLater()
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

    // ============================================================
    // BIEN TRANG THAI ANIMATION ENGINE (UC3.3, UC3.7)
    // ============================================================
    // [UC3.3] Master Timer 60 FPS - Điều khiển toàn bộ vòng lặp UI
    private Timer uiMasterTimer;
    // [UC3.3] pulseAlpha dao động [0.25, 0.75] - Hiệu ứng mạch đập (Breathing Pulse)
    private float pulseAlpha = 0.4f;
    private boolean pulseGrowing = true;  // Hướng biến thiên của pulseAlpha

    // [UC3.7] Cờ khóa tương tác khi animation đang chạy (chống spam click)
    private boolean isAnimating = false;
    // [UC3.7] Nước đi đang được animate (lưu tọa độ from, to, captures)
    private Move activeAnimatingMove = null;
    // [UC3.7] Tiến độ trượt 0.0 → 1.0 (Phase 1: SLIDE)
    private double slideProgress = 0.0;
    // [UC3.7] Độ mờ quân bị ăn 1.0 → 0.0 (Phase 2: FADE)
    private double captureFadeAlpha = 1.0;
    // [UC3.7, UC3.8] Callback được gọi sau khi animation kết thúc
    // -> gọi controller.makeMove() + checkWinner() + showWinDialog()
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

    // ============================================================
    // [CORE] Master UI Loop Engine - Trái tim của toàn bộ phân hệ UI
    // ============================================================
    // Chạy ở 60 FPS (16ms/frame), xử lý:
    //   1. [UC3.3] Cập nhật pulseAlpha (hiệu ứng mạch đập)
    //   2. [UC3.7] Cập nhật slideProgress (trượt quân) và captureFadeAlpha (mờ quân bị ăn)
    //   3. Gọi repaint() để vẽ lại toàn bộ giao diện
    // ============================================================
    // [UC3.3 - Buoc 1] Mỗi 16ms, uiMasterTimer tick → gọi ActionListener
    private void initAnimationEngine() {
        uiMasterTimer = new Timer(16, e -> {
            // ===== [UC3.3] Pulse Breathing Alpha (các bước 2-6) =====
            // [UC3.3 - Buoc 2] Nếu pulseGrowing == true: pulseAlpha += 0.025f
            if (pulseGrowing) {
                pulseAlpha += 0.025f;
                // [UC3.3 - Buoc 3] Nếu pulseAlpha >= 0.75f: gán pulseAlpha = 0.75f; pulseGrowing = false
                if (pulseAlpha >= 0.75f) { pulseAlpha = 0.75f; pulseGrowing = false; }
            } else {
                // [UC3.3 - Buoc 4] Nếu pulseGrowing == false: pulseAlpha -= 0.025f
                pulseAlpha -= 0.025f;
                // [UC3.3 - Buoc 5] Nếu pulseAlpha <= 0.25f: gán pulseAlpha = 0.25f; pulseGrowing = true
                if (pulseAlpha <= 0.25f) { pulseAlpha = 0.25f; pulseGrowing = true; }
            }

            // ===== [UC3.7] Slide & Fade Animation (xem chi tiết bên dưới) =====
            if (isAnimating) {
                // [UC3.7 - Phase 1 - Buoc 6] Mỗi Timer tick: slideProgress += 0.08 (0.0 → 1.0)
                if (slideProgress < 1.0) {
                    slideProgress += 0.08;
                    if (slideProgress >= 1.0) {
                        slideProgress = 1.0;
                        // [UC3.7 - Buoc 10] slideProgress >= 1.0 → chuyển sang Phase 2 (FADE)
                    }
                // [UC3.7 - Phase 2 - Buoc 11] Mỗi Timer tick: captureFadeAlpha -= 0.12 (1.0 → 0.0)
                } else if (captureFadeAlpha > 0.0) {
                    captureFadeAlpha -= 0.12;
                    if (captureFadeAlpha <= 0.0) {
                        // [UC3.7 - Buoc 13] captureFadeAlpha <= 0.0 → isAnimating = false (MỞ khóa)
                        captureFadeAlpha = 0.0;
                        isAnimating = false;
                        // [UC3.7 - Buoc 14] Gọi SwingUtilities.invokeLater(postAnimCallback)
                        if (postAnimCallback != null) {
                            SwingUtilities.invokeLater(postAnimCallback);
                        }
                    }
                }
            }
            // [UC3.3 - Buoc 6] Gọi repaint() → drawHighlights() dùng pulseAlpha làm alpha
            repaint();
        });
        uiMasterTimer.start();
    }

    // ============================================================
    // [UC3.7] Kích hoạt Animation trước khi cập nhật Board
    // ============================================================
    // Đây là phương thức đánh chặn (Interceptor) quan trọng:
    // Thay vì gọi controller.makeMove() ngay lập tức, chúng ta:
    //   1. Lưu nước đi vào activeAnimatingMove
    //   2. Reset slideProgress = 0.0, captureFadeAlpha = 1.0
    //   3. Bật cờ isAnimating = true (khóa tương tác)
    //   4. Lưu callback vào postAnimCallback
    //   5. Timer sẽ tự động chạy animation → kết thúc → gọi callback
    //   6. Trong callback: controller.makeMove() + onCompleteAction.run()
    // ============================================================
    // [UC3.7 - Buoc 1] Người chơi chọn ô đích → findMove(r,c) trả về Move → executeMoveWithAnimation(chosen, callback)
    private void executeMoveWithAnimation(Move chosen, Runnable onCompleteAction) {
        if (chosen == null) return;
        // [UC3.7 - Buoc 3] Lưu activeAnimatingMove = chosen; Reset slideProgress = 0.0; captureFadeAlpha = 1.0
        this.activeAnimatingMove = chosen;     // Lưu nước đi cần animate
        this.slideProgress = 0.0;              // Bắt đầu: chưa trượt (Phase 1 SLIDE)
        this.captureFadeAlpha = 1.0;            // Bắt đầu: chưa mờ (Phase 2 FADE)
        // [UC3.7 - Buoc 4] Bật isAnimating = true (KHÓA tương tác)
        this.isAnimating = true;                // KHÓA tương tác người dùng
        // [UC3.7 - Buoc 5] Tạo postAnimCallback: gọi controller.makeMove(chosen) + callback.run()
        this.postAnimCallback = () -> {
            // [UC3.7 - Buoc 15] controller.makeMove(chosen) cập nhật Board SAU animation
            controller.makeMove(chosen);
            // [UC3.7 - Buoc 16] callback.run() — check winner, AI move (nếu có)
            if (onCompleteAction != null) {
                onCompleteAction.run();
            }
        };
    }

    // ===============================================================================
    // [UC3.5 - Buoc 1] Load 4 ảnh PNG từ resource: white.png, black.png, whiteking.png, blackking.png
    // ===============================================================================
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
                List<Move> moves = controller.getValidMoves(r, c);
                // [UC5.2] Bat buoc an: neu co nuoc an tren ban, chi chon quan co the an
                if (controller.hasCaptureMoves(controller.isWhiteTurn()) && 
                    (moves.isEmpty() || !moves.get(0).isCapture())) {
                    repaint();
                    return;
                }
                selectedRow = r; selectedCol = c;
                possibleMoves = moves;
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
                List<Move> moves = controller.getValidMoves(r, c);
                // [UC5.2] Bat buoc an: neu co nuoc an tren ban, chi chon quan co the an
                if (controller.hasCaptureMoves(controller.isWhiteTurn()) && 
                    (moves.isEmpty() || !moves.get(0).isCapture())) {
                    repaint();
                    return;
                }
                selectedRow = r; selectedCol = c;
                possibleMoves = moves;
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
                List<Move> moves = controller.getValidMoves(r, c);
                // [UC5.2] Bat buoc an: neu co nuoc an tren ban, chi chon quan co the an
                if (controller.hasCaptureMoves(controller.isWhiteTurn()) && 
                    (moves.isEmpty() || !moves.get(0).isCapture())) {
                    repaint();
                    return;
                }
                selectedRow = r; selectedCol = c;
                possibleMoves = moves;
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

    // UC 2.1 : PVP
    private void handleClick4(int r, int c) {
        if (aiThinking || isAnimating) return;
        Piece p = controller.getBoard().getPiece(r, c);

        if (selectedRow == -1 || (p != null && p.isWhite == controller.isWhiteTurn())) {
            if (p != null && p.isWhite == controller.isWhiteTurn()) {
                List<Move> moves = controller.getValidMoves(r, c);
                // [UC5.2] Bat buoc an: neu co nuoc an tren ban, chi chon quan co the an
                if (controller.hasCaptureMoves(controller.isWhiteTurn()) && 
                    (moves.isEmpty() || !moves.get(0).isCapture())) {
                    repaint();
                    return;
                }
                selectedRow = r; selectedCol = c;
                possibleMoves = moves;
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

    /*
     * UC1.9 - Xac dinh nguoi di truoc
     * Lay chuoi hien thi thong tin luot di hien tai
     * 
     * [UC3.4 - Buoc 3] getTurnText() lấy text:
     *   - Nếu isPaused: "GAME DANG TAM DUNG"
     *   - Nếu White turn: "Luot di: Trang (White)"
     *   - Nếu Black turn: "Luot di: Den (Black)"
     */
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
        // [UC3.4 - Buoc 1] paintComponent() được gọi → g2d.translate + drawTurnInfo(g)
        drawTurnInfo(g);
        // Ve ban co (dich xuong duoi phan thong tin luot)
        Graphics2D g2d = (Graphics2D) g.create();
        // [UC3.1 - Buoc 1] translate(0, INFO_PANEL_HEIGHT) để dịch xuống dưới thanh trạng thái
        g2d.translate(0, INFO_PANEL_HEIGHT);
        // [UC3.1 - Buoc 2] Gọi drawBoard(g2d) — bắt đầu vẽ bàn cờ
        drawBoard(g2d);
        drawPieces(g2d);
        drawHighlights(g2d);
        g2d.dispose();
    }

    /*
     * UC1.9 - Xac dinh nguoi di truoc
     * Ve thong tin luot di hien tai o phia tren cung
     * 
     * [UC3.4] Thông báo lượt chơi (White/Black turn) trên thanh trạng thái
     * - Phối hợp với UC3.8 để hiển thị kết quả khi ván đấu kết thúc
     * - Font Arial Bold 14, khử răng cưa (ANTIALIASING)
     */
    private void drawTurnInfo(Graphics g) {
        // [UC3.4 - Buoc 2] Vẽ nền thanh + text lượt + nút trên panel
        g.setColor(new Color(50, 50, 50));
        g.fillRect(0, 0, getWidth(), INFO_PANEL_HEIGHT);

        // [UC3.4 - Buoc 4] Vẽ text căn giữa, font Arial Bold 14, màu trắng, khử răng cưa (ANTIALIASING)
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        // [UC3.4 - Buoc 3] getTurnText() lấy text: "Luot di: Trang (White)" hoặc "Luot di: Den (Black)"
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
    // [UC3.1] Hien thi ban co - Bảng màu gỗ Mahogany cao cấp
    // ===============================================================================
    // [UC3.1 - Buoc 1-2] paintComponent() → g2d.translate(0, INFO_PANEL_HEIGHT) → drawBoard(g2d)
    // Duyệt ma trận 8×8, xác định ô sáng/tối qua công thức (r + c) % 2 == 1
    // - Ô tối (isDark == true):  màu gỗ Mahogany (110, 80, 50)
    // - Ô sáng (isDark == false): màu kem tự nhiên (240, 220, 170)
    // CELL = 70px - đủ lớn để click chính xác
    // ===============================================================================
    private void drawBoard(Graphics g) {
        // [UC3.1 - Buoc 3] Duyệt vòng lặp lồng 8×8: for r in 0..7, for c in 0..7
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                // [UC3.1 - Buoc 4] Tính isDark = (r + c) % 2 == 1 để xác định ô sáng/tối
                boolean isDark = (r + c) % 2 == 1;
                // [UC3.1 - Buoc 5-6] isDark==true: màu gỗ Mahogany (110,80,50); isDark==false: màu kem (240,220,170)
                g.setColor(isDark ? new Color(110, 80, 50) : new Color(240, 220, 170));
                // [UC3.1 - Buoc 7] Vẽ ô hình chữ nhật fillRect(c*CELL, r*CELL, CELL, CELL)
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
    // [UC3.5] Vẽ quân cờ (Vua/Thường) + [UC3.7] Hoạt ảnh Di chuyển & Ăn quân
    // ===============================================================================
    // UC3.5:
    //   - Chọn ảnh phù hợp: white/black cho quân thường, whiteKing/blackKing cho Vua
    //   - Kỹ thuật Drop-Shadow 3D: vẽ bóng đổ (0,0,0,60) lệch 5px dưới quân cờ
    //   - Tạo hiệu ứng nổi khối mà không cần thư viện 3D
    //
    // UC3.7 (Phase 1 - SLIDE):
    //   - Bỏ qua ô xuất phát (skipRow, skipCol)
    //   - Tính tọa độ nội suy với Cubic Ease-Out: easeProgress = 1 - (1-t)^3
    //   - Vẽ quân tại vị trí nội suy (curX, curY) có bóng đổ đậm hơn (0,0,0,80)
    //
    // UC3.7 (Phase 2 - FADE):
    //   - Quân bị ăn được vẽ với AlphaComposite.SRC_OVER có alpha = captureFadeAlpha
    //   - captureFadeAlpha giảm từ 1.0 → 0.0, quân tan biến dần
    // ===============================================================================
    private void drawPieces(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        Board board = controller.getBoard();

        // [UC3.7] Bỏ qua ô xuất phát (quân đang trượt) và lưu danh sách quân bị ăn
        int skipRow = -1, skipCol = -1;
        List<Point> capturedPoints = new ArrayList<>();

        if (isAnimating && activeAnimatingMove != null) {
            skipRow = activeAnimatingMove.from().y;        // Ô quân đang rời đi
            skipCol = activeAnimatingMove.from().x;
            capturedPoints = activeAnimatingMove.captures;  // Danh sách quân bị ăn
        }

        // === [UC3.5] Vẽ các quân cờ tĩnh (trừ quân đang chạy) ===
        // [UC3.5 - Buoc 2] Duyệt từng ô (r,c) trên bàn cờ 8×8
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                // [UC3.5 - Buoc 3] Bỏ qua ô đang có quân chạy animation (nếu có)
                if (r == skipRow && c == skipCol) continue;

                // [UC3.5 - Buoc 4] Lấy Piece p tại (r,c), nếu null thì bỏ qua
                Piece p = board.getPiece(r, c);
                if (p == null) continue;

                // [UC3.5 - Buoc 5] Chọn ảnh dựa trên loại quân (Thường/Vua) và màu (Trắng/Đen):
                //   p.isWhite && !p.isKing → whiteImg
                //   p.isWhite &&  p.isKing → whiteKingImg
                //   !p.isWhite && !p.isKing → blackImg
                //   !p.isWhite &&  p.isKing → blackKingImg
                BufferedImage img = p.isWhite ? (p.isKing ? whiteKingImg : whiteImg)
                        : (p.isKing ? blackKingImg : blackImg);

                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

                // [UC3.7 - Phase 2 - Buoc 12] Nếu quân đang bị ăn → AlphaComposite với captureFadeAlpha
                if (isAnimating && capturedPoints.contains(new Point(c, r))) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) captureFadeAlpha));
                }

                // [UC3.5 - Buoc 6] Bóng đổ Drop-Shadow: fillOval lệch 5px dưới quân, alpha 60/255
                g2d.setColor(new Color(0, 0, 0, 60));
                g2d.fillOval(c * CELL + 8, r * CELL + 11, CELL - 16, CELL - 16);

                // [UC3.5 - Buoc 7] Vẽ ảnh quân cờ chính: drawImage căn giữa ô, kích thước CELL-12
                g2d.drawImage(img, c * CELL + 6, r * CELL + 6, CELL - 12, CELL - 12, null);
            }
        }

        // === [UC3.7 - Phase 1] Vẽ quân cờ đang chạy với Cubic Ease-Out ===
        // [UC3.7 - Buoc 6-9] Phase 1: SLIDE (Cubic Ease-Out, ~0.2s)
        if (isAnimating && activeAnimatingMove != null) {
            // Lấy tọa độ xuất phát và đích
            int fr = activeAnimatingMove.from().y;  // Hàng xuất phát
            int fc = activeAnimatingMove.from().x;  // Cột xuất phát
            int tr = activeAnimatingMove.to().y;    // Hàng đích
            int tc = activeAnimatingMove.to().x;    // Cột đích

            Piece movingPiece = board.getPiece(fr, fc);
            if (movingPiece != null) {
                BufferedImage img = movingPiece.isWhite ? (movingPiece.isKing ? whiteKingImg : whiteImg)
                        : (movingPiece.isKing ? blackKingImg : blackImg);

                // [UC3.7 - Buoc 7] Công thức Cubic Ease-Out: easeProgress = 1 - (1-t)^3, chậm dần về cuối
                // easeProgress = 0.0 → 0.488 → 0.875 → 0.992 → 1.0
                double easeProgress = 1.0 - Math.pow(1.0 - slideProgress, 3);

                // [UC3.7 - Buoc 8] Nội suy tọa độ từ (fc, fr) đến (tc, tr)
                int curX = (int) (fc * CELL + (tc - fc) * CELL * easeProgress);
                int curY = (int) (fr * CELL + (tr - fr) * CELL * easeProgress);

                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                // [UC3.7 - Buoc 9] Bóng đổ cho quân đang chạy (đậm hơn: alpha 80)
                g2d.setColor(new Color(0, 0, 0, 80));
                g2d.fillOval(curX + 8, curY + 11, CELL - 16, CELL - 16);

                // Vẽ ảnh quân tại vị trí nội suy
                g2d.drawImage(img, curX + 6, curY + 6, CELL - 12, CELL - 12, null);
            }
        }
        g2d.dispose();
    }

    // ===============================================================================
    // [UC3.8] Thông báo kết quả trận đấu (Deferred Dialog)
    // ===============================================================================
    // Đặc điểm quan trọng: Dialog này KHÔNG được gọi ngay khi phát hiện người thắng.
    // Thay vào đó, nó được gọi TRONG postAnimCallback, tức là SAU KHI animation
    // kết thúc. Điều này tránh xung đột luồng (EDT) và đảm bảo:
    //   1. Animation SLIDE + FADE chạy hoàn chỉnh
    //   2. controller.makeMove() cập nhật Board
    //   3. controller.checkWinner() kiểm tra điều kiện thắng
    //   4. showWinDialog() hiển thị kết quả
    // ===============================================================================
    // [UC3.8 - Buoc 1] Animation kết thúc → postAnimCallback.run() được gọi
    // [UC3.8 - Buoc 2] controller.makeMove(chosen) cập nhật Board (xử lý trong postAnimCallback)
    // [UC3.8 - Buoc 3] controller.checkWinner(controller.getBoard()) — gọi trước khi vào đây
    // [UC3.8 - Buoc 4-5] Kiểm tra winner: WHITE → "Quan TRANG...", BLACK → "Quan DEN..."
    public void showWinDialog(Winner winner) {
        // [UC3.8 - Buoc 6] Nếu winner == Winner.NONE: không hiển thị gì, game tiếp tục
        String message = (winner == Winner.WHITE) 
            ? "Quan TRANG da gianh chien thang!" 
            : "Quan DEN da gianh chien thang!";
        // [UC3.8 - Buoc 7] Hiển thị JOptionPane.showMessageDialog với title "KET THUC TRAN DAU"
        JOptionPane.showMessageDialog(
                this,
                message,
                "KET THUC TRAN DAU",
                JOptionPane.INFORMATION_MESSAGE
        );
        // [UC3.8 - Buoc 8] Người chơi nhấn OK → dialog đóng, trở về timer loop
    }
}