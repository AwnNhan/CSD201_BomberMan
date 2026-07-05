package core;

import algorithm.CustomLinkedList;
import algorithm.MinHeapQueue;
import model.Bomb;
import model.Flame;
import model.IdObject;
import model.Player;

public class BombManager {

    private final GamePanel gp;
    public final MinHeapQueue bombQueue; // Public để Boss xài
    private long lastBombTime = 0;
    private final long bombCooldown = 500; 

    public BombManager(core.GamePanel gp) {
        this.gp = gp;
        this.bombQueue = new MinHeapQueue();
    }

    public void reset() {
        while (!bombQueue.isEmpty()) {
            bombQueue.dequeue();
        }
        lastBombTime = 0;
    }

    public void handlePlacingBomb(Player player, KeyHandler keyH) {
        long currentTimeMs = System.currentTimeMillis();

        if (keyH.spacePressed && (currentTimeMs - lastBombTime >= bombCooldown)) {
            
            int currentBombCount = 0;
            CustomLinkedList.Node countTemp = gp.objectList.head;
            while (countTemp != null) {
                // Chỉ đếm bom của Player (bỏ qua bom Boss)
                if (countTemp.data.getId() == IdObject.BOMB) {
                    Bomb checkBomb = (Bomb) countTemp.data;
                    if (!checkBomb.isBossBomb()) {
                        currentBombCount++;
                    }
                }
                countTemp = countTemp.next;
            }

            if (currentBombCount >= 3) {
                keyH.spacePressed = false;
                return; 
            }

            int bombX = ((int) player.getX() + gp.tileSize / 2) / gp.tileSize * gp.tileSize;
            int bombY = ((int) player.getY() + gp.tileSize / 2) / gp.tileSize * gp.tileSize;

            boolean hasBombHere = false;
            CustomLinkedList.Node temp = gp.objectList.head;
            while (temp != null) {
                if (temp.data.getId() == IdObject.BOMB && temp.data.getX() == bombX && temp.data.getY() == bombY) {
                    hasBombHere = true;
                    break;
                }
                temp = temp.next;
            }

            if (!hasBombHere) {
                Bomb b = new Bomb(bombX, bombY, gp.tileSize, gp.tileSize, IdObject.BOMB, currentTimeMs + 3000, false);
                bombQueue.enqueue(b);
                gp.objectList.addLast(b);
                lastBombTime = currentTimeMs;
            }
            keyH.spacePressed = false;
        }
    }

    public void updateBombs() {
        long currentTimeMs = System.currentTimeMillis();

        if (!bombQueue.isEmpty() && currentTimeMs >= bombQueue.peek().getTimeToExplode()) {
            Bomb b = bombQueue.dequeue();

            CustomLinkedList.Node temp = gp.objectList.head;
            while (temp != null) {
                if (temp.data == b) {
                    gp.objectList.removeNode(temp);
                    break;
                }
                temp = temp.next;
            }
            executeExplosion(b);
        }
    }

    private void executeExplosion(Bomb bomb) {
        int bx = (int) bomb.getX();
        int by = (int) bomb.getY();
        boolean isBossFlame = bomb.isBossBomb(); 
        
        gp.objectList.addLast(new Flame(bx, by, gp.tileSize, gp.tileSize, IdObject.FLAME, "CENTER", isBossFlame));

        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; 
        int[][] map = gp.mapM.getMapMatrix();
        
        // SỬA LỖI MAP: Lấy max Row và Col từ MapManager (13x25)
        int maxR = gp.mapM.getMaxRow();
        int maxC = gp.mapM.getMaxCol();

        for (int[] dir : dirs) {
            String flameType = (dir[0] != 0) ? "VERTICAL" : "HORIZONTAL";

            for (int i = 1; i <= 2; i++) {
                int nextCol = bx / gp.tileSize + dir[1] * i;
                int nextRow = by / gp.tileSize + dir[0] * i;

                // Áp dụng giới hạn map 25 cột
                if (nextCol < 0 || nextCol >= maxC || nextRow < 0 || nextRow >= maxR) {
                    break; 
                }

                int tileType = map[nextRow][nextCol];
                if (tileType == 1) {
                    break; 
                }

                String currentType = (i == 2 || tileType == 2) ? "END" : flameType;

                if (tileType == 2) { 
                    gp.objectList.addLast(new Flame(nextCol * gp.tileSize, nextRow * gp.tileSize, gp.tileSize, gp.tileSize, IdObject.FLAME, "END", isBossFlame));
                    gp.mapM.destroySoftWall(nextRow, nextCol);
                    break; 
                }
                
                gp.objectList.addLast(new Flame(nextCol * gp.tileSize, nextRow * gp.tileSize, gp.tileSize, gp.tileSize, IdObject.FLAME, currentType, isBossFlame));
            }
        }
    }

    public int[][] generateMapWithBombs() {
        int maxR = gp.mapM.getMaxRow();
        int maxC = gp.mapM.getMaxCol();
        
        int[][] mapWithBombs = new int[maxR][maxC];
        int[][] originalMap = gp.mapM.getMapMatrix();
        
        for (int r = 0; r < maxR; r++) {
            System.arraycopy(originalMap[r], 0, mapWithBombs[r], 0, maxC);
        }

        CustomLinkedList.Node t = gp.objectList.head;
        while (t != null) {
            if (t.data.getId() == IdObject.BOMB) {
                mapWithBombs[(int) t.data.getY() / gp.tileSize][(int) t.data.getX() / gp.tileSize] = 1;
            }
            t = t.next;
        }
        return mapWithBombs;
    }
}