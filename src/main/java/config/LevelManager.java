package config;

public class LevelManager {

    private static int unlockedLevelIndex = 0;

    private static final LevelConfig[] LEVELS = {
        new LevelConfig(1, "/maps/map01.txt", 4, 1, 23, 11),
        new LevelConfig(2, "/maps/map02.txt", 5, 1, 12, 5),
        new LevelConfig(3, "/maps/map03.txt", 6, 2, 21, 1)
    };

    public static void unlockNextLevel(int currentIndex) {
        if (currentIndex == unlockedLevelIndex && unlockedLevelIndex < LEVELS.length - 1) {
            unlockedLevelIndex++;
        }
    }

    public static int getUnlockedLevelIndex() {
        return unlockedLevelIndex;
    }

    public static LevelConfig getLevel(int index) {
        if (index < 0 || index >= LEVELS.length) {
            return LEVELS[0];
        }
        return LEVELS[index];
    }

    public static int getTotalLevels() {
        return LEVELS.length;
    }
}
