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
import model.Player; // IMPORT CLASS PLAYER MỚI

public class GamePanel extends JPanel implements Runnable {

    final int originalTileSize = 16;
    final int scale = 3;
    public final int tileSize = originalTileSize * scale; 
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

    // SỬ DỤNG CLASS PLAYER ĐÚNG CHUẨN OOP
    Player player;
    
    ArrayList<Enemy> enemyList = new ArrayList<>();
    private MinHeapQueue bombQueue = new MinHeapQueue();
    private ArrayList<Bomb> bombList = new ArrayList<>(); 
    private ArrayList<Flame> flameList = new ArrayList<>();
    
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
        
        // Khởi tạo Player thật
        player = new Player(tileSize * 1, tileSize * 1, keyH, assetManager.getSprite("PLAYER"));

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
        player.setX(tileSize * 1);
        player.setY(tileSize * 1);
        
        mapM = new MapManager();
        graphConverter.updateGraph(mapM.getMapMatrix());
        
        bombQueue = new MinHeapQueue();
        bombList.clear(); 
        flameList.clear();
        enemyList.clear();
        
        lastBombTime = 0; 
        
        enemyList.add(new Enemy(tileSize * 13, tileSize * 1));  
        enemyList.add(new Enemy(tileSize * 1, tileSize * 11));  
        enemyList.add(new Enemy(tileSize * 13, tileSize * 11)); 
        
        isGameOver = false;
        gameState = GameState.PLAYING;
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
            
            // 1. UPDATE PLAYER (Đã uỷ quyền)
            player.setRealData(mapM.getMapMatrix(), bombList);
            player.update();

            // 2. LOGIC ĐẶT BOM (Vẫn giữ ở đây vì liên quan tương tác nhiều đối tượng)
            long currentTimeMs = System.currentTimeMillis();
            if (keyH.spacePressed) {
                if (currentTimeMs - lastBombTime >= 500) { 
                    int bombGridX = (int)((player.getX() + tileSize / 2) / tileSize);
                    int bombGridY = (int)((player.getY() + tileSize / 2) / tileSize);
                    
                    boolean hasBombHere = false;
                    for(Bomb b : bombList) {
                        if (b.getX() == bombGridX && b.getY() == bombGridY) {
                            hasBombHere = true; break;
                        }
                    }
                    if (!hasBombHere) {
                        long timeToExplode = currentTimeMs + 3000;
                        Bomb newBomb = new Bomb(bombGridX, bombGridY, tileSize, tileSize, IdObject.BOMB, timeToExplode);
                        bombQueue.enqueue(newBomb);
                        bombList.add(newBomb); 
                        lastBombTime = currentTimeMs; 
                    }
                }
                keyH.spacePressed = false;
            }

            if (!bombQueue.isEmpty() && currentTimeMs >= bombQueue.peek().getTimeToExplode()) {
                Bomb bombToExplode = bombQueue.dequeue();
                bombList.removeIf(b -> b.getX() == bombToExplode.getX() && b.getY() == bombToExplode.getY());
                executeExplosion(bombToExplode);          
            }
            flameList.removeIf(Flame::isExpired);

            Rectangle playerHitbox = new Rectangle((int)player.getX() + 5, (int)player.getY() + 5, tileSize - 10, tileSize - 10);
            
            for (Flame f : flameList) {
                Rectangle flameHitbox = new Rectangle((int) f.getX() * tileSize + 5, (int) f.getY() * tileSize + 5, tileSize - 10, tileSize - 10);
                if (playerHitbox.intersects(flameHitbox)) {
                    isGameOver = true;
                }
            }

            int[][] mapWithBombs = new int[maxScreenRow][maxScreenCol];
            int[][] originalMap = mapM.getMapMatrix();
            for (int r = 0; r < maxScreenRow; r++) {
                for (int c = 0; c < maxScreenCol; c++) mapWithBombs[r][c] = originalMap[r][c];
            }
            
            for (Bomb b : bombList) {
                int br = (int) b.getY();
                int bc = (int) b.getX();
                if (br >= 0 && br < maxScreenRow && bc >= 0 && bc < maxScreenCol) mapWithBombs[br][bc] = 1; 
            }

            for (int i = enemyList.size() - 1; i >= 0; i--) {
                Enemy e = enemyList.get(i);
                e.setRealData(mapWithBombs); 
                e.update();
                
                Rectangle enemyHitbox = new Rectangle((int)e.getX() + 5, (int)e.getY() + 5, e.getWidth() - 10, e.getHeight() - 10);
                
                if (playerHitbox.intersects(enemyHitbox)) isGameOver = true;
                
                boolean isEnemyKilled = false;
                for (Flame f : flameList) {
                    Rectangle flameHitbox = new Rectangle((int) f.getX() * tileSize + 5, (int) f.getY() * tileSize + 5, tileSize - 10, tileSize - 10);
                    if (enemyHitbox.intersects(flameHitbox)) {
                        isEnemyKilled = true; break;
                    }
                }
                
                if (isEnemyKilled) enemyList.remove(i); 
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
            
            // Uỷ quyền vẽ cho các Class tương ứng
            for (Bomb b : bombList) b.render(g2);
            for (Flame f : flameList) f.render(g2);
            for (Enemy e : enemyList) e.render(g2); 
            player.render(g2);
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