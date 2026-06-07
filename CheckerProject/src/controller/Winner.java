package controller;

/**
 * Enum Winner – kết quả game.
 *
 * UC1.6  - Kiểm tra trạng thái
 * UC1.17 - Hết quân → thua
 * UC1.18 - Hết nước đi → thua
 * UC1.19 - Lập trạng thái / không ăn lâu → hòa
 * Người thực hiện: Nguyễn Khánh Duy
 * Ngày cập nhật: 07/06/2026
 * Nội dung:
 * - Thêm DRAW để biểu diễn trạng thái hòa (UC1.19)
 *   → xảy ra khi 40 lượt liên tiếp không có capture
 * - WHITE: White thắng (Black hết quân hoặc hết nước đi)
 * - BLACK: Black thắng (White hết quân hoặc hết nước đi)
 * - DRAW:  Hòa (không ăn quân đủ 40 lượt liên tiếp)
 * - NONE:  Game vẫn đang chơi, chưa có kết quả
 */
public enum Winner {
    WHITE,
    BLACK,

    /*
     * UC1.19 - Lập trạng thái / không ăn lâu → hòa
     * Trạng thái hòa: xảy ra khi đủ DRAW_LIMIT lượt không capture liên tiếp
     */
    DRAW,

    NONE
}
