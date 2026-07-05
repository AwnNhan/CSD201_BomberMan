package model;

import algorithm.CustomLinkedList;
import algorithm.GridPoint;
import algorithm.MinHeapQueue;
import algorithm.PathFinder;
import java.awt.Color;
import java.awt.Graphics;
import java.util.List;
import java.util.Random;

public class Boss extends GameObject {

    private int hp = 15;
    private double speed;
    private static final int TILE_SIZE = 48;

    private int[][] currentMap;
    private Random random = new Random();
    private PathFinder pathFinder;
    private List<GridPoint> currentPath;
    private GridPoint targetPos;
    private String direction = "DOWN";
    private long lastSkillTime = 0;

    public Boss(double startX, double startY, int customSpeed) {
        super(startX, startY, TILE_SIZE, TILE_SIZE, IdObject.ENEMY);
        this.speed = customSpeed + 1.0; // Boss di chuyển nhanh
        this.pathFinder = new PathFinder();
    }

    public void setRealData(int[][] map, int playerGridR, int playerGridC) {
        this.currentMap = map;
        this.targetPos = new GridPoint(playerGridR, playerGridC);
    }

    public void takeDamage() {
        this.hp--;
    }

    public int getHp() {
        return this.hp;
    }

    public String getDirection() {
        return direction;
    }

    // Boss chọi 3 quả bom gần người chơi
    public void castSkill(MinHeapQueue bombQueue, CustomLinkedList objectList, int maxCol, int maxRow) {
        long currentTime = System.currentTimeMillis();
        // Cứ 15 giây Boss ném 3 quả bom 1 lần
        if (currentTime - lastSkillTime > 15000 && targetPos != null) {

            int bombsThrown = 0;
            int attempts = 0;

            // Thử tối đa 15 lần để tìm ra 3 ô trống ném bom
            while (bombsThrown < 3 && attempts < 15) {
                attempts++;
                
                // Tính tọa độ ngẫu nhiên gần người chơi (cách từ -2 đến 2 ô)
                int offsetC = random.nextInt(5) - 2; 
                int offsetR = random.nextInt(5) - 2;

                int targetC = targetPos.c + offsetC;
                int targetR = targetPos.r + offsetR;

                // Đảm bảo không văng ra khỏi map
                if (targetC >= 0 && targetC < maxCol && targetR >= 0 && targetR < maxRow) {
                    if (currentMap[targetR][targetC] == 0) { // Chỉ ném vào ô trống
                        
                        // Kiểm tra xem chỗ đó có bom chưa
                        boolean hasBombHere = false;
                        CustomLinkedList.Node temp = objectList.head;
                        while (temp != null) {
                            if (temp.data.getId() == IdObject.BOMB 
                                    && temp.data.getX() == targetC * TILE_SIZE 
                                    && temp.data.getY() == targetR * TILE_SIZE) {
                                hasBombHere = true;
                                break;
                            }
                            temp = temp.next;
                        }

                        if (!hasBombHere) {
                            long timeToExplode = currentTime + 3000;
                            Bomb bossBomb = new Bomb(targetC * TILE_SIZE, targetR * TILE_SIZE, TILE_SIZE, TILE_SIZE, IdObject.BOMB, timeToExplode, true);
                            bombQueue.enqueue(bossBomb);
                            objectList.addLast(bossBomb);
                            bombsThrown++;
                        }
                    }
                }
            }
            lastSkillTime = currentTime; // Reset lại hồi chiêu 15s
        }
    }

    @Override
    public boolean update() {
        if (currentMap == null || targetPos == null) return true;

        if ((int) this.X % TILE_SIZE == 0 && (int) this.Y % TILE_SIZE == 0) {
            GridPoint myPos = new GridPoint((int) (this.Y / TILE_SIZE), (int) (this.X / TILE_SIZE));
            currentPath = pathFinder.bfsSearch(myPos, targetPos, currentMap);
        }

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
        return true;
    }

    @Override
    public boolean render(Graphics g) {
        g.setColor(new Color(138, 43, 226)); // Màu tím
        g.fillRect((int) getX(), (int) getY(), getWidth(), getHeight());

        // Vẽ thanh máu (HP)
        g.setColor(Color.RED);
        g.fillRect((int) getX(), (int) getY() - 10, TILE_SIZE, 5);
        g.setColor(Color.GREEN);
        g.fillRect((int) getX(), (int) getY() - 10, (int) (TILE_SIZE * (this.hp / 15.0)), 5);

        return true;
    }
}