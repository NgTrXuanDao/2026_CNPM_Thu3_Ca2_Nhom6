package controller;

import model.Board;
import model.GameState;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SaveLoadManager {

    // ─── CONSTANTS ────────────────────────────────────────────────────────────

    /** Đường dẫn file save mặc định (cùng thư mục chạy JAR) */
    public static final String DEFAULT_SAVE_PATH = "checkers_save.dat";

    /** Format timestamp dùng trong tên file save tự động */
    private static final String TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss";

    // ─── UC7.1 – SAVE METHODS ────────────────────────────────────────────────

    /**
     * UC7.1 – Lưu trạng thái game vào file mặc định "checkers_save.dat".
     * Đây là overload tiện dụng – gọi saveGame(filePath) bên trong.
     *
     * @param whiteTurn   Lượt hiện tại (true = Trắng)
     * @param board       Bàn cờ hiện tại cần lưu
     * @param moveHistory Danh sách notation lịch sử nước đi (từ MoveHistoryManager)
     * @return            true nếu lưu thành công, false nếu có lỗi IO
     *
     * ĐƯỢC GỌI BỞI: GameView (khi người dùng nhấn nút "Lưu game")
     *               GameController.saveGame()
     */
    public static boolean saveGame(boolean whiteTurn, Board board, List<String> moveHistory) {
        return saveGame(whiteTurn, board, moveHistory, DEFAULT_SAVE_PATH);
    }

    /**
     * UC7.1 – Lưu trạng thái game vào file với đường dẫn chỉ định.
     * Quy trình:
     *   1. Tạo GameState từ (whiteTurn, board, moveHistory)
     *   2. Mở FileOutputStream → ObjectOutputStream
     *   3. Ghi GameState object (Java serialization)
     *   4. Đóng stream (try-with-resources tự động)
     *
     * @param whiteTurn   Lượt hiện tại
     * @param board       Bàn cờ cần lưu
     * @param moveHistory Lịch sử nước đi dạng List<String>
     * @param filePath    Đường dẫn file đầu ra (ví dụ: "C:/saves/game1.dat")
     * @return            true nếu ghi file thành công, false nếu exception
     *
     * ĐƯỢC GỌI BỞI: saveGame(whiteTurn, board, moveHistory) — overload mặc định
     *               GameView.onSaveAsClicked() — khi người dùng chọn "Lưu As..."
     */
    public static boolean saveGame(boolean whiteTurn, Board board,
                                   List<String> moveHistory, String filePath) {
        // Tạo đối tượng snapshot trạng thái hiện tại
        GameState state = new GameState(whiteTurn, board, moveHistory);

        // Ghi ra file bằng Java Object Serialization
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(filePath)))) {

            oos.writeObject(state);
            System.out.println("[SaveLoadManager] Đã lưu game: " + filePath
                               + " | " + state);
            return true;

        } catch (IOException e) {
            System.err.println("[SaveLoadManager] Lỗi khi lưu game: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * UC7.1 – Lưu game với tên file tự động theo timestamp.
     * Ví dụ tạo file: "checkers_20260604_143022.dat"
     * Hữu ích khi muốn giữ nhiều file save (không ghi đè).
     *
     * @param whiteTurn   Lượt hiện tại
     * @param board       Bàn cờ cần lưu
     * @param moveHistory Lịch sử nước đi
     * @return            Đường dẫn file đã lưu, hoặc null nếu thất bại
     *
     * ĐƯỢC GỌI BỞI: GameView.onAutoSaveClicked()
     */
    public static String saveGameWithTimestamp(boolean whiteTurn, Board board,
                                               List<String> moveHistory) {
        String timestamp = new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
        String filePath  = "checkers_" + timestamp + ".dat";
        boolean ok = saveGame(whiteTurn, board, moveHistory, filePath);
        return ok ? filePath : null;
    }

    // ─── UC7.2 – LOAD METHODS ────────────────────────────────────────────────

    /**
     * UC7.2 – Tải trạng thái game từ file mặc định "checkers_save.dat".
     * Overload tiện dụng – gọi loadGame(filePath) bên trong.
     *
     * @return GameState đã đọc, hoặc null nếu file không tồn tại / lỗi IO
     *
     * ĐƯỢC GỌI BỞI: GameView (khi nhấn "Tải game")
     *               GameController.loadGame()
     */
    public static GameState loadGame() {
        return loadGame(DEFAULT_SAVE_PATH);
    }

    /**
     * UC7.2 – Tải trạng thái game từ file chỉ định.
     * Quy trình:
     *   1. Kiểm tra file tồn tại
     *   2. Mở FileInputStream → ObjectInputStream
     *   3. readObject() → cast sang GameState
     *   4. Kiểm tra version tương thích
     *   5. Trả về GameState hoặc null nếu lỗi
     *
     * @param filePath Đường dẫn file cần đọc
     * @return         GameState đã deserialize, null nếu thất bại
     *
     * ĐƯỢC GỌI BỞI: loadGame() — overload mặc định
     *               GameView.onLoadClicked() — khi người dùng chọn file
     */
    public static GameState loadGame(String filePath) {
        // Kiểm tra file tồn tại trước khi mở
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("[SaveLoadManager] File save không tồn tại: " + filePath);
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {

            GameState state = (GameState) ois.readObject();

            // Cảnh báo nếu version không khớp (vẫn load nhưng warn)
            if (!state.isVersionCompatible()) {
                System.err.println("[SaveLoadManager] CẢNH BÁO: File save version '"
                        + state.saveVersion + "' khác version hiện tại '"
                        + GameState.CURRENT_VERSION + "'. Có thể xảy ra lỗi!");
            }

            System.out.println("[SaveLoadManager] Đã load game: " + filePath
                               + " | " + state);
            return state;

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[SaveLoadManager] Lỗi khi load game: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // ─── UTILITY METHODS ─────────────────────────────────────────────────────

    /**
     * Kiểm tra file save mặc định có tồn tại trên đĩa không.
     * Dùng để bật/tắt nút "Tải game" trên UI.
     *
     * @return true nếu file "checkers_save.dat" tồn tại
     *
     * ĐƯỢC GỌI BỞI: GameView.initButtons() để enable/disable nút Load
     */
    public static boolean defaultSaveExists() {
        return new File(DEFAULT_SAVE_PATH).exists();
    }

    /**
     * Lấy thông tin metadata file save (dung lượng, ngày sửa cuối).
     * Dùng để hiển thị tooltip trên nút "Tải game".
     *
     * @param filePath Đường dẫn file
     * @return         Chuỗi mô tả, ví dụ "Lưu lúc: 04/06/2026 14:30 | 1.2 KB"
     */
    public static String getSaveFileInfo(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) return "Chưa có file save";
        long sizeKB  = file.length() / 1024;
        String date  = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(
                           new Date(file.lastModified()));
        return "Lưu lúc: " + date + " | " + sizeKB + " KB";
    }
}