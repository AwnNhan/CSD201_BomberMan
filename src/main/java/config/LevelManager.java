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
