/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core;

/**
 *
 * @author LENOVO
 */
import algorithm.GraphConverter;
import algorithm.MinHeapQueue; // IMPORT cấu trúc Min-Heap của bạn
import algorithm.ScoreBST;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList; // Dùng để quản lý danh sách ngọn lửa đang cháy
import javax.swing.JPanel;
import map.MapManager;
import model.Bomb;
import model.Flame;
import model.IdObject; // IMPORT Enum định danh thực thể của nhóm bạn

public class GamePanel extends JPanel implements Runnable {

    final int originalTileSize = 16;
    final int scale = 3;

    public final int tileSize = originalTileSize * scale; // 48x48 pixel
    public final int maxScreenCol = 15;
    public final int maxScreenRow = 13;
    public final int screenWidth = tileSize * maxScreenCol; // 720 pixel
    public final int screenHeight = tileSize * maxScreenRow; // 624 pixel
    public GameState gameState = GameState.PLAYING;
    public AssetManager assetManager = new AssetManager();
    public UIManager uiManager = new UIManager();
    public ScoreBST scoreBoard = new ScoreBST();
    Thread gameThread;
    KeyHandler keyH = new KeyHandler();

    MapManager mapM = new MapManager();
    GraphConverter graphConverter = new GraphConverter();

    int playerX = 100;
    int playerY = 100;
    int playerSpeed = 4;
    int enemyX = 300;
    int enemyY = 300;
    int enemySpeed = 2;
    // ==========================================
    // PHẦN KHAI BÁO CỦA KỸ SƯ CHÁY NỔ
    // ==========================================
    private MinHeapQueue bombQueue = new MinHeapQueue(); // Hàng đợi bom nổ bằng Min-Heap
    private ArrayList<Flame> flameList = new ArrayList<>(); // Danh sách các ngọn lửa đang hiển thị
    // ==========================================

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.addKeyListener(keyH);
       
        graphConverter.updateGraph(mapM.getMapMatrix());
        assetManager.createPlaceholderSprite("PLAYER", Color.BLUE);
        assetManager.createPlaceholderSprite("ENEMY", Color.MAGENTA);
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        int FPS = 60;
        double drawInterval = 1000000000 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    public void update() {
        if (gameState == GameState.PLAYING) {
            // Logic di chuyển khối trắng của nhóm giữ nguyên
            if (keyH.upPressed == true) {
                playerY -= playerSpeed;
            } else if (keyH.downPressed == true) {
                playerY += playerSpeed;
            } else if (keyH.leftPressed == true) {
                playerX -= playerSpeed;
            } else if (keyH.rightPressed == true) {
                playerX += playerSpeed;
            }

            // --------------------------------------------------
            // LOGIC LÀM VIỆC CỦA BẠN (CẬP NHẬT BOM & LỬA)
            // --------------------------------------------------
            // 1. Kiểm tra sự kiện đặt bom khi ấn SPACE
            if (keyH.spacePressed) {
                // Đổi tọa độ Pixel sang tọa độ Ô LƯỚI (Grid)
                int bombGridX = (playerX + tileSize / 2) / tileSize;
                int bombGridY = (playerY + tileSize / 2) / tileSize;

                // Cài đặt thời gian nổ sau 3000ms (3 giây)
                long timeToExplode = System.currentTimeMillis() + 3000;

                // Khởi tạo Bomb theo đúng constructor mới (X, Y, width, height, id, timeToExplode)
                // Lưu ý: Nhớ đổi IdObject.BOMB thành đúng tên enum mà nhóm bạn đặt cho quả bom
                Bomb newBomb = new Bomb(bombGridX, bombGridY, tileSize, tileSize, IdObject.BOMB, timeToExplode);
                bombQueue.enqueue(newBomb);

                // Khóa phím ngay lập tức để tránh bị spam đè nút đặt liên tục
                keyH.spacePressed = false;
            }

            // 2. Kiểm tra kích nổ bom đến hạn trong hàng đợi Min-Heap
            long currentTimeMs = System.currentTimeMillis();
            if (!bombQueue.isEmpty()) {
                if (currentTimeMs >= bombQueue.peek().getTimeToExplode()) {
                    Bomb bombToExplode = bombQueue.dequeue(); // Rút quả bom sắp nổ nhất ra
                    executeExplosion(bombToExplode);          // Chạy thuật toán loang lửa
                }
            }

            // 3. Tự động xóa ngọn lửa cũ khi hết hạn (500ms)
            flameList.removeIf(Flame::isExpired);
            // --------------------------------------------------

            if (keyH.pausePressed) {
                gameState = GameState.PAUSE;
                keyH.pausePressed = false;
            }

        } else if (gameState == GameState.PAUSE) {
            if (keyH.pausePressed) {
                gameState = GameState.PLAYING;
                keyH.pausePressed = false;
            }
        }
    }

    // Thuật toán Loang Lửa (Flood Fill biến thể) của bạn
    private void executeExplosion(Bomb bomb) {
        int bombX = (int) bomb.getX(); // Lấy tọa độ ô lưới của bom
        int bombY = (int) bomb.getY();
        int flameLength = 2; // Độ dài tia lửa lan ra (2 ô)

        // Tạo ngọn lửa ngay tại tâm bom nổ (Truyền đủ 5 tham số cho constructor mới của Flame)
        // Lưu ý: Đổi IdObject.FLAME thành đúng tên enum của nhóm bạn nếu cần
        flameList.add(new Flame(bombX, bombY, tileSize, tileSize, IdObject.FLAME));

        // 4 hướng di chuyển trên ô lưới: {Dòng, Cột} -> Lên, Xuống, Trái, Phải
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int[] dir : directions) {
            for (int i = 1; i <= flameLength; i++) {
                int nextX = bombX + dir[1] * i; // Xét theo cột
                int nextY = bombY + dir[0] * i; // Xét theo dòng

                // Kiểm tra biên an toàn để không bị văng lỗi mảng
                if (nextX < 0 || nextX >= maxScreenCol || nextY < 0 || nextY >= maxScreenRow) {
                    break;
                }

                /* * ĐOẠN LIÊN KẾT VỚI NGƯỜI SỐ 3 (MAP):
                 * Khi nào bạn bên Map làm xong ma trận mapTileNum[][], hãy gỡ đoạn comment này ra:
                 *
                 * int tileType = tileM.mapTileNum[nextX][nextY]; 
                 * if (tileType == 1) break; // Gặp tường cứng -> Dừng lan ngay
                 * if (tileType == 2) { 
                 * flameList.add(new Flame(nextX, nextY, tileSize, tileSize, IdObject.FLAME)); 
                 * tileM.mapTileNum[nextX][nextY] = 0; // Xóa gạch mềm/vật phẩm khỏi map
                 * break; // Dừng lan hướng này
                 * }
                 */
                // Tạm thời chưa ráp Map, cho lửa lan tự do:
                flameList.add(new Flame(nextX, nextY, tileSize, tileSize, IdObject.FLAME));
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        mapM.render(g2);

        if (gameState == GameState.PLAYING || gameState == GameState.PAUSE) {

            // --------------------------------------------------
            // PHẦN VẼ ĐỒ HỌA (BOM & LỬA)
            // --------------------------------------------------
            // Do tọa độ b.getX() và f.getX() đang lưu ở dạng ô lưới (0, 1, 2...)
            // nên khi vẽ lên đồ họa cần NHÂN với tileSize để chuyển về pixel.
            // 1. Vẽ tạm quả Bom hình tròn màu Cam
            if (!bombQueue.isEmpty()) {
                Bomb b = bombQueue.peek();
                g2.setColor(Color.ORANGE);
                g2.fillOval((int) b.getX() * tileSize + 4, (int) b.getY() * tileSize + 4, tileSize - 8, tileSize - 8);
            }

            // 2. Vẽ danh sách các tia lửa hình vuông màu Đỏ
            g2.setColor(Color.RED);
            for (int i = 0; i < flameList.size(); i++) {
                Flame f = flameList.get(i);
                g2.fillRect((int) f.getX() * tileSize, (int) f.getY() * tileSize, tileSize, tileSize);
            }
            // --------------------------------------------------
            //vẽ kẻ thù 
            g2.drawImage(assetManager.getSprite("ENEMY"), enemyX, enemyY, tileSize, tileSize, null);
            // Vẽ nhân vật khối trắng của nhóm
            g2.drawImage(assetManager.getSprite("PLAYER"), playerX, playerY, tileSize, tileSize, null);
        }

        if (gameState == GameState.PAUSE) {
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRect(0, 0, screenWidth, screenHeight);

            g2.setColor(Color.WHITE);
            g2.setFont(g2.getFont().deriveFont(30f));
            g2.drawString("GAME PAUSED", screenWidth / 2 - 100, screenHeight / 2);
        }

        g2.dispose();
    }
}
