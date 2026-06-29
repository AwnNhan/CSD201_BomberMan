/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core;

import java.awt.Rectangle;

/**
 *
 * @author Nguyen Minh Phat - CE201621
 */
public class CollisionChecker {

    private int[][] mockMap = new int[13][15];

    public CollisionChecker() {
        // Vẽ 4 bức tường bao quanh viền màn hình để chống đi lố
        for (int i = 0; i < 15; i++) {
            mockMap[0][i] = 1;
            mockMap[12][i] = 1;
        }
        for (int i = 0; i < 13; i++) {
            mockMap[i][0] = 1;
            mockMap[i][14] = 1;
        }

        // Đặt thêm 1 cục gạch ở giữa màn hình (tọa độ dòng 5, cột 5) để bạn test "lượn lách"
        mockMap[5][5] = 1;
    }

    public boolean checkTile(Rectangle nextHitbox) {
        int tileSize = 48;

        // Tính ra Cột và Dòng của 4 góc hitbox
        int leftCol = nextHitbox.x / tileSize;
        int rightCol = (nextHitbox.x + nextHitbox.width) / tileSize;
        int topRow = nextHitbox.y / tileSize;
        int bottomRow = (nextHitbox.y + nextHitbox.height) / tileSize;

        // Chặn lỗi văng mảng (Nếu nhân vật cố đi xuyên ra ngoài cửa sổ game)
        if (leftCol < 0 || rightCol >= 15 || topRow < 0 || bottomRow >= 13) {
            return true; // Coi như đụng tường
        }

        // Lấy giá trị của 4 góc trên bản đồ
        int tile1 = mockMap[topRow][leftCol];
        int tile2 = mockMap[topRow][rightCol];
        int tile3 = mockMap[bottomRow][leftCol];
        int tile4 = mockMap[bottomRow][rightCol];

        // Nếu bất kỳ góc nào đè lên số 1 (Tường), thổi còi cấm đi!
        if (tile1 == 1 || tile2 == 1 || tile3 == 1 || tile4 == 1) {
            return true;
        }

        return false;
    }

    public boolean checkEntity(Rectangle hitboxA, Rectangle hitboxB) {
        // Trong Java, class Rectangle đã hỗ trợ sẵn thuật toán AABB (Axis-Aligned Bounding Box)
        // để kiểm tra xem 2 khối hình chữ nhật có đè lên nhau không.
        return hitboxA.intersects(hitboxB);
    }
}
