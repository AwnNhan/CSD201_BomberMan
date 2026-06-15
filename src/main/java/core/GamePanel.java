/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core;

/**
 *
 * @author LENOVO
 */
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable {

    final int originalTileSize = 16;
    final int scale = 3;

    public final int tileSize = originalTileSize * scale; // 48x48 pixel
    public final int maxScreenCol = 15;
    public final int maxScreenRow = 13;
    public final int screenWidth = tileSize * maxScreenCol; // 720 pixel
    public final int screenHeight = tileSize * maxScreenRow; // 624 pixel

    Thread gameThread;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);

        // Bật tính năng vẽ đệm kép (Double Buffering) giúp game không bị nhấp nháy (flicker)
        this.setDoubleBuffered(true);
    }

    public void startGameThread() {
        gameThread = new Thread(this); // Giao GamePanel này cho Thread quản lý
        gameThread.start(); // Lệnh start() sẽ tự động gọi hàm run() ở bên dưới
    }

    @Override
    public void run() {
        int FPS = 60; // Tốc độ khung hình mong muốn
        double drawInterval = 1000000000 / FPS; // 1 tỷ nano-giây chia 60 (Khoảng thời gian 1 frame)
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        // VÒNG LẶP VÔ TẬN: Chỉ dừng khi tắt game
        while (gameThread != null) {
            currentTime = System.nanoTime();

            // Tính toán xem đã trôi qua bao nhiêu phần của 1 khung hình
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            // Nếu delta >= 1, nghĩa là đã đủ thời gian để vẽ khung hình tiếp theo
            if (delta >= 1) {
                update(); // 1. Cập nhật vị trí, trạng thái (Toán học)
                repaint(); // 2. Vẽ lại toàn bộ lên màn hình (Đồ họa)
                delta--; // Trừ đi 1 để đếm lại từ đầu cho frame sau
            }
        }
    }

    public void update() {
        // (Tương lai): Gọi Player.update(), Enemy.update() tại đây
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g); // Xóa sạch màn hình cũ trước khi vẽ cái mới

        // Ép kiểu sang Graphics2D để có nhiều tính năng vẽ mạnh mẽ hơn
        Graphics2D g2 = (Graphics2D) g;

        // (Ví dụ test): Vẽ một hình vuông đại diện cho nhân vật
        g2.setColor(Color.WHITE);
        g2.fillRect(100, 100, tileSize, tileSize);

        // Gỡ bỏ cọ vẽ để giải phóng bộ nhớ RAM (Cực kỳ quan trọng)
        g2.dispose();
    }
}
