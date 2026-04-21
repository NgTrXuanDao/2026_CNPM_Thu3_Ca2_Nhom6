# Business Requirements Document (BRD) - Dự án Checkers Elite

**Tên dự án:** Checkers Elite (Cờ Đam Quốc tế)  
**Phiên bản:** 1.1  
**Trạng thái:** Draft  
**Ngày tạo:** 21/04/2026

---

## 1. Giới thiệu dự án
Dự án nhằm tạo ra một ứng dụng Cờ Đam (International Checkers) chuẩn hóa, tập trung vào trải nghiệm người dùng mượt mà và tính chính xác của luật chơi quốc tế.

### 1.1 Mục tiêu
* Xây dựng bộ engine xử lý luật cờ đam 8x8 và 10x10.
* Cung cấp chế độ chơi Offline PvP (người đấu với người) và PvE (đấu với AI đơn giản).
* Tối ưu hóa giao diện cho cả thiết bị di động và máy tính.

---

## 2. Phạm vi dự án (Scope)
### 2.1 Trong phạm vi (In-Scope)
* **Game Engine:** Xử lý di chuyển, bắt quân bắt buộc, phong Vua.
* **Chế độ chơi:** Local PvP (cùng thiết bị) và Solo vs AI (Easy/Medium).
* **Tính năng bổ trợ:** Hoàn tác (Undo), gợi ý nước đi (Hint), đếm thời gian.
* **Giao diện:** Tùy chỉnh màu bàn cờ và loại quân cờ.

### 2.2 Ngoài phạm vi (Out-of-Scope)
* Chế độ Rank/Online Multiplayer (sẽ phát triển ở Phase 2).
* Hệ thống nạp thẻ hoặc mua sắm vật phẩm.

---

## 3. Yêu cầu chức năng (Functional Requirements)

| ID | Tính năng | Mô tả chi tiết | Ưu tiên |
| :--- | :--- | :--- | :--- |
| **FR-1** | **Luật ăn quân** | Bắt buộc phải ăn quân nếu có cơ hội. Nếu có nhiều nhánh ăn, phải chọn nhánh ăn được NHIỀU quân nhất. | **Critical** |
| **FR-2** | **Flying Kings** | Quân Vua có thể di chuyển và ăn quân theo đường chéo bất kỳ khoảng cách nào (giống tượng trong cờ vua). | **High** |
| **FR-3** | **Undo/Redo** | Cho phép người chơi quay lại tối đa 5 nước đi trước đó. | **Medium** |
| **FR-4** | **Save Game** | Tự động lưu trạng thái bàn cờ khi người chơi thoát ứng dụng đột ngột. | **High** |
| **FR-5** | **AI Opponent** | Phát triển thuật toán Minimax đơn giản để người chơi luyện tập. | **Medium** |

---

## 4. Quy tắc nghiệp vụ (Business Rules)
* **BR-01:** Bàn cờ 8x8 hoặc 10x10. Quân cờ chỉ di chuyển trên các ô tối.
* **BR-02:** Quân thường chỉ đi tiến, nhưng khi ăn quân có thể nhảy lùi (theo luật quốc tế).
* **BR-03:** Một quân cờ chỉ được phong Vua nếu kết thúc lượt đi tại hàng cuối cùng của đối phương.

---

## 5. Yêu cầu phi chức năng (Non-Functional Requirements)
* **Hiệu năng:** Thời gian phản hồi của AI cho mỗi nước đi không quá 2 giây.
* **Độ tin cậy:** Ứng dụng phải hoạt động ổn định không cần kết nối Internet.
* **Giao diện:** Đạt chuẩn hiển thị trên các màn hình có tỉ lệ từ 16:9 đến 21:9.

---

## 6. Phác thảo giao diện (UI Mockup Concept)
```text
[  Player 2 Info - 00:05:00  ]
------------------------------
| [ ] [O] [ ] [O] [ ] [O] [ ] |
| [O] [ ] [O] [ ] [O] [ ] [O] |
| [ ] [ ] [ ] [ ] [ ] [ ] [ ] |
| [ ] [X] [ ] [ ] [ ] [ ] [ ] |
| [X] [ ] [X] [ ] [X] [ ] [X] |
------------------------------
[  Player 1 Info - 00:04:30  ]
[ Undo ] [ Hint ] [ New Game ]
