package core;

import algorithm.CustomLinkedList;
import algorithm.MinHeapQueue;
import model.Bomb;
import model.Flame;
import model.IdObject;
import model.Player;

public class BombManager {

    private final GamePanel gp;
    public final MinHeapQueue bombQueue; 
    private long lastBombTime = 0;
    private final long bombCooldown = 500; 
    public SoundManager soundManager = new SoundManager();

    public BombManager(GamePanel gp) {
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
                if (countTemp.data.getId() == IdObject.BOMB) {
                    Bomb checkBomb = (Bomb) countTemp.data;
                    if (!checkBomb.isBossBomb()) {
                        currentBombCount++;
                    }
                }
                countTemp = countTemp.next;
            }

            if (currentBombCount >= 2) {
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
                soundManager.playSFX(2);
            }
            keyH.spacePressed = false;
        }
    }

    public void updateBombs() {
        long currentTimeMs = System.currentTimeMillis();

        if (!bombQueue.isEmpty() && currentTimeMs >= bombQueue.peek().getTimeToExplode()) {
            Bomb b = bombQueue.dequeue();

            // Tìm và gỡ bom khỏi băng chuyền objectList bằng O(N) trước khi nổ
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
        soundManager.playSFX(0);
        int bx = (int) bomb.getX();
        int by = (int) bomb.getY();
        boolean isBossFlame = bomb.isBossBomb();

        gp.objectList.addLast(new Flame(bx, by, gp.tileSize, gp.tileSize, IdObject.FLAME, "CENTER", isBossFlame));

        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // Lên, Xuống, Trái, Phải
        int[][] map = gp.mapM.getMapMatrix();

        int maxR = gp.mapM.getMaxRow();
        int maxC = gp.mapM.getMaxCol();

        for (int[] dir : dirs) {
            String flameType = (dir[0] != 0) ? "VERTICAL" : "HORIZONTAL";

            for (int i = 1; i <= 2; i++) { // Độ dài tia lửa mặc định là 2 ô lưới
                int nextCol = bx / gp.tileSize + dir[1] * i;
                int nextRow = by / gp.tileSize + dir[0] * i;

                // Kiểm tra biên an toàn của map tránh văng lỗi OutOfBounds
                if (nextCol < 0 || nextCol >= maxC || nextRow < 0 || nextRow >= maxR) {
                    break;
                }

                int tileType = map[nextRow][nextCol];
                if (tileType == 1) {
                    break; // Gặp tường đá cứng (Không thể phá) -> Ngắt tia lửa ngay
                }

                // Định dạng ô cuối cùng của tia lửa hoặc ô chuẩn bị phá gạch
                String currentType = (i == 2 || tileType == 2) ? "END" : flameType;

                if (tileType == 2) { // Gặp tường gạch mịn (Phá được)
                    gp.objectList.addLast(new Flame(nextCol * gp.tileSize, nextRow * gp.tileSize, gp.tileSize, gp.tileSize, IdObject.FLAME, "END", isBossFlame));
                    gp.mapM.destroySoftWall(nextRow, nextCol); // Gọi MapManager dọn gạch chuyển về ô trống (0)
                    break; // Phá tường gạch xong ngắt tia lửa luôn không cho xuyên thấu
                }

                // Ô đường đi trống bình thường
                gp.objectList.addLast(new Flame(nextCol * gp.tileSize, nextRow * gp.tileSize, gp.tileSize, gp.tileSize, IdObject.FLAME, currentType, isBossFlame));
            }
        }
    }

    // Tạo bản sao ma trận map kèm vị trí các quả bom (được xem như tường vật cản có giá trị 1) để AI quái vật tìm đường né tránh
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
                int bombCol = (int) t.data.getX() / gp.tileSize;
                int bombRow = (int) t.data.getY() / gp.tileSize;
                // Đảm bảo toạ độ bom không nằm ngoài ma trận trước khi gán dữ liệu
                if (bombRow >= 0 && bombRow < maxR && bombCol >= 0 && bombCol < maxC) {
                    mapWithBombs[bombRow][bombCol] = 1; // Coi ô đang chứa quả bom như một bức tường
                }
            }
            t = t.next;
        }
        return mapWithBombs;
    }
}
