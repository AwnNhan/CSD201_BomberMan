package core;

import algorithm.CustomLinkedList;
import algorithm.MinHeapQueue;
import model.Bomb;
import model.Flame;
import model.IdObject;
import model.Player;

public class BombManager {

    private final GamePanel gp;
    private final MinHeapQueue bombQueue;
    private long lastBombTime = 0;
    private final long bombCooldown = 500; // Thời gian giãn cách giữa 2 lần đặt bom (ms)

    public BombManager(core.GamePanel gp) {
    this.gp = gp;
    this.bombQueue = new MinHeapQueue();
}

    // Reset lại hàng đợi khi chơi lại hoặc chuyển map
    public void reset() {
        while (!bombQueue.isEmpty()) {
            bombQueue.dequeue();
        }
        lastBombTime = 0;
    }

    // Xử lý khi người chơi ấn nút đặt bom
    // Xử lý khi người chơi ấn nút đặt bom
    public void handlePlacingBomb(Player player, KeyHandler keyH) {
        long currentTimeMs = System.currentTimeMillis();

        if (keyH.spacePressed && (currentTimeMs - lastBombTime >= bombCooldown)) {
            
            // --- BƯỚC THÊM MỚI: ĐẾM SỐ BOM HIỆN CÓ TRÊN BẢN ĐỒ ---
            int currentBombCount = 0;
            CustomLinkedList.Node countTemp = gp.objectList.head;
            while (countTemp != null) {
                if (countTemp.data.getId() == IdObject.BOMB) {
                    currentBombCount++;
                }
                countTemp = countTemp.next;
            }

            // Nếu đã có đủ hoặc vượt quá 3 trái bom thì hủy lệnh đặt bom, không cho đặt thêm
            if (currentBombCount >= 3) {
                keyH.spacePressed = false;
                return; 
            }
            // -----------------------------------------------------

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
                Bomb b = new Bomb(bombX, bombY, gp.tileSize, gp.tileSize, IdObject.BOMB, currentTimeMs + 3000);
                bombQueue.enqueue(b);
                gp.objectList.addLast(b);
                lastBombTime = currentTimeMs;
            }
            keyH.spacePressed = false;
        }
    }

    // Xử lý cập nhật và kích nổ bom theo thời gian
    public void updateBombs() {
        long currentTimeMs = System.currentTimeMillis();

        if (!bombQueue.isEmpty() && currentTimeMs >= bombQueue.peek().getTimeToExplode()) {
            Bomb b = bombQueue.dequeue();

            // Gỡ bom khỏi băng chuyền objectList
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

    // Logic lan tỏa ngọn lửa khi nổ
    private void executeExplosion(Bomb bomb) {
    int bx = (int) bomb.getX();
    int by = (int) bomb.getY();
    
    // Tâm nổ là CENTER
    gp.objectList.addLast(new Flame(bx, by, gp.tileSize, gp.tileSize, IdObject.FLAME, "CENTER"));

    int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // Lên, Xuống, Trái, Phải
    int[][] map = gp.mapM.getMapMatrix();

    for (int[] dir : dirs) {
        String flameType = (dir[0] != 0) ? "VERTICAL" : "HORIZONTAL"; // Xác định thân lửa dọc hay ngang

        for (int i = 1; i <= 2; i++) { // Độ dài tia lửa = 2
            int nextCol = bx / gp.tileSize + dir[1] * i;
            int nextRow = by / gp.tileSize + dir[0] * i;

            if (nextCol < 0 || nextCol >= gp.maxScreenCol || nextRow < 0 || nextRow >= gp.maxScreenRow) {
                break;
            }

            int tileType = map[nextRow][nextCol];
            if (tileType == 1) {
                break; // Gặp tường cứng -> Ngắt tia lửa
            }

            // Nếu là ô cuối cùng của tia lửa (i == 2) hoặc ô chuẩn bị nổ tường gạch
            String currentType = (i == 2 || tileType == 2) ? "END" : flameType;

            if (tileType == 2) { // Tường gạch
                gp.objectList.addLast(new Flame(nextCol * gp.tileSize, nextRow * gp.tileSize, gp.tileSize, gp.tileSize, IdObject.FLAME, "END"));
                gp.mapM.destroySoftWall(nextRow, nextCol);
                break; // Phá gạch xong thì dừng tia lửa tại ô đó luôn
            }
            
            // Ô trống bình thường
            gp.objectList.addLast(new Flame(nextCol * gp.tileSize, nextRow * gp.tileSize, gp.tileSize, gp.tileSize, IdObject.FLAME, currentType));
        }
    }
}

    // Cung cấp ma trận clone chứa vị trí bom để quái dùng thuật toán né
    public int[][] generateMapWithBombs() {
        int[][] mapWithBombs = new int[gp.maxScreenRow][gp.maxScreenCol];
        int[][] originalMap = gp.mapM.getMapMatrix();
        
        for (int r = 0; r < gp.maxScreenRow; r++) {
            System.arraycopy(originalMap[r], 0, mapWithBombs[r], 0, gp.maxScreenCol);
        }

        CustomLinkedList.Node t = gp.objectList.head;
        while (t != null) {
            if (t.data.getId() == IdObject.BOMB) {
                mapWithBombs[(int) t.data.getY() / gp.tileSize][(int) t.data.getX() / gp.tileSize] = 1; // Coi ô có bom là tường
            }
            t = t.next;
        }
        return mapWithBombs;
    }
}