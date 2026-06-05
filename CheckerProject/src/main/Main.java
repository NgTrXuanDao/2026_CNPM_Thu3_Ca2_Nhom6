package main;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import controller.GameController;
import model.Board;
import model.FirstTurnMode;
import view.FirstTurnDialog;
import view.GameView;

/*
 * UC1.9 - Xac dinh nguoi di truoc
 * Nguoi thuc hien: Nhom 6
 * Ngay cap nhat: 02/06/2026
 * Noi dung:
 * - Hien thi dialog chon nguoi di truoc khi bat dau game
 * - Truyen FirstTurnMode vao GameController
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

        // Tao GameView va truyen controller
        GameView gameView = new GameView(controller);

        // Cập nhật chế độ chơi đã chọn từ Dialog sang bàn cờ
        gameView.currentChoice = turnDialog.getSelectedGameChoice();

        f.add(gameView, BorderLayout.CENTER);

        // Panel phia duoi chua nut Restart
        JPanel bottomPanel = new JPanel(new FlowLayout());
        Button restartBtn = new Button("Restart");
        bottomPanel.add(restartBtn);
        f.add(bottomPanel, BorderLayout.SOUTH);

        // Cau hinh frame
        f.setSize(600, 660);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}