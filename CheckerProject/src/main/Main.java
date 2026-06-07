package main;

import controller.GameController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import model.Board;
import model.FirstTurnMode;
import view.FirstTurnDialog;
import view.GameView;
import view.HistoryPanel;

/*
 * UC1.9 - Xac dinh nguoi di truoc
 * Nguoi thuc hien: Nhom 6
 * Ngay cap nhat: 02/06/2026
 * Noi dung:
 * - Hien thi dialog chon nguoi di truoc khi bat dau game
 * - Truyen FirstTurnMode vao GameController
 */

/**
 * UC 7.1, 7.2, 7.3 - Lịch sử nước đi, lưu và tải lịch sử   
 * Người thực hiện: Nguyễn Trần Xuân Đào
 * Ngày thực hiện: 07/06/2026
 * Nội dung:
 * Tạo HistoryPanel (UC7.3) và truyền vào GameView
 * Đặt HistoryPanel ở EAST trong BorderLayout
 * buildToolBar() thêm nút: Lưu, Tải, Lưu As, Xuất lịch sử
 * Restart button: nối trực tiếp với controller.resetGame() + historyPanel.clear()
 * Frame width tăng 200px (chỗ cho HistoryPanel)
 */

public class Main {
    public static void main(String[] args) {
        Board board = new Board();

        // Tao frame chinh (an cho den khi dialog hoan tat)
        JFrame f = new JFrame("Checkers");
        f.setLayout(new BorderLayout());

        // === UC1.9: Hien thi dialog chon nguoi di truoc ===
        FirstTurnDialog turnDialog = new FirstTurnDialog(f);
        turnDialog.setVisible(true); // modal -> cho nguoi dung chon

        // Lay che do da chon tu dialog
        FirstTurnMode firstTurnMode = turnDialog.getSelectedMode();

        // Khoi tao GameController voi che do nguoi di truoc da chon
        GameController controller = new GameController(board, firstTurnMode);

        // === UC7.3: Tao HistoryPanel ===
        HistoryPanel historyPanel = new HistoryPanel();

        // Tao GameView va truyen controller
         GameView gameView = new GameView(controller, historyPanel);

        // Cập nhật chế độ chơi đã chọn từ Dialog sang bàn cờ
        gameView.currentChoice = turnDialog.getSelectedGameChoice();

        f.add(gameView, BorderLayout.CENTER);
        f.add(historyPanel, BorderLayout.EAST);   // UC7.3 – lich su nuoc di

        // Panel phia duoi chua cac nut cong cu
        JPanel bottomPanel = buildToolBar(gameView, controller, historyPanel, firstTurnMode);
        f.add(bottomPanel, BorderLayout.SOUTH);

        // Cau hinh frame
        // Width: 560 (board) + 200 (history) = 760; Height: giu nguyen 660
        f.setSize(760, 660);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLocationRelativeTo(null); // can giua man hinh
        f.setResizable(false);
        f.setVisible(true);
 }
     /**
     * Xây dựng thanh công cụ phía dưới JFrame.
     * Chứa: Restart | Lưu | Tải | Lưu As | Xuất lịch sử
     *
     * @param gameView     GameView để gọi onSaveClicked(), onLoadClicked(), ...
     * @param controller   GameController để gọi resetGame()
     * @param historyPanel HistoryPanel để gọi clear() khi Restart
     * @param firstMode    Chế độ người đi trước (dùng lại khi Restart)
     * @return             JPanel đã build xong
     *
     * ĐƯỢC GỌI BỞI: main() một lần duy nhất khi khởi tạo UI
     */
    private static JPanel buildToolBar(GameView gameView,
                                       GameController controller,
                                       HistoryPanel historyPanel,
                                       FirstTurnMode firstMode) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        panel.setBackground(new Color(50, 50, 50));
 
        // ── Nút Restart ──────────────────────────────────────────────────────
        // UC1.1 – Khởi tạo lại ván chơi; UC7.3 – xóa lịch sử cũ
        JButton btnRestart = makeButton("Restart", new Color(70, 130, 180));
        btnRestart.addActionListener(e -> {
            controller.resetGame(firstMode); // UC1.1 + UC7.3 (clear history bên trong)
            historyPanel.clear();            // UC7.3: xóa hiển thị HistoryPanel
            gameView.repaint();
        });
 
        // ── UC7.1: Nút Lưu game ──────────────────────────────────────────────
        // → GameView.onSaveClicked() → controller.saveGame() → SaveLoadManager
        JButton btnSave = makeButton("Luu game", new Color(34, 139, 34));
        btnSave.setToolTipText("Luu van hien tai vao file mac dinh (checkers_save.dat)");
        btnSave.addActionListener(e -> gameView.onSaveClicked());
 
        // ── UC7.2: Nút Tải game ──────────────────────────────────────────────
        // → GameView.onLoadClicked() → controller.loadGame() → SaveLoadManager
        JButton btnLoad = makeButton("Tai game", new Color(180, 120, 20));
        btnLoad.setToolTipText("Tai lai van da luu tu file mac dinh (checkers_save.dat)");
        btnLoad.addActionListener(e -> gameView.onLoadClicked());
 
        // ── UC7.1: Nút Lưu As ────────────────────────────────────────────────
        // → GameView.onSaveAsClicked() → JFileChooser → controller.saveGame(path)
        JButton btnSaveAs = makeButton("Luu As...", new Color(20, 100, 100));
        btnSaveAs.setToolTipText("Luu van vao file tuy chon");
        btnSaveAs.addActionListener(e -> gameView.onSaveAsClicked());
 
        // ── UC7.3: Nút Xuất lịch sử ──────────────────────────────────────────
        // → GameView.onExportHistoryClicked() → JFileChooser → PrintWriter → .txt
        JButton btnExport = makeButton("Xuat lich su", new Color(100, 60, 160));
        btnExport.setToolTipText("Xuat toan bo lich su nuoc di ra file .txt");
        btnExport.addActionListener(e -> gameView.onExportHistoryClicked());
 
        panel.add(btnRestart);
        panel.add(btnSave);
        panel.add(btnLoad);
        panel.add(btnSaveAs);
        panel.add(btnExport);
 
        return panel;
    }
 
    /**
     * Helper: tạo JButton với style thống nhất cho toolbar.
     *
     * @param text  Nhãn hiển thị trên nút
     * @param color Màu nền nút
     * @return      JButton đã được style sẵn
     */
    private static JButton makeButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 30));
        return btn;
    }
}