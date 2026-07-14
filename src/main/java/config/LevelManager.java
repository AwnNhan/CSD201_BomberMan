/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package config;

/**
 *
 * @author LENOVO
 */
public class LevelManager {

    private static int unlockedLevelIndex = 0;

    private static final LevelConfig[] LEVELS = {
        // Cửa màn 1 giấu ở góc phải cùng (Cột 23, Dòng 11)
        new LevelConfig(1, "/maps/map01.txt", 4, 1, 23, 11),
        // Cửa màn 2 giấu ở giữa bản đồ (Cột 12, Dòng 6)
        new LevelConfig(2, "/maps/map02.txt", 5, 1, 12, 6),
        // Cửa màn 3 giấu ở góc trên cùng bên phải (Cột 21, Dòng 1)
        new LevelConfig(3, "/maps/map03.txt", 6, 2, 21, 1)
    };

    // mở khóa level tiếp theo 
    public static void unlockNextLevel(int currentIndex) {
        if (currentIndex == unlockedLevelIndex && unlockedLevelIndex < LEVELS.length - 1) {
            unlockedLevelIndex++;
        }
    }

    public static int getUnlockedLevelIndex() {
        return unlockedLevelIndex;
    }

    private static final LevelConfig[] LEVELS = {
        // Cấu trúc: LevelNumber, Đường dẫn file txt, Số lượng Quái, Tốc độ Quái
        new LevelConfig(1, "/maps/map01.txt", 4, 1), // Level 1: Dễ
        new LevelConfig(2, "/maps/map02.txt", 4, 1), // Level 2: Trung bình
        new LevelConfig(3, "/maps/map03.txt", 6, 2), // Level 3: Khó
        new LevelConfig(4, "/maps/map04.txt", 8, 2) // Level 4: Siêu khó
    };

    public static LevelConfig getLevel(int index) {
        if (index < 0 || index >= LEVELS.length) {
            return LEVELS[0]; // Trở về map mặc định nếu vượt giới hạn
        }
        return LEVELS[index];
    }

    public static int getTotalLevels() {
        return LEVELS.length;
    }
}
