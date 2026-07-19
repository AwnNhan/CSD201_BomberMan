package model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import core.AssetManager;

public class ExitDoor extends GameObject {

    // Kích thước chuẩn từ GamePanel
    private static final int TILE_SIZE = 48;
    private AssetManager assetManager;
   public ExitDoor(double startX, double startY, AssetManager assetManager) {
        super(startX, startY, TILE_SIZE, TILE_SIZE, IdObject.DOOR);
        this.assetManager = assetManager;
    }

    @Override
    public boolean update() {
        // Cánh cửa đứng yên nên hàm update không cần xử lý logic di chuyển
        return true;
    }

    @Override
    public boolean render(Graphics g) {
       Graphics2D g2 = (Graphics2D) g;
        
        // 4. KIỂM TRA VÀ VẼ HÌNH ẢNH
        if (assetManager != null && assetManager.getSprite("DOOR") != null) {
            // Nếu có ảnh -> Vẽ ảnh cửa
            g2.drawImage(assetManager.getSprite("DOOR"), (int) getX(), (int) getY(), getWidth(), getHeight(), null);
        } else {
            // Nếu chưa có ảnh -> Tự động vẽ khối tím dự phòng như cũ
            g2.setColor(new Color(138, 43, 226)); // Màu tím
            g2.fillRect((int) getX(), (int) getY(), getWidth(), getHeight());
            
            g2.setColor(Color.YELLOW); // Viền vàng
            g2.drawRect((int) getX(), (int) getY(), getWidth(), getHeight());    }
        return true;
}
}