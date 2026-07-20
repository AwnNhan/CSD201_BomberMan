package core;

import algorithm.CustomLinkedList;
import algorithm.MinHeapQueue;
import model.Bomb;
import model.Flame;
import model.IdObject;
import model.Player;

public class BombManager {

    private final GamePanel gp;
    public final MinHeapQueue bombQueue; // Public để Boss có thể truy cập xài chung hàng đợi
    private long lastBombTime = 0;
    private final long bombCooldown = 500; // Thời gian giãn cách giữa 2 lần đặt bom (ms)
    public SoundManager soundManager = new SoundManager();

    public BombManager(GamePanel gp) {
        this.gp = gp;
        this.bombQueue = new MinHeapQueue();
    }

    // Reset lại hàng đợi khi người chơi Reset Game hoặc đổi màn
    public void reset() {
        while (!bombQueue.isEmpty()) {
            bombQueue.dequeue();
        }
        lastBombTime = 0;
    }

    // Xử lý logic khi người chơi bấm nút đặt bom (Phím SPACE)
    public void handlePlacingBomb(Player player, KeyHandler keyH) {
        long currentTimeMs = System.currentTimeMillis();

        if (keyH.spacePressed && (currentTimeMs - lastBombTime >= bombCooldown)) {

            // --- ĐẾM SỐ BOM HIỆN CÓ CỦA PLAYER TRÊN BẢN ĐỒ ---
            int currentBombCount = 0;
            CustomLinkedList.Node countTemp = gp.objectList.head;
            while (countTemp != null) {
                if (countTemp.data.getId() == IdObject.BOMB) {
                    Bomb checkBomb = (Bomb) countTemp.data;
                    // Chỉ đếm bom của Player, bỏ qua bom của Boss ném ra
                    if (!checkBomb.isBossBomb()) {
                        currentBombCount++;
                    }
                }
                countTemp = countTemp.next;
            }

            // GIỚI HẠN: Nếu Player đã đặt đủ 2 quả bom trên sân thì không cho đặt thêm
            if (currentBombCount >= 2) {
                keyH.spacePressed = false;
                return;
            }

            // Tính toán toạ độ bom căn khít theo ô lưới TileSize
            int bombX = ((int) player.getX() + gp.tileSize / 2) / gp.tileSize * gp.tileSize;
            int bombY = ((int) player.getY() + gp.tileSize / 2) / gp.tileSize * gp.tileSize;

            // Kiểm tra xem tại ô này đã có quả bom nào nằm sẵn chưa (Tránh đặt trùng vị trí)
            boolean hasBombHere = false;
            CustomLinkedList.Node temp = gp.objectList.head;
            while (temp != null) {
                if (temp.data.getId() == IdObject.BOMB && temp.data.getX() == bombX && temp.data.getY() == bombY) {
                    hasBombHere = true;
                    break;
                }
                temp = temp.next;
            }

            // Nếu ô trống, tiến hành nạp bom vào hàng đợi MinHeap và danh sách thực thể
            if (!hasBombHere) {
                // Tham số cuối là 'false' vì đây là bom của Player
                Bomb b = new Bomb(bombX, bombY, gp.tileSize, gp.tileSize, IdObject.BOMB, currentTimeMs + 3000, false);
                bombQueue.enqueue(b);
                gp.objectList.addLast(b);
                lastBombTime = currentTimeMs;
                soundManager.playSFX(2);
            }
            keyH.spacePressed = false;
        }
    }

    // Xử lý cập nhật thời gian và kích nổ quả bom đến hạn tự động trong Game Loop
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

    // Xử lý lan tỏa tia lửa nổ theo 4 hướng độc lập linh hoạt theo kích thước Map
    private void executeExplosion(Bomb bomb) {
        soundManager.playSFX(0);
        int bx = (int) bomb.getX();
        int by = (int) bomb.getY();
        boolean isBossFlame = bomb.isBossBomb();

        // Tạo tâm nổ dạng mây nấm (CENTER)
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
