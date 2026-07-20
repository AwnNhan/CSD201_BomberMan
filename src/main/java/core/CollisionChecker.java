package core;

import algorithm.CustomLinkedList;
import java.awt.Rectangle;
import model.GameObject;
import model.IdObject;

public class CollisionChecker {

    private final GamePanel gp;
    private final int tileSize = 48;

    // Nhận vào GamePanel để truy cập động cả mapM và danh sách vật thể objectList mọi lúc
    public CollisionChecker(GamePanel gp) {
        this.gp = gp;
    }

    // =====================================================================
    // KIỂM TRA VA CHẠM VỚI TƯỜNG/GẠCH TRÊN MA TRẬN BẢN ĐỒ LINH HOẠT
    // =====================================================================
    public boolean checkTile(Rectangle hitbox) {
        if (gp == null || gp.mapM == null) {
            return false;
        }

        // Tính toán độ rộng/cao thực tế của toàn bộ bản đồ dựa theo dữ liệu Ma trận (ví dụ 13x25)
        int maxMapWidth = gp.mapM.getMaxCol() * tileSize;
        int maxMapHeight = gp.mapM.getMaxRow() * tileSize;

        // 1. Kiểm tra giới hạn biên cứng của TOÀN BỘ BẢN ĐỒ thay vì chỉ biên màn hình hiển thị
        if (hitbox.x < 0 || hitbox.y < 0) {
            return true;
        }
        if (hitbox.x + hitbox.width > maxMapWidth || hitbox.y + hitbox.height > maxMapHeight) {
            return true;
        }

        // 2. Tính toán các ô lưới (ô cột/hàng) mà hitbox thực thể đang đè lên
        int leftCol = hitbox.x / tileSize;
        int rightCol = (hitbox.x + hitbox.width - 1) / tileSize;
        int topRow = hitbox.y / tileSize;
        int bottomRow = (hitbox.y + hitbox.height - 1) / tileSize;

        // Kiểm tra an toàn biên mảng ma trận tránh lỗi OutOfBounds
        if (leftCol >= 0 && rightCol < gp.mapM.getMaxCol() && topRow >= 0 && bottomRow < gp.mapM.getMaxRow()) {
            int[][] map = gp.mapM.getMapMatrix();
            
            // Nếu bất kỳ ô nào thực thể chạm vào là Tường cứng (1) hoặc Gạch mềm (2) -> Báo có va chạm (true)
            if (map[topRow][leftCol] != 0 || map[topRow][rightCol] != 0
                    || map[bottomRow][leftCol] != 0 || map[bottomRow][rightCol] != 0) {
                return true;
            }
        }
        return false;
    }

    // =====================================================================
    // KIỂM TRA VA CHẠM THÔNG MINH VỚI BOM (GIÚP PLAYER ĐẶT BOM XONG THOÁT RA ĐƯỢC)
    // =====================================================================
    public boolean checkBomb(Rectangle currentHitbox, Rectangle nextHitbox) {
        if (gp == null || gp.objectList == null) {
            return false;
        }

        CustomLinkedList.Node current = gp.objectList.head;
        while (current != null) {
            if (current.data.getId() == IdObject.BOMB) {
                Rectangle bombHitbox = current.data.getHitbox();

                if (!currentHitbox.intersects(bombHitbox) && nextHitbox.intersects(bombHitbox)) {
                    return true;
                }
            }
            current = current.next;
        }
        return false;
    }

    // =====================================================================
    // KIỂM TRA VA CHẠM GIỮA 2 THỰC THỂ (Ví dụ: Player đụng Quái, Lửa liếm Quái)
    // =====================================================================
    public boolean checkEntity(Rectangle hitboxA, Rectangle hitboxB) {
        return hitboxA.intersects(hitboxB);
    }
}