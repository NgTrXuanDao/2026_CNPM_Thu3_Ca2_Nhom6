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
 * UC1.1 - Khởi tạo ván chơi - Đoàn Ngọc Ánh
 * UC1.9 - Xác định người đi trước
 * Người thực hiện: Nhóm 6 → Đoàn Ngọc Ánh (kế thừa chỉnh sửa)
 * Ngày cập nhật: 07/06/2026
 * Nội dung:
 * - UC1.1.1: Khởi tạo Board, gọi initialize() tạo bàn cờ 8x8
 * - UC1.1.3: Tạo JFrame và hiển thị FirstTurnDialog (modal)
 * - UC1.1.5: Lấy FirstTurnMode từ dialog qua getSelectedMode()
 * - UC1.1.6: Khởi tạo GameController với Board và FirstTurnMode
 * - UC1.1.7: Khởi tạo GameView, thêm vào JFrame và hiển thị
 * - UC1.9.4: resolveFirstTurn() xác định whiteTurn từ FirstTurnMode
 * - UC1.9.5: Khởi tạo bàn cờ với lượt đã xác định
 * - UC1.9.6: GameView hiển thị thông tin lượt đi
 */

public class Main {
    public static void main(String[] args) {
        // UC1.1.1 + UC1.7: Khởi tạo Board, gọi initialize() tạo bàn cờ 8x8
        Board board = new Board();

        // UC1.1.3: Tạo JFrame và hiển thị FirstTurnDialog (modal)
        JFrame f = new JFrame("Checkers");
        f.setLayout(new BorderLayout());

        // === UC1.9.1: Hiển thị dialog chọn người đi trước ===
        FirstTurnDialog turnDialog = new FirstTurnDialog(f);
        turnDialog.setVisible(true); // modal -> chờ người dùng chọn (UC1.9.2 - 1.9.3)

        // UC1.1.5: Lấy chế độ đã chọn từ dialog
        FirstTurnMode firstTurnMode = turnDialog.getSelectedMode();

        // UC1.1.6 + UC1.9.5: Khởi tạo GameController với Board và FirstTurnMode
        // resolveFirstTurn() xác định whiteTurn (UC1.9.4)
        GameController controller = new GameController(board, firstTurnMode);

        // UC1.1.7: Tạo GameView và truyền controller
        GameView gameView = new GameView(controller);

        // Cập nhật chế độ chơi đã chọn từ Dialog sang bàn cờ
        gameView.currentChoice = turnDialog.getSelectedGameChoice();

        f.add(gameView, BorderLayout.CENTER);

        // Panel phía dưới chứa nút Restart
        JPanel bottomPanel = new JPanel(new FlowLayout());
        Button restartBtn = new Button("Restart");
        bottomPanel.add(restartBtn);
        f.add(bottomPanel, BorderLayout.SOUTH);

        // Cấu hình frame
        f.setSize(600, 660);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLocationRelativeTo(null);
        
        // UC1.1.7 + UC1.9.6: Hiển thị bàn cờ và thông tin lượt đi
        f.setVisible(true);
    }
}