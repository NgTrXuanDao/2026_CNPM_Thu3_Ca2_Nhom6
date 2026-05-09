package model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Move {
    // UC1.2 - Di chuyển quân cờ: lưu danh sách các ô trên đường đi
    // UC1.4 - Ăn liên tiếp: path chứa nhiều bước trong một lượt ăn liên tiếp
    public final List<Point> path = new ArrayList<>();

    // UC1.3 - Ăn quân: lưu danh sách các quân bị ăn trong nước đi
    // UC1.4 - Ăn liên tiếp: captures chứa nhiều quân bị ăn liên tiếp
    // UC1.13 - Xóa quân bị ăn: danh sách ô cần xóa sau khi thực hiện nước đi
    public final List<Point> captures = new ArrayList<>();

    public Move() {}

    // UC1.2 - Di chuyển quân cờ: khởi tạo nước đi đơn giản từ ô này sang ô khác
    // UC1.10 - Chọn quân & hiển thị nước đi hợp lệ: tạo nước đi để hiển thị gợi ý
    public Move(int fromRow, int fromCol, int toRow, int toCol) {
        path.add(new Point(fromCol, fromRow));
        path.add(new Point(toCol, toRow));
    }

    // UC1.4 - Ăn liên tiếp: thêm từng bước trung gian trong chuỗi ăn liên tiếp
    // UC1.2 - Di chuyển quân cờ: mở rộng đường đi khi vua di chuyển nhiều ô
    // UC1.16 - Vua đi lùi & di chuyển xa hơn: vua có thể đi nhiều bước → cần addStep
    public void addStep(int row, int col) {
        path.add(new Point(col, row));
    }

    // UC1.3 - Ăn quân: ghi nhận ô chứa quân bị ăn
    // UC1.4 - Ăn liên tiếp: ghi nhận từng quân bị ăn trong chuỗi
    // UC1.13 - Xóa quân bị ăn: cung cấp danh sách ô để xóa sau khi di chuyển
    public void addCapture(int row, int col) {
        captures.add(new Point(col, row));
    }

    // UC5.2 - Bắt buộc ăn quân nếu có thể: kiểm tra nước đi có ăn quân không
    // UC5.4 - Chặn nước đi thường khi có thể ăn: phân biệt nước đi thường vs ăn quân
    // UC1.14 - Kiểm tra chuỗi ăn tiếp theo: xác định nước đi có phải ăn không
    public boolean isCapture() {
        return !captures.isEmpty();
    }

    // UC1.2 - Di chuyển quân cờ: lấy ô đích của nước đi
    // UC1.5 - Phong cấp vua: kiểm tra ô đích có phải hàng cuối không
    // UC5.1 - Kiểm tra nước đi hợp lệ: xác định ô đến để validate
    public Point to() {
        if (path.isEmpty()) return null;
        return path.get(path.size() - 1);
    }

    // UC1.2 - Di chuyển quân cờ: lấy ô xuất phát của nước đi
    // UC1.10 - Chọn quân & hiển thị nước đi hợp lệ: xác định quân nào đang được chọn
    public Point from() {
        if (path.isEmpty()) return null;
        return path.get(0);
    }

    // UC1.2 - Di chuyển quân cờ: lấy tọa độ hàng/cột của ô đi và ô đến
    // UC5.1 - Kiểm tra nước đi hợp lệ: truy xuất tọa độ để validate logic
    // UC6.1 - Tính toán nước đi (AI): AI dùng các getter này để đọc nước đi
    public int getFromRow() { return from().y; }
    public int getFromCol() { return from().x; }
    public int getToRow()   { Point t = to(); return t == null ? -1 : t.y; }
    public int getToCol()   { Point t = to(); return t == null ? -1 : t.x; }

    // UC7.3 - Xem lịch sử nước đi: hiển thị thông tin nước đi dưới dạng chuỗi
    // UC6.2 - Đánh giá bàn cờ (debug/log): in thông tin nước đi để kiểm tra AI
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Move:");
        for (Point p : path) sb.append(" (" + p.x + "," + p.y + ")");
        if (isCapture()) {
            sb.append(" captures:");
            for (Point c : captures) sb.append(" (" + c.x + "," + c.y + ")");
        }
        return sb.toString();
    }
}