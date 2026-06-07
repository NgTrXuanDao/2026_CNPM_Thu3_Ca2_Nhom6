package view;

import model.FirstTurnMode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/*
 * UC1.9 - Xác định người đi trước - Đoàn Ngọc Ánh
 * Người thực hiện: Nhóm 6 → Đoàn Ngọc Ánh (kế thừa chỉnh sửa)
 * Ngày cập nhật: 07/06/2026
 * Nội dung:
 * - UC1.9.1: Hiển thị dialog "Chọn người đi trước" với 3 tùy chọn
 * - UC1.9.2: Người chơi chọn 1 trong 3 tùy chọn
 * - UC1.9.3: Người chơi nhấn nút "Bắt đầu"
 * - Trả về kết quả FirstTurnMode để Controller xử lý UC1.9.4
 */
public class FirstTurnDialog extends JDialog {

    /** Kết quả chế độ lượt đi được chọn */
    private FirstTurnMode selectedMode = FirstTurnMode.WHITE;

    private int selectedGameChoice = 4;

    private CardLayout cardLayout;
    private JPanel mainCardPanel;

    /**
     * Constructor: Tạo dialog chọn người đi trước
     * @param parent JFrame cha để căn giữa dialog
     */
    public FirstTurnDialog(JFrame parent) {
        super(parent, "Chọn người đi trước", true); // modal dialog
        setSize(400, 320);
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initUI();
    }

    // UC1.9.1 - Hệ thống hiển thị dialog "Chọn người đi trước" với 3 tùy chọn
    /**
     * Khởi tạo giao diện dialog
     * - Panel chính với title, các radio button và nút Start
     */
    private void initUI() {
        cardLayout = new CardLayout();
        mainCardPanel = new JPanel(cardLayout);

        // --- PANEL TẦNG ĐẦU TIÊN: CHỌN ĐẤU NGƯỜI HOẶC ĐẤU MÁY ---
        JPanel modePanel = new JPanel(new BorderLayout(10, 10));
        JLabel modeTitle = new JLabel("Chọn chế độ chơi chính", SwingConstants.CENTER);
        modeTitle.setFont(new Font("Arial", Font.BOLD, 16));
        modeTitle.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        modePanel.add(modeTitle, BorderLayout.NORTH);

        JPanel modeCenter = new JPanel(new GridLayout(2, 1, 15, 15));
        modeCenter.setBorder(BorderFactory.createEmptyBorder(20, 40, 30, 40));
        JButton btnPvP = new JButton("Người đấu với Người (PvP)");
        JButton btnPvE = new JButton("Đấu với Máy (AI)");
        btnPvP.setFont(new Font("Arial", Font.BOLD, 14));
        btnPvE.setFont(new Font("Arial", Font.BOLD, 14));

        modeCenter.add(btnPvP);
        modeCenter.add(btnPvE);
        modePanel.add(modeCenter, BorderLayout.CENTER);

        // --- PANEL TẦNG 2: CHỌN ĐỘ KHÓ (NẾU LÀ ĐẤU MÁY) ---
        JPanel diffPanel = new JPanel(new BorderLayout(10, 10));
        JLabel diffTitle = new JLabel("Chọn độ khó của Máy", SwingConstants.CENTER);
        diffTitle.setFont(new Font("Arial", Font.BOLD, 16));
        diffTitle.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        diffPanel.add(diffTitle, BorderLayout.NORTH);

        JPanel diffCenter = new JPanel(new GridLayout(3, 1, 10, 10));
        diffCenter.setBorder(BorderFactory.createEmptyBorder(15, 50, 20, 50));
        JButton btnEasy = new JButton("Cấp độ: DỄ");
        JButton btnMedium = new JButton("Cấp độ: TRUNG BÌNH");
        JButton btnHard = new JButton("Cấp độ: KHÓ");
        diffCenter.add(btnEasy);
        diffCenter.add(btnMedium);
        diffCenter.add(btnHard);
        diffPanel.add(diffCenter, BorderLayout.CENTER);

        // --- UC1.9.1: PANEL CHỌN NGƯỜI ĐI TRƯỚC (3 tùy chọn) ---
        JPanel turnPanel = new JPanel(new BorderLayout(10, 10));
        JLabel titleLabel = new JLabel("Chọn người đi trước", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        turnPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));

        // UC1.9.1: 3 tùy chọn radio button: Trắng, Đen, Random
        JRadioButton whiteFirstBtn = new JRadioButton("Trắng (White) đi trước", true);
        JRadioButton blackFirstBtn = new JRadioButton("Đen (Black) đi trước", false);
        JRadioButton randomBtn = new JRadioButton("Random ngẫu nhiên", false);

        ButtonGroup group = new ButtonGroup();
        group.add(whiteFirstBtn);
        group.add(blackFirstBtn);
        group.add(randomBtn);

        Font radioFont = new Font("Arial", Font.PLAIN, 15);
        whiteFirstBtn.setFont(radioFont);
        blackFirstBtn.setFont(radioFont);
        randomBtn.setFont(radioFont);

        centerPanel.add(whiteFirstBtn);
        centerPanel.add(blackFirstBtn);
        centerPanel.add(randomBtn);
        turnPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton startBtn = new JButton("Bắt đầu");
        startBtn.setFont(new Font("Arial", Font.BOLD, 14));
        startBtn.setPreferredSize(new Dimension(150, 40));

        // UC1.9.2 + UC1.9.3: Người chơi chọn tùy chọn và nhấn nút "Bắt đầu"
        startBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // UC1.9.2: Xác định lựa chọn của người chơi
                if (whiteFirstBtn.isSelected()) {
                    selectedMode = FirstTurnMode.WHITE;
                } else if (blackFirstBtn.isSelected()) {
                    selectedMode = FirstTurnMode.BLACK;
                } else if (randomBtn.isSelected()) {
                    selectedMode = FirstTurnMode.RANDOM;
                }
                dispose(); // UC1.9.3: Đóng dialog sau khi chọn
            }
        });

        bottomPanel.add(startBtn);
        turnPanel.add(bottomPanel, BorderLayout.SOUTH);

        // --- ĐIỀU HƯỚNG LUỒNG SỰ KIỆN CHUYỂN PANEL ---
        btnPvP.addActionListener(e -> {
            selectedGameChoice = 4;
            cardLayout.show(mainCardPanel, "TurnPage");
        });
        btnPvE.addActionListener(e -> {
            cardLayout.show(mainCardPanel, "DiffPage");
        });

        btnEasy.addActionListener(e -> { selectedGameChoice = 2; cardLayout.show(mainCardPanel, "TurnPage"); });
        btnMedium.addActionListener(e -> { selectedGameChoice = 3; cardLayout.show(mainCardPanel, "TurnPage"); });
        btnHard.addActionListener(e -> { selectedGameChoice = 1; cardLayout.show(mainCardPanel, "TurnPage"); });

        mainCardPanel.add(modePanel, "ModePage");
        mainCardPanel.add(diffPanel, "DiffPage");
        mainCardPanel.add(turnPanel, "TurnPage");

        add(mainCardPanel);
        cardLayout.show(mainCardPanel, "ModePage");
    }

    public int getSelectedGameChoice() {
        return selectedGameChoice;
    }

    /**
     * Lấy chế độ người đi trước đã chọn
     * @return FirstTurnMode đã chọn (mặc định WHITE nếu chưa chọn)
     */
    public FirstTurnMode getSelectedMode() {
        return selectedMode;
    }
}