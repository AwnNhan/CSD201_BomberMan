package config;

/**
 * @author LENOVO
 */
public class LevelConfig {

    private int levelNumber;
    private String mapFilePath;
    private int enemyCount;
    private int enemySpeed;
    private int doorCol;
    private int doorRow;

    // Constructor đầy đủ tham số để nạp cấu hình cho từng Level game
    public LevelConfig(int levelNumber, String mapFilePath, int enemyCount, int enemySpeed, int doorCol, int doorRow) {
        this.levelNumber = levelNumber;
        this.mapFilePath = mapFilePath;
        this.enemyCount = enemyCount;
        this.enemySpeed = enemySpeed;
        this.doorCol = doorCol;
        this.doorRow = doorRow;
    }

    // =========================================================================
    // CÁC HÀM GETTER ĐỂ GAMEPANEL TRUY XUẤT DỮ LIỆU
    // =========================================================================

    public int getLevelNumber() {
        return levelNumber;
    }

    public String getMapFilePath() {
        return mapFilePath;
    }

    public int getEnemyCount() {
        return enemyCount;
    }

    public int getEnemySpeed() {
        return enemySpeed;
    }

    public int getDoorCol() {
        return doorCol;
    }

    public int getDoorRow() {
        return doorRow;
    }
}