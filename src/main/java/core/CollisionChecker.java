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

    public boolean checkTile(Rectangle hitbox) {

        if (hitbox.x < 0 || hitbox.y < 0) {
            return true; // Bị chạm viền trái/trên
        }
        // Giả sử screenWidth = 720, screenHeight = 624 (Lấy từ GamePanel)
        if (hitbox.x + hitbox.width > 720 || hitbox.y + hitbox.height > 624) {
            return true; // Bị chạm viền phải/dưới
        }

        return false; // Không chạm gì cả
    }

    public boolean checkEntity(Rectangle hitboxA, Rectangle hitboxB) {
        // Trong Java, class Rectangle đã hỗ trợ sẵn thuật toán AABB (Axis-Aligned Bounding Box)
        // để kiểm tra xem 2 khối hình chữ nhật có đè lên nhau không.
        return hitboxA.intersects(hitboxB);
    }
}
