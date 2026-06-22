/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 *
 * @author Admin
 */
public class AssetManager {
    private HashMap<String, BufferedImage> sprites = new HashMap<>();

    // Hàm tạo ảnh nhân vật bằng code (Pixel Art đơn giản)
    public void createPlaceholderSprite(String name, Color color) {
        int size = 48; // Kích thước tile (theo GamePanel của bạn)
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        
        // Vẽ một hình vuông đại diện cho nhân vật
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                // Tạo hiệu ứng viền để nhìn rõ nhân vật
                if (i < 2 || i > size - 3 || j < 2 || j > size - 3) {
                    image.setRGB(i, j, Color.BLACK.getRGB()); // Viền đen
                } else {
                    image.setRGB(i, j, color.getRGB()); // Màu chính
                }
            }
        }
        sprites.put(name, image);
    }

    public BufferedImage getSprite(String name) {
        return sprites.get(name);
    }
}
