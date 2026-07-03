/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package config;

/**
 *
 * @author LENOVO
 */
public class LevelConfig {

    private int levelNumber;
    private String mapFilePath;
    private int enemyCount;
    private int enemySpeed;

    public LevelConfig(int levelNumber, String mapFilePath, int enemyCount, int enemySpeed) {
        this.levelNumber = levelNumber;
        this.mapFilePath = mapFilePath;
        this.enemyCount = enemyCount;
        this.enemySpeed = enemySpeed;
    }

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
}
