/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core;

/**
 *
 * @author LENOVO
 */
import algorithm.ScoreBST;
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
    public GameState gameState = GameState.PLAYING; // Tạm thời để PLAYING để test luôn
    public AssetManager assetManager = new AssetManager();
    public UIManager uiManager = new UIManager();
    public ScoreBST scoreBoard = new ScoreBST();
    
    Thread gameThread;
    KeyHandler keyH = new KeyHandler();

    int playerX = 100;
    int playerY = 100;
    int playerSpeed = 4;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);

        // Bật tính năng vẽ đệm kép (Double Buffering) giúp game không bị nhấp nháy (flicker)
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.addKeyListener(keyH);
    
        assetManager.createPlaceholderSprite("PLAYER", Color.BLUE);
        assetManager.createPlaceholderSprite("BOOM", Color.red);
        assetManager.createPlaceholderSprite("ENEMY", Color.yellow);
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
        if (gameState == GameState.PLAYING) {
            // Logic di chuyển khối trắng của bạn giữ nguyên
            if (keyH.upPressed == true) {
                playerY -= playerSpeed;
            } else if (keyH.downPressed == true) {
                playerY += playerSpeed;
            } else if (keyH.leftPressed == true) {
                playerX -= playerSpeed;
            } else if (keyH.rightPressed == true) {
                playerX += playerSpeed;
            }

            // Nếu đang chơi mà bấm P -> Chuyển sang tạm dừng
            if (keyH.pausePressed) {
                gameState = GameState.PAUSE;
                keyH.pausePressed = false; // Xóa trạng thái bấm để tránh dính phím
            }

        } else if (gameState == GameState.PAUSE) {
            // Nếu đang tạm dừng mà bấm P lần nữa -> Tiếp tục chơi
            if (keyH.pausePressed) {
                gameState = GameState.PLAYING;
                keyH.pausePressed = false;
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Luôn vẽ khối trắng khi đang chơi hoặc đang tạm dừng
        if (gameState == GameState.PLAYING || gameState == GameState.PAUSE) {
            g2.setColor(Color.WHITE);
            g2.fillRect(playerX, playerY, tileSize, tileSize);
        }

        // Nếu trạng thái hiện tại là PAUSE -> Vẽ thêm một lớp phủ mờ và chữ thông báo lên trên
        if (gameState == GameState.PAUSE) {
            g2.setColor(new Color(0, 0, 0, 150)); // Màu đen trong suốt
            g2.fillRect(0, 0, screenWidth, screenHeight);

            g2.setColor(Color.WHITE);
            g2.setFont(g2.getFont().deriveFont(30f)); // Phóng to font chữ
            g2.drawString("GAME PAUSED", screenWidth / 2 - 100, screenHeight / 2);
        }

        g2.dispose();
    }

}
