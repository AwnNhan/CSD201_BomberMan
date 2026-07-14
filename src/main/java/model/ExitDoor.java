package model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

public class ExitDoor extends GameObject {

    // Kích thước chuẩn từ GamePanel
    private static final int TILE_SIZE = 48;

    public ExitDoor(double startX, double startY) {
        // Gọi hàm khởi tạo của lớp cha (GameObject)
        // Truyền vào Tọa độ X, Y, Chiều rộng, Chiều cao và Định danh
        super(startX, startY, TILE_SIZE, TILE_SIZE, IdObject.DOOR);
    }

    @Override
    public boolean update() {
        // Cánh cửa đứng yên nên hàm update không cần xử lý logic di chuyển
        return true;
    }

    @Override
    public boolean render(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        // Tạm thời vẽ một khối vuông màu Tím để đại diện cho cánh cửa 
        // (Nếu sau này bạn có hình ảnh, người số 6 có thể thay thế bằng assetManager.getSprite)
        g2.setColor(Color.MAGENTA);
        g2.fillRect((int) getX(), (int) getY(), getWidth(), getHeight());

        // Vẽ viền để dễ nhìn
        g2.setColor(Color.YELLOW);
        g2.drawRect((int) getX(), (int) getY(), getWidth(), getHeight());

        return true;
    }
}
