/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package config;

/**
 * @author LENOVO
 */
public class LevelManager {

    private static int unlockedLevelIndex = 0;

    // Chỉ giữ lại MỘT mảng LEVELS duy nhất, nạp đủ 6 tham số đồng bộ với class LevelConfig
    private static final LevelConfig[] LEVELS = {
        // Cấu trúc: LevelNumber, Đường dẫn map, Số lượng Quái, Tốc độ Quái, Cột Cửa, Dòng Cửa
        new LevelConfig(1, "/maps/map01.txt", 4, 1, 23, 11), // Màn 1: Cửa ở góc dưới phải
        new LevelConfig(2, "/maps/map02.txt", 5, 1, 12, 6),  // Màn 2: Cửa ở giữa bản đồ
        new LevelConfig(3, "/maps/map03.txt", 6, 2, 21, 1),  // Màn 3: Cửa ở góc trên phải
        new LevelConfig(4, "/maps/map04.txt", 8, 2, 1, 11)   // Màn 4: Cửa ở góc dưới trái (Siêu khó)
    };

    // Hàm mở khóa level tiếp theo khi người chơi vượt ải thành công
    public static void unlockNextLevel(int currentIndex) {
        if (currentIndex == unlockedLevelIndex && unlockedLevelIndex < LEVELS.length - 1) {
            unlockedLevelIndex++;
        }
    }

    // Lấy chỉ số màn chơi lớn nhất hiện tại đã được mở khóa
    public static int getUnlockedLevelIndex() {
        return unlockedLevelIndex;
    }

    // Lấy cấu hình của một Level cụ thể qua Index
    public static LevelConfig getLevel(int index) {
        if (index < 0 || index >= LEVELS.length) {
            return LEVELS[0]; // Trở về màn mặc định nếu truyền chỉ số sai hoặc vượt giới hạn
        }
        return LEVELS[index];
    }

    // Lấy tổng số lượng màn chơi có trong game
    public static int getTotalLevels() {
        return LEVELS.length;
    }
}