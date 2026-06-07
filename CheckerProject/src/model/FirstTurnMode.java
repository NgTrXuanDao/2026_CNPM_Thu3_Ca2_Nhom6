package model;

/*
 * UC1.9 - Xác định người đi trước - Đoàn Ngọc Ánh
 * Người thực hiện: Nhóm 6 → Đoàn Ngọc Ánh (kế thừa chỉnh sửa)
 * Ngày cập nhật: 07/06/2026
 * Nội dung:
 * - Enum định nghĩa 3 chế độ chọn người đi trước (UC1.9.1)
 * - WHITE: Trắng đi trước
 * - BLACK: Đen đi trước
 * - RANDOM: Random ngẫu nhiên
 */
public enum FirstTurnMode {
    /** Người chơi Trắng (White) đi trước */
    WHITE,
    /** Người chơi Đen (Black) đi trước */
    BLACK,
    /** Random ngẫu nhiên người đi trước */
    RANDOM
}
