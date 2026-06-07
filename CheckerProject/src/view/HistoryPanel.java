package view;

import controller.MoveHistoryManager;
import model.MoveRecord;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

public class HistoryPanel extends JPanel {

    // ─── UI COMPONENTS ───────────────────────────────────────────────────────

    /** Model dữ liệu của JList – chứa các chuỗi hiển thị */
    private final DefaultListModel<MoveRecord> listModel;

    /** Danh sách hiển thị lịch sử nước đi */
    private final JList<MoveRecord> historyList;

    /** Label đếm tổng số nước đã đi */
    private final JLabel countLabel;

    /** Label hiển thị nước đi cuối cùng (dễ đọc) */
    private final JLabel lastMoveLabel;

    // ─── CONSTANTS ────────────────────────────────────────────────────────────

    /** Màu nền ô nước Trắng */
    private static final Color COLOR_WHITE_MOVE  = new Color(245, 240, 220);
    /** Màu nền ô nước Đen */
    private static final Color COLOR_BLACK_MOVE  = new Color(200, 195, 180);
    /** Màu nền ô nước ăn quân (nổi bật hơn) */
    private static final Color COLOR_CAPTURE     = new Color(255, 220, 150);
    /** Màu nền ô phong vua */
    private static final Color COLOR_PROMOTION   = new Color(200, 230, 255);
    /** Chiều rộng panel */
    private static final int PANEL_WIDTH = 200;

    // ─── CONSTRUCTOR ─────────────────────────────────────────────────────────

    /**
     * UC7.3 – Khởi tạo HistoryPanel với đầy đủ các component UI.
     * Được gọi một lần duy nhất trong Main.main() khi tạo giao diện chính.
     *
     * LUỒNG XỬ LÝ:
     *   1. Thiết lập layout BorderLayout
     *   2. Tạo label tiêu đề (NORTH)
     *   3. Tạo JList + JScrollPane (CENTER)
     *   4. Tạo panel thống kê (SOUTH)
     *   5. Đặt custom CellRenderer để tô màu phân biệt các loại nước đi
     */
    public HistoryPanel() {
        setLayout(new BorderLayout(4, 4));
        setPreferredSize(new Dimension(PANEL_WIDTH, 600));
        setBorder(new TitledBorder("Lịch sử nước đi"));
        setBackground(new Color(245, 245, 240));

        // ── NORTH: Tiêu đề ──
        JLabel titleLabel = new JLabel("📋  Lịch sử nước đi", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 13));
        titleLabel.setBorder(new EmptyBorder(4, 0, 4, 0));
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(80, 60, 40));
        titleLabel.setForeground(Color.WHITE);
        add(titleLabel, BorderLayout.NORTH);

        // ── CENTER: Danh sách nước đi ──
        listModel   = new DefaultListModel<>();
        historyList = new JList<>(listModel);
        historyList.setFont(new Font("Monospaced", Font.PLAIN, 11));
        historyList.setFixedCellHeight(22);
        historyList.setCellRenderer(new MoveRecordCellRenderer());
        historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(historyList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        // ── SOUTH: Thống kê ──
        JPanel southPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        southPanel.setBackground(getBackground());
        southPanel.setBorder(new EmptyBorder(4, 4, 4, 4));

        lastMoveLabel = new JLabel("Nước cuối: —", SwingConstants.CENTER);
        lastMoveLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        lastMoveLabel.setForeground(new Color(80, 80, 80));

        countLabel = new JLabel("Tổng: 0 nước", SwingConstants.CENTER);
        countLabel.setFont(new Font("Arial", Font.BOLD, 11));

        southPanel.add(lastMoveLabel);
        southPanel.add(countLabel);
        add(southPanel, BorderLayout.SOUTH);
    }

    // ─── PUBLIC METHODS ───────────────────────────────────────────────────────

    /**
     * UC7.3 – Cập nhật toàn bộ danh sách hiển thị từ MoveHistoryManager.
     * Được gọi sau mỗi nước đi (cả người chơi và AI).
     *
     * @param records Danh sách MoveRecord mới nhất từ MoveHistoryManager.getRecords()
     *
     * LUỒNG XỬ LÝ:
     *   1. Xóa listModel cũ
     *   2. Thêm từng MoveRecord vào listModel
     *   3. Cập nhật countLabel + lastMoveLabel
     *   4. Cuộn JScrollPane xuống cuối (ensureIndexIsVisible)
     *
     * ĐƯỢC GỌI BỞI: GameView sau mỗi lần gọi controller.makeMove()
     */
    public void updateHistory(List<MoveRecord> records) {
        listModel.clear();
        for (MoveRecord record : records) {
            listModel.addElement(record);
        }

        int total = records.size();
        countLabel.setText("Tổng: " + total + " nước");

        if (total > 0) {
            MoveRecord last = records.get(total - 1);
            lastMoveLabel.setText("Cuối: " + last.notation);
            // Cuộn xuống nước mới nhất
            historyList.ensureIndexIsVisible(listModel.getSize() - 1);
        } else {
            lastMoveLabel.setText("Nước cuối: —");
        }

        repaint();
    }

    /**
     * UC7.3 – Xóa toàn bộ hiển thị (gọi khi Restart hoặc New Game).
     * ĐƯỢC GỌI BỞI: GameView.onRestartClicked()
     */
    public void clear() {
        listModel.clear();
        countLabel.setText("Tổng: 0 nước");
        lastMoveLabel.setText("Nước cuối: —");
        repaint();
    }

    // ─── INNER CLASS: CELL RENDERER ───────────────────────────────────────────

    /**
     * UC7.3 – Custom renderer để tô màu phân biệt các loại nước đi:
     *   - Nước Trắng: nền vàng nhạt
     *   - Nước Đen: nền xám nhạt
     *   - Nước ăn quân: nền cam nhạt (ưu tiên hơn màu bên)
     *   - Nước phong vua: nền xanh nhạt (ưu tiên cao nhất)
     */
    private static class MoveRecordCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof MoveRecord) {
                MoveRecord record = (MoveRecord) value;
                setText(record.toString());

                if (!isSelected) {
                    // Ưu tiên màu: phong vua > ăn quân > màu bên
                    if (record.isPromotion) {
                        setBackground(COLOR_PROMOTION);
                    } else if (record.isCapture) {
                        setBackground(COLOR_CAPTURE);
                    } else if (record.isWhite) {
                        setBackground(COLOR_WHITE_MOVE);
                    } else {
                        setBackground(COLOR_BLACK_MOVE);
                    }
                }

                // Tooltip khi hover: hiển thị chi tiết
                String tip = String.format("Nước %d | Bên: %s | Ăn: %d quân | Phong vua: %s",
                        record.moveNumber,
                        record.isWhite ? "Trắng" : "Đen",
                        record.captureCount,
                        record.isPromotion ? "Có ♛" : "Không");
                setToolTipText(tip);

                // Font đậm nếu là nước ăn liên tiếp (>= 2 quân)
                if (record.captureCount >= 2) {
                    setFont(getFont().deriveFont(Font.BOLD));
                }
            }

            return this;
        }
    }
}