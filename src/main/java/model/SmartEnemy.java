package model;

import algorithm.GridPoint;
import algorithm.PathFinder;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SmartEnemy extends GameObject {

    private double speed;
    private static final int TILE_SIZE = 48;
    private int[][] currentMap;
    private PathFinder pathFinder;
    private List<GridPoint> currentPath;

    private GridPoint targetPos;
    private String direction = "DOWN";

    // Thêm các biến để đi Random khi bị kẹt
    private int currentDir = -1;
    private Random random = new Random();

    public SmartEnemy(double startX, double startY, int customSpeed) {
        super(startX, startY, TILE_SIZE, TILE_SIZE, IdObject.ENEMY);
        this.speed = customSpeed + 0.5; // Nhanh hơn một chút so với Enemy thường ở cùng level
        this.pathFinder = new PathFinder();
    }

    public void setRealData(int[][] map, int playerGridR, int playerGridC) {
        this.currentMap = map;
        this.targetPos = new GridPoint(playerGridR, playerGridC);
    }

    @Override
    public boolean update() {
        if (currentMap == null || targetPos == null) return true;

        if ((int) this.X % TILE_SIZE == 0 && (int) this.Y % TILE_SIZE == 0) {
            GridPoint myPos = new GridPoint((int) (this.Y / TILE_SIZE), (int) (this.X / TILE_SIZE));
            currentPath = pathFinder.bfsSearch(myPos, targetPos, currentMap);

            // LOGIC CHỮA CHÁY: Nếu không có đường tới người chơi (bị kẹt tường)
            if (currentPath == null || currentPath.isEmpty()) {
                List<Integer> validDirs = getValidDirections();
                if (!validDirs.isEmpty()) {
                    if (currentDir == -1 || !validDirs.contains(currentDir) || random.nextInt(5) == 0) {
                        currentDir = validDirs.get(random.nextInt(validDirs.size()));
                    }
                } else {
                    currentDir = -1; // Kẹt cứng 4 bề
                }
            } else {
                currentDir = -1; // Có đường thì tắt chế độ Random
            }
        }

        // 1. ƯU TIÊN ĐI RƯỢT THEO ĐƯỜNG BFS
        if (currentPath != null && !currentPath.isEmpty()) {
            GridPoint nextStep = currentPath.get(0);

            double targetX = nextStep.c * TILE_SIZE;
            double targetY = nextStep.r * TILE_SIZE;

            double nextX = this.X;
            double nextY = this.Y;

            if (Math.abs(this.X - targetX) > 0) {
                if (Math.abs(this.X - targetX) <= speed) {
                    nextX = targetX;
                } else if (this.X < targetX) {
                    nextX += speed;
                    direction = "RIGHT";
                } else {
                    nextX -= speed;
                    direction = "LEFT";
                }
            } else if (Math.abs(this.Y - targetY) > 0) {
                if (Math.abs(this.Y - targetY) <= speed) {
                    nextY = targetY;
                } else if (this.Y < targetY) {
                    nextY += speed;
                    direction = "DOWN";
                } else {
                    nextY -= speed;
                    direction = "UP";
                }
            }

            this.setX(nextX);
            this.setY(nextY);
        }
        // 2. NẾU KHÔNG CÓ ĐƯỜNG, ĐI RANDOM
        else if (currentDir != -1) {
            if (currentDir == 0) {
                this.setY(this.Y - speed);
                direction = "UP";
            } else if (currentDir == 1) {
                this.setY(this.Y + speed);
                direction = "DOWN";
            } else if (currentDir == 2) {
                this.setX(this.X - speed);
                direction = "LEFT";
            } else if (currentDir == 3) {
                this.setX(this.X + speed);
                direction = "RIGHT";
            }
        }

        return true;
    }

    private List<Integer> getValidDirections() {
        List<Integer> dirs = new ArrayList<>();
        int col = (int) (this.X / TILE_SIZE);
        int row = (int) (this.Y / TILE_SIZE);

        if (row > 0 && currentMap[row - 1][col] == 0) dirs.add(0);
        if (row < currentMap.length - 1 && currentMap[row + 1][col] == 0) dirs.add(1);
        if (col > 0 && currentMap[row][col - 1] == 0) dirs.add(2);
        if (col < currentMap[0].length - 1 && currentMap[row][col + 1] == 0) dirs.add(3);

        return dirs;
    }

    public String getDirection() {
        return direction;
    }

    @Override
    public boolean render(Graphics g) {
        g.setColor(new Color(255, 140, 0)); // Màu Cam đậm
        g.fillRect((int) getX(), (int) getY(), getWidth(), getHeight());
        return true;
    }
}