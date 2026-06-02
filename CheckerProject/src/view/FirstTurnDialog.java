package view;

import model.FirstTurnMode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/*
 * UC1.9 - Xác định người đi trước
 * Người thực hiện: Nhóm 6
 * Ngày cập nhật: 02/06/2026
 * Nội dung:
 * - Tạo dialog cho phép người dùng chọn người đi trước
 * - Hỗ trợ 3 chế độ: White first, Black first, Random
 * - Trả về kết quả FirstTurnMode để Controller xử lý
 */
public class FirstTurnDialog extends JDialog {

    /** Kết quả chế độ lượt đi được chọn */
    private FirstTurnMode selectedMode = FirstTurnMode.WHITE;

    /**
     * Constructor: Tạo dialog chọn người đi trước
     * @param parent JFrame cha để căn giữa dialog
     */
    public FirstTurnDialog(JFrame parent) {
        super(parent, "Chọn người đi trước", true); // modal dialog
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initUI();
    }

    /**
     * Khởi tạo giao diện dialog
     * - Panel chính với title, các radio button và nút Start
     */
    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // === Panel tiêu đề ===
        JLabel titleLabel = new JLabel("Chọn người đi trước", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        add(titleLabel, BorderLayout.NORTH);

        // === Panel chứa các radio button ===
        JPanel centerPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));

        // Radio button cho từng chế độ
        JRadioButton whiteFirstBtn = new JRadioButton("Trắng (White) đi trước", true);
        JRadioButton blackFirstBtn = new JRadioButton("Đen (Black) đi trước", false);
        JRadioButton randomBtn = new JRadioButton("Random ngẫu nhiên", false);

        // Nhóm các radio button để chỉ chọn 1
        ButtonGroup group = new ButtonGroup();
        group.add(whiteFirstBtn);
        group.add(blackFirstBtn);
        group.add(randomBtn);

        // Style cho radio button
        Font radioFont = new Font("Arial", Font.PLAIN, 15);
        whiteFirstBtn.setFont(radioFont);
        blackFirstBtn.setFont(radioFont);
        randomBtn.setFont(radioFont);

        centerPanel.add(whiteFirstBtn);
        centerPanel.add(blackFirstBtn);
        centerPanel.add(randomBtn);
        add(centerPanel, BorderLayout.CENTER);

        // === Panel chứa nút Start ===
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton startBtn = new JButton("Bắt đầu");
        startBtn.setFont(new Font("Arial", Font.BOLD, 14));
        startBtn.setPreferredSize(new Dimension(150, 40));

        // Xử lý sự kiện khi nhấn nút Bắt đầu
        startBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Xác định chế độ được chọn
                if (whiteFirstBtn.isSelected()) {
                    selectedMode = FirstTurnMode.WHITE;
                } else if (blackFirstBtn.isSelected()) {
                    selectedMode = FirstTurnMode.BLACK;
                } else if (randomBtn.isSelected()) {
                    selectedMode = FirstTurnMode.RANDOM;
                }
                dispose(); // đóng dialog
            }
        });

        bottomPanel.add(startBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Lấy chế độ người đi trước đã chọn
     * @return FirstTurnMode đã chọn (mặc định WHITE nếu chưa chọn)
     */
    public FirstTurnMode getSelectedMode() {
        return selectedMode;
    }
}
