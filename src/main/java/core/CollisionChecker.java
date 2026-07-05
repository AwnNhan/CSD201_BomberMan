package core;

import java.awt.Rectangle;
import map.MapManager;

public class CollisionChecker {

    private MapManager mapM;
    private final int tileSize = 48;

    // Truyền MapManager vào để đọc ma trận bản đồ
    public CollisionChecker(MapManager mapM) {
        this.mapM = mapM;
    }

    public boolean checkTile(Rectangle hitbox) {
        if (mapM == null) {
            return false;
        }

        // Tính toán độ rộng/cao thực tế của toàn bộ bản đồ dựa theo dữ liệu Ma trận (13x25)
        int maxMapWidth = mapM.getMaxCol() * tileSize;
        int maxMapHeight = mapM.getMaxRow() * tileSize;

        // 1. Kiểm tra giới hạn biên của TOÀN BỘ BẢN ĐỒ thay vì biên màn hình
        if (hitbox.x < 0 || hitbox.y < 0) {
            return true;
        }
        if (hitbox.x + hitbox.width > maxMapWidth || hitbox.y + hitbox.height > maxMapHeight) {
            return true;
        }

        // 2. Kiểm tra gạch/tường từ ma trận khổ lớn của Người số 3
        int leftCol = hitbox.x / tileSize;
        int rightCol = (hitbox.x + hitbox.width - 1) / tileSize;
        int topRow = hitbox.y / tileSize;
        int bottomRow = (hitbox.y + hitbox.height - 1) / tileSize;

        // Cập nhật điều kiện mảng theo độ dài thực tế của MapManager (không fix cứng số 15 nữa)
        if (leftCol >= 0 && rightCol < mapM.getMaxCol() && topRow >= 0 && bottomRow < mapM.getMaxRow()) {
            int[][] map = mapM.getMapMatrix();
            // 1: Tường cứng, 2: Gạch mềm
            if (map[topRow][leftCol] != 0 || map[topRow][rightCol] != 0
                    || map[bottomRow][leftCol] != 0 || map[bottomRow][rightCol] != 0) {
                return true;
            }
        }
        return false;
    }

    public boolean checkEntity(Rectangle hitboxA, Rectangle hitboxB) {
        return hitboxA.intersects(hitboxB);
    }
}
