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
        // 1. Kiểm tra viền màn hình
        if (hitbox.x < 0 || hitbox.y < 0) {
            return true;
        }
        if (hitbox.x + hitbox.width > 720 || hitbox.y + hitbox.height > 624) {
            return true;
        }

        // 2. Kiểm tra gạch/tường từ ma trận của Người số 3
        if (mapM != null) {
            int leftCol = hitbox.x / tileSize;
            int rightCol = (hitbox.x + hitbox.width - 1) / tileSize;
            int topRow = hitbox.y / tileSize;
            int bottomRow = (hitbox.y + hitbox.height - 1) / tileSize;

            if (leftCol >= 0 && rightCol < 15 && topRow >= 0 && bottomRow < 13) {
                int[][] map = mapM.getMapMatrix();
                // 1: Tường cứng, 2: Gạch mềm
                if (map[topRow][leftCol] != 0 || map[topRow][rightCol] != 0
                        || map[bottomRow][leftCol] != 0 || map[bottomRow][rightCol] != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkEntity(Rectangle hitboxA, Rectangle hitboxB) {
        return hitboxA.intersects(hitboxB);
    }
}
