/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import javax.imageio.ImageIO;

/**
 *
 * @author Admin
 */
public class AssetManager {
    private HashMap<String, BufferedImage> sprites = new HashMap<>();

    // --- HÀM MỚI: Tải ảnh từ thư mục ---
    public void loadImage(String name, String filePath) {
        try {
            // Lấy ảnh từ đường dẫn (ví dụ: "/sprites/player.png")
            BufferedImage image = ImageIO.read(getClass().getResourceAsStream(filePath));
            sprites.put(name, image);
            System.out.println("✅ Tải ảnh thành công: " + name);
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("❌ Lỗi: Không tìm thấy ảnh " + name + " tại " + filePath);
            e.printStackTrace();
        }
    }

    // Hàm tạo ảnh nhân vật bằng code (Pixel Art đơn giản - Giữ lại để dự phòng)
    public void createPlaceholderSprite(String name, Color color) {
        int size = 48; // Kích thước tile
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i < 2 || i > size - 3 || j < 2 || j > size - 3) {
                    image.setRGB(i, j, Color.BLACK.getRGB()); 
                } else {
                    image.setRGB(i, j, color.getRGB()); 
                }
            }
        }
        sprites.put(name, image);
    }

    public BufferedImage getSprite(String name) {
        return sprites.get(name);
    }
}