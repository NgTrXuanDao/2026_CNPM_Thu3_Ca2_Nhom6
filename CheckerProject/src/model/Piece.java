package model;

// UC1.8 - Đặt quân ban đầu - Đoàn Ngọc Ánh
// UC1.9 - Xác định người đi trước
// UC1.11 - Đi chéo 1 ô (không lùi trừ vua)
public class Piece {
    // UC1.9: Phân biệt quân trắng/đen để xác định lượt
    // UC5.1 - Kiểm tra nước đi hợp lệ: xác định quân thuộc bên nào để validate
    // UC1.17 - Hết quân → thua: đếm quân theo màu để kiểm tra điều kiện thua
    public boolean isWhite;

    // UC1.11.2: Xác định quân vua có thể đi 4 hướng (cả tiến lẫn lùi)
    // UC1.5 - Phong cấp – vua: đánh dấu quân đã được phong cấp thành vua
    // UC1.16 - Vua đi lùi & di chuyển xa hơn: kiểm tra isKing để mở rộng nước đi
    public boolean isKing;

    // UC1.8.1/1.8.3: Khởi tạo quân cờ thường (chưa phong vua) khi đặt quân ban đầu
    // UC1.1 - Khởi tạo ván chơi: tạo các quân cờ khi bắt đầu ván mới
    public Piece(boolean isWhite) {
        this.isWhite = isWhite;
        this.isKing = false;
    }

    // UC1.5 - Phong cấp – vua: tạo quân với trạng thái vua khi copy/restore
    // UC6.1 - Tính toán nước đi (AI): AI tạo bản sao quân với đầy đủ trạng thái
    // UC7.1 - Lưu trạng thái game: khôi phục quân cờ với đúng trạng thái isKing
    public Piece(boolean isWhite, boolean isKing) {
        this.isWhite = isWhite;
        this.isKing = isKing;
    }

    // UC6.1 - Tính toán nước đi (AI): AI cần deep copy quân để duyệt cây Minimax
    // UC6.3 - Minimax – Medium: sao chép trạng thái quân khi tạo bàn cờ giả lập
    // UC6.4 - Alpha-Beta Pruning – Hard: tương tự, cần copy quân không ảnh hưởng bản gốc
    // UC7.1 - Lưu trạng thái game: tạo bản sao quân khi lưu trạng thái ván đấu
    public Piece copy() {
        return new Piece(this.isWhite, this.isKing);
    }

    // UC3.1 - Hiển thị bàn cờ: render ký hiệu quân (W/B/Wk/Bk) lên giao diện
    // UC3.5 - Hiển thị quân thường & vua: phân biệt hiển thị quân thường vs vua
    // UC7.3 - Xem lịch sử nước đi: in ký hiệu quân khi log lịch sử nước đi
    @Override
    public String toString() {
        if (isWhite) return isKing ? "Wk" : "W";
        else return isKing ? "Bk" : "B";
    }
}