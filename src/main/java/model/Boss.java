package model;

import algorithm.CustomLinkedList;
import algorithm.GridPoint;
import algorithm.MinHeapQueue;
import algorithm.PathFinder;
import core.AssetManager;
import java.awt.Color;
import java.awt.Graphics;
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
    // Biến tạo thời gian bất tử cho Boss (tránh 1 ngọn lửa trừ máu liên tục 60 lần/giây)
    private long invincibleUntil = 0;

    // Thêm các biến để đi Random khi bị kẹt
    private int currentDir = -1;

    public Boss(double startX, double startY, int customSpeed, AssetManager assetManager) {
        super(startX, startY, TILE_SIZE, TILE_SIZE, IdObject.ENEMY);
        this.speed = customSpeed + 1.0; // Boss di chuyển nhanh
        this.pathFinder = new PathFinder();
        this.assetManager = assetManager;
        // MỚI: Thiết lập để 8 giây sau khi sinh ra mới ném bom lần đầu (Cooldown là 15s)
        // Hệ thống sẽ lấy Hiện tại trừ đi 7 giây -> Mất thêm 8 giây nữa mới đủ 15 giây để kích hoạt.
        this.lastSkillTime = System.currentTimeMillis() - 7000;
    }

    public void setRealData(int[][] map, int playerGridR, int playerGridC) {
        this.currentMap = map;
        this.targetPos = new GridPoint(playerGridR, playerGridC);
    }

    public void takeDamage() {
        long currentTime = System.currentTimeMillis();
        // Chỉ trừ máu nếu thời gian hiện tại đã vượt qua thời gian bất tử
        if (currentTime > invincibleUntil) {
            this.hp--;
            invincibleUntil = currentTime + 1000; // Cho Boss bất tử 1 giây (1000ms) sau khi dính bom
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
        // Cứ 15 giây Boss ném bom 1 lần
        if (currentTime - lastSkillTime > 15000 && targetPos != null) {

            int bombsThrown = 0;
            int attempts = 0;

            // Thử tối đa 15 lần để tìm ra 2 ô trống ném bom (Giảm xuống còn 2 quả)
            while (bombsThrown < 2 && attempts < 15) {
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
        if (currentMap == null || targetPos == null) {
            return true;
        }

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
        } // 2. NẾU KHÔNG CÓ ĐƯỜNG, ĐI RANDOM
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

        // 2. GIỮ NGUYÊN DÒNG CODE NHẤP NHÁY MÀU TRẮNG
        if (System.currentTimeMillis() < invincibleUntil && (System.currentTimeMillis() / 100 % 2 == 0)) {
            g.setColor(Color.WHITE);
            g.fillRect(x, y, w, h);
        }
        // Vẽ thanh máu (HP)
        g.setColor(Color.RED);
        g.fillRect((int) getX(), (int) getY() - 10, TILE_SIZE, 5);
        g.setColor(Color.GREEN);

        // Sửa công thức vẽ thanh máu xanh dựa trên maxHp
        g.fillRect((int) getX(), (int) getY() - 10, (int) (TILE_SIZE * ((double) this.hp / maxHp)), 5);

        return true;
    }

    @Override
    public java.awt.Rectangle getHitbox() {
        // Thu nhỏ hitbox lại mỗi bên 6 pixel (Tổng cộng thu hẹp 12px chiều rộng và chiều cao)
        int padding = 6;

        return new java.awt.Rectangle(
                (int) this.X + padding,
                (int) this.Y + padding,
                this.width - (padding * 2),
                this.height - (padding * 2)
        );
    }
}
