package core;

import algorithm.GraphConverter;
import algorithm.MinHeapQueue;
import algorithm.ScoreBST;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle; 
import java.util.ArrayList;
import javax.swing.JPanel;
import map.MapManager;
import model.Bomb;
import model.Flame;
import model.IdObject;
import model.Enemy;

public class GamePanel extends JPanel implements Runnable {

    final int originalTileSize = 16;
    final int scale = 3;
    public final int tileSize = originalTileSize * scale; // 48x48
    public final int maxScreenCol = 15;
    public final int maxScreenRow = 13;
    public final int screenWidth = tileSize * maxScreenCol;
    public final int screenHeight = tileSize * maxScreenRow;
    
    public GameState gameState = GameState.PLAYING;
    public boolean isGameOver = false; 
    
    public AssetManager assetManager = new AssetManager();
    public UIManager uiManager = new UIManager();
    public ScoreBST scoreBoard = new ScoreBST();
    Thread gameThread;
    KeyHandler keyH = new KeyHandler();

    MapManager mapM = new MapManager();
    GraphConverter graphConverter = new GraphConverter();

    int playerX = tileSize * 1;
    int playerY = tileSize * 1;
    int playerSpeed = 4;
    
    ArrayList<Enemy> enemyList = new ArrayList<>();

    private MinHeapQueue bombQueue = new MinHeapQueue();
    // THÊM DANH SÁCH NÀY ĐỂ VẼ TẤT CẢ CÁC QUẢ BOM (Tránh lỗi tàng hình)
    private ArrayList<Bomb> bombList = new ArrayList<>(); 
    private ArrayList<Flame> flameList = new ArrayList<>();
    
    // BIẾN COOLDOWN BOM
    private long lastBombTime = 0;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.addKeyListener(keyH);
        
        graphConverter.updateGraph(mapM.getMapMatrix());
        assetManager.createPlaceholderSprite("PLAYER", Color.BLUE);
        assetManager.createPlaceholderSprite("ENEMY", Color.MAGENTA);
        
        enemyList.add(new Enemy(tileSize * 13, tileSize * 1));  
        enemyList.add(new Enemy(tileSize * 1, tileSize * 11));  
        enemyList.add(new Enemy(tileSize * 13, tileSize * 11)); 
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

    private void resetGame() {
        playerX = tileSize * 1;
        playerY = tileSize * 1;
        
        mapM = new MapManager();
        graphConverter.updateGraph(mapM.getMapMatrix());
        
        bombQueue = new MinHeapQueue();
        bombList.clear(); // Reset list bom
        flameList.clear();
        enemyList.clear();
        
        lastBombTime = 0; // Reset Cooldown
        
        enemyList.add(new Enemy(tileSize * 13, tileSize * 1));  
        enemyList.add(new Enemy(tileSize * 1, tileSize * 11));  
        enemyList.add(new Enemy(tileSize * 13, tileSize * 11)); 
        
        isGameOver = false;
        gameState = GameState.PLAYING;
    }

    // ==============================================================
    // LUẬT ĐI XUYÊN BOM (WALK-OFF) CỰC XỊN Ở ĐÂY
    // ==============================================================
    private boolean canMove(int nextX, int nextY) {
        int margin = 12; 
        
        // 1. KIỂM TRA TƯỜNG (BẢN ĐỒ)
        int leftCol = (nextX + margin) / tileSize;
        int rightCol = (nextX + tileSize - margin - 1) / tileSize;
        int topRow = (nextY + margin) / tileSize;
        int bottomRow = (nextY + tileSize - margin - 1) / tileSize;

        if (leftCol < 0 || rightCol >= maxScreenCol || topRow < 0 || bottomRow >= maxScreenRow) return false;

        int[][] map = mapM.getMapMatrix();
        if (map[topRow][leftCol] != 0 || map[topRow][rightCol] != 0 || 
            map[bottomRow][leftCol] != 0 || map[bottomRow][rightCol] != 0) {
            return false;
        }

        // 2. KIỂM TRA VA CHẠM VỚI BOM SẴN CÓ
        Rectangle nextHitbox = new Rectangle(nextX + margin, nextY + margin, tileSize - 2 * margin, tileSize - 2 * margin);
        Rectangle currentHitbox = new Rectangle(playerX + margin, playerY + margin, tileSize - 2 * margin, tileSize - 2 * margin);

        for (Bomb b : bombList) {
            Rectangle bombHitbox = new Rectangle((int)b.getX() * tileSize, (int)b.getY() * tileSize, tileSize, tileSize);
            
            // Nếu bước tiếp theo dẫm trúng quả bom
            if (nextHitbox.intersects(bombHitbox)) {
                // Nếu hiện tại nhân vật KHÔNG dẫm lên bom (đã ra ngoài hoàn toàn) -> Bị chặn lại (thành Tường cứng)
                if (!currentHitbox.intersects(bombHitbox)) {
                    return false; 
                }
                // Nếu hiện tại đang dẫm lên bom (vừa mới đặt xong) -> Cho phép đi tiếp để thoát ra
            }
        }

        return true;
    }

    public void update() {
        if (isGameOver) {
            if (keyH.spacePressed) {
                resetGame();
                keyH.spacePressed = false;
            }
            return; 
        }

        if (gameState == GameState.PLAYING) {
            int nextPlayerX = playerX;
            int nextPlayerY = playerY;

            if (keyH.upPressed) {
                nextPlayerY -= playerSpeed;
                int targetX = ((playerX + tileSize / 2) / tileSize) * tileSize;
                if (playerX < targetX) nextPlayerX += Math.min(playerSpeed, targetX - playerX);
                else if (playerX > targetX) nextPlayerX -= Math.min(playerSpeed, playerX - targetX);
            } 
            else if (keyH.downPressed) {
                nextPlayerY += playerSpeed; // ĐÃ SỬA LỖI Ở ĐÂY: Phải là cộng (+) để đi xuống
                int targetX = ((playerX + tileSize / 2) / tileSize) * tileSize;
                if (playerX < targetX) nextPlayerX += Math.min(playerSpeed, targetX - playerX);
                else if (playerX > targetX) nextPlayerX -= Math.min(playerSpeed, playerX - targetX);
            } 
            else if (keyH.leftPressed) {
                nextPlayerX -= playerSpeed;
                int targetY = ((playerY + tileSize / 2) / tileSize) * tileSize;
                if (playerY < targetY) nextPlayerY += Math.min(playerSpeed, targetY - playerY);
                else if (playerY > targetY) nextPlayerY -= Math.min(playerSpeed, playerY - targetY);
            } 
            else if (keyH.rightPressed) {
                nextPlayerX += playerSpeed;
                int targetY = ((playerY + tileSize / 2) / tileSize) * tileSize;
                if (playerY < targetY) nextPlayerY += Math.min(playerSpeed, targetY - playerY);
                else if (playerY > targetY) nextPlayerY -= Math.min(playerSpeed, playerY - targetY);
            }

            if (canMove(nextPlayerX, playerY)) playerX = nextPlayerX;
            if (canMove(playerX, nextPlayerY)) playerY = nextPlayerY;

            // ==============================================================
            // LOGIC ĐẶT BOM (CÓ COOLDOWN VÀ CHỐNG TRÙNG LẶP Ô)
            // ==============================================================
            long currentTimeMs = System.currentTimeMillis();
            if (keyH.spacePressed) {
                // Thời gian Cooldown: 500ms (Nửa giây mới được thả bom tiếp)
                if (currentTimeMs - lastBombTime >= 500) { 
                    int bombGridX = (playerX + tileSize / 2) / tileSize;
                    int bombGridY = (playerY + tileSize / 2) / tileSize;
                    
                    // Chống đặt 2 quả bom lồng vào nhau trên cùng 1 ô vuông
                    boolean hasBombHere = false;
                    for(Bomb b : bombList) {
                        if (b.getX() == bombGridX && b.getY() == bombGridY) {
                            hasBombHere = true; 
                            break;
                        }
                    }
                    
                    if (!hasBombHere) {
                        long timeToExplode = currentTimeMs + 3000;
                        Bomb newBomb = new Bomb(bombGridX, bombGridY, tileSize, tileSize, IdObject.BOMB, timeToExplode);
                        bombQueue.enqueue(newBomb);
                        bombList.add(newBomb); // Thêm vào list để vẽ lên màn hình
                        lastBombTime = currentTimeMs; // Ghi nhận thời gian vừa đặt
                    }
                }
                keyH.spacePressed = false;
            }

            // Kích nổ bom
            if (!bombQueue.isEmpty() && currentTimeMs >= bombQueue.peek().getTimeToExplode()) {
                Bomb bombToExplode = bombQueue.dequeue();
                
                // Cực kỳ quan trọng: Xóa quả bom khỏi danh sách vẽ đồ họa dựa theo tọa độ
                bombList.removeIf(b -> b.getX() == bombToExplode.getX() && b.getY() == bombToExplode.getY());
                
                executeExplosion(bombToExplode);          
            }
            flameList.removeIf(Flame::isExpired);

            Rectangle playerHitbox = new Rectangle(playerX + 5, playerY + 5, tileSize - 10, tileSize - 10);
            
            for (Flame f : flameList) {
                Rectangle flameHitbox = new Rectangle((int) f.getX() * tileSize + 5, (int) f.getY() * tileSize + 5, tileSize - 10, tileSize - 10);
                if (playerHitbox.intersects(flameHitbox)) {
                    isGameOver = true;
                    System.out.println("YOU DIED BY FIRE!");
                }
            }

            // ==============================================================
            // TẠO BẢN ĐỒ TẠM THỜI ĐỂ QUÁI VẬT NÉ BOM
            // ==============================================================
            int[][] mapWithBombs = new int[maxScreenRow][maxScreenCol];
            int[][] originalMap = mapM.getMapMatrix();
            for (int r = 0; r < maxScreenRow; r++) {
                for (int c = 0; c < maxScreenCol; c++) {
                    mapWithBombs[r][c] = originalMap[r][c];
                }
            }
            
            // Đánh dấu các ô có bom thành tường cứng (số 1) để quái né tránh
            for (Bomb b : bombList) {
                int br = (int) b.getY();
                int bc = (int) b.getX();
                if (br >= 0 && br < maxScreenRow && bc >= 0 && bc < maxScreenCol) {
                    mapWithBombs[br][bc] = 1; 
                }
            }

            for (int i = enemyList.size() - 1; i >= 0; i--) {
                Enemy e = enemyList.get(i);
                
                // Thay vì cấp bản đồ gốc, ta cấp cho quái bản đồ đã được đánh dấu bom
                e.setRealData(mapWithBombs); 
                e.update();
                
                Rectangle enemyHitbox = new Rectangle((int)e.getX() + 5, (int)e.getY() + 5, e.getWidth() - 10, e.getHeight() - 10);
                
                if (playerHitbox.intersects(enemyHitbox)) {
                    isGameOver = true;
                    System.out.println("YOU DIED BY ENEMY!");
                }
                
                boolean isEnemyKilled = false;
                for (Flame f : flameList) {
                    Rectangle flameHitbox = new Rectangle((int) f.getX() * tileSize + 5, (int) f.getY() * tileSize + 5, tileSize - 10, tileSize - 10);
                    if (enemyHitbox.intersects(flameHitbox)) {
                        isEnemyKilled = true;
                        break;
                    }
                }
                
                if (isEnemyKilled) {
                    enemyList.remove(i); 
                    System.out.println("ENEMY KILLED!");
                }
            }

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

    private void executeExplosion(Bomb bomb) {
        int bombX = (int) bomb.getX();
        int bombY = (int) bomb.getY();
        int flameLength = 2;

        flameList.add(new Flame(bombX, bombY, tileSize, tileSize, IdObject.FLAME));

        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        int[][] currentMap = mapM.getMapMatrix();

        for (int[] dir : directions) {
            for (int i = 1; i <= flameLength; i++) {
                int nextCol = bombX + dir[1] * i;
                int nextRow = bombY + dir[0] * i;

                if (nextCol < 0 || nextCol >= maxScreenCol || nextRow < 0 || nextRow >= maxScreenRow) break;

                int tileType = currentMap[nextRow][nextCol];

                if (tileType == 1) break;
                
                if (tileType == 2) { 
                    flameList.add(new Flame(nextCol, nextRow, tileSize, tileSize, IdObject.FLAME));
                    currentMap[nextRow][nextCol] = 0; 
                    break; 
                }

                flameList.add(new Flame(nextCol, nextRow, tileSize, tileSize, IdObject.FLAME));
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        mapM.render(g2);

        if (gameState == GameState.PLAYING || gameState == GameState.PAUSE || isGameOver) {
            
            // SỬA LỖI TÀNG HÌNH: Vẽ tất cả bom trong bombList thay vì chỉ lấy 1 cái từ bombQueue
            g2.setColor(Color.ORANGE);
            for (Bomb b : bombList) {
                g2.fillOval((int) b.getX() * tileSize + 4, (int) b.getY() * tileSize + 4, tileSize - 8, tileSize - 8);
            }

            g2.setColor(Color.RED);
            for (Flame f : flameList) {
                g2.fillRect((int) f.getX() * tileSize, (int) f.getY() * tileSize, tileSize, tileSize);
            }
            
            for (Enemy e : enemyList) {
                e.render(g2); 
            }
            
            g2.drawImage(assetManager.getSprite("PLAYER"), playerX, playerY, tileSize, tileSize, null);
        }

        if (gameState == GameState.PAUSE && !isGameOver) {
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRect(0, 0, screenWidth, screenHeight);
            g2.setColor(Color.WHITE);
            g2.setFont(g2.getFont().deriveFont(30f));
            String text = "GAME PAUSED";
            int x = screenWidth / 2 - g2.getFontMetrics().stringWidth(text) / 2;
            g2.drawString(text, x, screenHeight / 2);
        }
        
        if (isGameOver) {
            g2.setColor(new Color(0, 0, 0, 180)); 
            g2.fillRect(0, 0, screenWidth, screenHeight);
            
            g2.setColor(Color.RED);
            g2.setFont(g2.getFont().deriveFont(50f));
            String textGameOver = "GAME OVER";
            int textX = screenWidth / 2 - g2.getFontMetrics().stringWidth(textGameOver) / 2;
            g2.drawString(textGameOver, textX, screenHeight / 2 - 20);
            
            g2.setColor(Color.WHITE);
            g2.setFont(g2.getFont().deriveFont(20f));
            String textPlayAgain = "Press SPACE to Play Again";
            int textPlayX = screenWidth / 2 - g2.getFontMetrics().stringWidth(textPlayAgain) / 2;
            g2.drawString(textPlayAgain, textPlayX, screenHeight / 2 + 30);
        }

        g2.dispose();
    }
}