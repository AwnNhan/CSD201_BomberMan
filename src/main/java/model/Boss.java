package model;

import algorithm.CustomLinkedList;
import algorithm.GridPoint;
import algorithm.MinHeapQueue;
import algorithm.PathFinder;
import core.AssetManager;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Boss extends GameObject {

    private int hp = 1;
    private int maxHp = 1;
    private double speed;
    private static final int TILE_SIZE = 48;

    private int[][] currentMap;
    private Random random = new Random();
    private PathFinder pathFinder;
    private List<GridPoint> currentPath;
    private GridPoint targetPos;
    private String direction = "DOWN";
    private long lastSkillTime = 0;
    private AssetManager assetManager;
    private long invincibleUntil = 0;

    private int currentDir = -1;
    
    // Padding thu nhỏ hitbox ôm sát thân Boss (Thụt lùi 12px mỗi bên)
    private final int padding = 12;

    public Boss(double startX, double startY, int customSpeed, AssetManager assetManager) {
        super(startX, startY, TILE_SIZE, TILE_SIZE, IdObject.ENEMY);
        this.speed = customSpeed + 1.0; // Boss di chuyển nhanh
        this.pathFinder = new PathFinder();
        this.assetManager = assetManager;
        this.lastSkillTime = System.currentTimeMillis() - 7000;
    }

    public void setRealData(int[][] map, int playerGridR, int playerGridC) {
        this.currentMap = map;
        this.targetPos = new GridPoint(playerGridR, playerGridC);
    }

    public void takeDamage() {
        long currentTime = System.currentTimeMillis();
        if (currentTime > invincibleUntil) {
            this.hp--;
            invincibleUntil = currentTime + 1000; // Cho Boss bất tử 1s sau khi dính đòn
            System.out.println("Boss trúng bom! Máu còn: " + this.hp);
        }
    }

    public int getHp() {
        return this.hp;
    }

    public String getDirection() {
        return direction;
    }

    // Boss chọi 2 quả bom gần người chơi
    public void castSkill(MinHeapQueue bombQueue, CustomLinkedList objectList, int maxCol, int maxRow) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSkillTime > 15000 && targetPos != null) {

            int bombsThrown = 0;
            int attempts = 0;

            while (bombsThrown < 2 && attempts < 15) {
                attempts++;

                int offsetC = random.nextInt(5) - 2;
                int offsetR = random.nextInt(5) - 2;

                int targetC = targetPos.c + offsetC;
                int targetR = targetPos.r + offsetR;

                if (targetC >= 0 && targetC < maxCol && targetR >= 0 && targetR < maxRow) {
                    if (currentMap[targetR][targetC] == 0) {

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
            lastSkillTime = currentTime;
        }
    }

    @Override
    public boolean update() {
        if (currentMap == null || targetPos == null) {
            return true;
        }

        if ((int) this.X % TILE_SIZE == 0 && (int) this.Y % TILE_SIZE == 0) {
            GridPoint myPos = new GridPoint((int) (this.Y / TILE_SIZE), (int) (this.X / TILE_SIZE));
            currentPath = pathFinder.bfsSearch(myPos, targetPos, currentMap);

            if (currentPath == null || currentPath.isEmpty()) {
                List<Integer> validDirs = getValidDirections();
                if (!validDirs.isEmpty()) {
                    if (currentDir == -1 || !validDirs.contains(currentDir) || random.nextInt(5) == 0) {
                        currentDir = validDirs.get(random.nextInt(validDirs.size()));
                    }
                } else {
                    currentDir = -1;
                }
            } else {
                currentDir = -1;
            }
        }

        // Ưu tiên đi theo thuật toán tìm đường
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
        } else if (currentDir != -1) {
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

        // ĐỒNG BỘ LẠI HITBOX THU NHỎ CỦA BOSS DÀNH CHO XỬ LÝ VA CHẠM
        if (this.hitbox != null) {
            this.hitbox.x = (int) this.X + padding;
            this.hitbox.y = (int) this.Y + padding;
            this.hitbox.width = getWidth() - (padding * 2);
            this.hitbox.height = getHeight() - (padding * 2);
        }

        return true;
    }

    private List<Integer> getValidDirections() {
        List<Integer> dirs = new ArrayList<>();
        int col = (int) (this.X / TILE_SIZE);
        int row = (int) (this.Y / TILE_SIZE);

        if (row > 0 && currentMap[row - 1][col] == 0) {
            dirs.add(0);
        }
        if (row < currentMap.length - 1 && currentMap[row + 1][col] == 0) {
            dirs.add(1);
        }
        if (col > 0 && currentMap[row][col - 1] == 0) {
            dirs.add(2);
        }
        if (col < currentMap[0].length - 1 && currentMap[row][col + 1] == 0) {
            dirs.add(3);
        }

        return dirs;
    }

    @Override
    public boolean render(Graphics g) {
        int x = (int) getX();
        int y = (int) getY();
        int w = getWidth();
        int h = getHeight();

        // 1. VẼ HÌNH ẢNH BOSS THEO HƯỚNG
        if (assetManager != null) {
            String dir = getDirection();
            java.awt.Image bossImg = null;

            if ("UP".equalsIgnoreCase(dir)) {
                bossImg = assetManager.getSprite("BOSS_UP");
            } else if ("DOWN".equalsIgnoreCase(dir)) {
                bossImg = assetManager.getSprite("BOSS_DOWN");
            } else if ("LEFT".equalsIgnoreCase(dir)) {
                bossImg = assetManager.getSprite("BOSS_LEFT");
            } else if ("RIGHT".equalsIgnoreCase(dir)) {
                bossImg = assetManager.getSprite("BOSS_RIGHT");
            }

            if (bossImg != null) {
                g.drawImage(bossImg, x, y, w, h, null);
            }
        }

        // 2. NHẤP NHÁY TRẮNG KHI TRÚNG ĐÒN
        if (System.currentTimeMillis() < invincibleUntil && (System.currentTimeMillis() / 100 % 2 == 0)) {
            g.setColor(Color.WHITE);
            g.fillRect(x, y, w, h);
        }

        // Vẽ thanh máu (HP)
        g.setColor(Color.RED);
        g.fillRect((int) getX(), (int) getY() - 10, TILE_SIZE, 5);
        g.setColor(Color.GREEN);

        g.fillRect((int) getX(), (int) getY() - 10, (int) (TILE_SIZE * ((double) this.hp / maxHp)), 5);

        return true;
    }

    @Override
    public Rectangle getHitbox() {
        // Thu nhỏ hitbox lại mỗi bên 12 pixel (Chiểu rộng & cao còn 24px)
        return new Rectangle(
                (int) this.X + padding,
                (int) this.Y + padding,
                this.width - (padding * 2),
                this.height - (padding * 2)
        );
    }
}