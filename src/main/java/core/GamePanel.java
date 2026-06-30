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

    // Đổi trạng thái mặc định ban đầu thành MENU thay vì PLAYING
    public GameState gameState = GameState.MENU;
    public boolean isGameOver = false;
    // Biến điều hướng menu (0: START, 1: TUTORIAL, 2: ABOUT US, 3: LEADERBOARD)
    public int menuOption = 0;

    public AssetManager assetManager = new AssetManager();
    public UIManager uiManager = new UIManager();
    public ScoreBST scoreBoard = new ScoreBST();
    Thread gameThread;
    KeyHandler keyH = new KeyHandler();
    // điểm
    public int score = 0;
    // Số mạng
    public int playerLives = 2;

    // Thời gian kết thúc trạng thái bất tử
    private long invincibleUntil = 0;
    // khai báo chiến thăngs
    public boolean isVictory = false;
    // tên người chơi
    public String playerName = "Player";
    // Danh sách các map, dễ dàng mở rộng trong tương lai
    public String[] mapList = {"Map 1 (Default)", "Map 2 (Coming Soon)", "Map 3", "Map 4"};
    public int currentMapIndex = 0; // Vị trí map đang được hiển thị

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

        assetManager.loadImage("PLAYER", "/sprites/player.png");
        assetManager.loadImage("ENEMY", "/sprites/enemy.png");

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
        // --- 1. THÊM ĐOẠN NÀY ĐỂ LUÔN TẢI LẠI ĐÚNG MAP THEO currentMapIndex ---
        String selectedMapPath = "";
        if (currentMapIndex == 0) {
            selectedMapPath = "/maps/map01.txt";
        } else if (currentMapIndex == 1) {
            selectedMapPath = "/maps/map02.txt";
        } else if (currentMapIndex == 2) {
            selectedMapPath = "/maps/map03.txt";
        } else if (currentMapIndex == 3) {
            selectedMapPath = "/maps/map04.txt";
        }
        mapM.loadMap(selectedMapPath);
        // ----------------------------------------------------------------------
        graphConverter.updateGraph(mapM.getMapMatrix());

        bombQueue = new MinHeapQueue();
        bombList.clear(); // Reset list bom
        flameList.clear();
        enemyList.clear();

        lastBombTime = 0; // Reset Cooldown

        enemyList.add(new Enemy(tileSize * 13, tileSize * 1));
        enemyList.add(new Enemy(tileSize * 1, tileSize * 11));
        enemyList.add(new Enemy(tileSize * 13, tileSize * 11));
        // --- 2. XỬ LÝ LẠI ĐIỂM SỐ VÀ MẠNG ---
        if (!isVictory) {
            // Nếu là GAME OVER hoặc Thoát về Menu thì mới bị reset máu và điểm
        playerLives = 2;
        score = 0;
}
        // GHI CHÚ: Nếu là VICTORY (Chiến thắng qua màn), trò chơi sẽ bỏ qua lệnh if này 
        // -> Giữ nguyên điểm số đang có và số mạng hiện tại để mang sang map tiếp theo.
        isGameOver = false;
        isVictory = false;
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

        if (leftCol < 0 || rightCol >= maxScreenCol || topRow < 0 || bottomRow >= maxScreenRow) {
            return false;
        }

        int[][] map = mapM.getMapMatrix();
        if (map[topRow][leftCol] != 0 || map[topRow][rightCol] != 0
                || map[bottomRow][leftCol] != 0 || map[bottomRow][rightCol] != 0) {
            return false;
        }

        // 2. KIỂM TRA VA CHẠM VỚI BOM SẴN CÓ
        Rectangle nextHitbox = new Rectangle(nextX + margin, nextY + margin, tileSize - 2 * margin, tileSize - 2 * margin);
        Rectangle currentHitbox = new Rectangle(playerX + margin, playerY + margin, tileSize - 2 * margin, tileSize - 2 * margin);

        for (Bomb b : bombList) {
            Rectangle bombHitbox = new Rectangle((int) b.getX() * tileSize, (int) b.getY() * tileSize, tileSize, tileSize);

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
        // 1. XỬ LÝ LOGIC KHI ĐANG Ở MÀN HÌNH MENU CHÍNH
        if (gameState == GameState.MENU) {
            if (keyH.upPressed) {
                menuOption--;
                if (menuOption < 0) {
                    menuOption = 4; // Quay vòng lên nút cuối
                }
                keyH.upPressed = false; // Khống chế nhận 1 lần bấm
            }
            if (keyH.downPressed) {
                menuOption++;
                if (menuOption > 4) {
                    menuOption = 0; // Quay vòng xuống nút đầu
                }
                keyH.downPressed = false;
            }
            if (keyH.enterPressed) {
                if (menuOption == 0) {
                    gameState = GameState.MAP_SELECTION; // Chuyển sang màn hình chọn map
                } else if (menuOption == 1) {
                    gameState = GameState.TUTORIAL;
                } else if (menuOption == 2) {
                    gameState = GameState.ABOUT_US;
                } else if (menuOption == 3) {
                    gameState = GameState.LEADERBOARD;
                } else if (menuOption == 4) {
                    // THÊM CHỨC NĂNG THOÁT GAME
                    System.out.println("Goodbye and see you again");
                    System.exit(0);
                }
                keyH.enterPressed = false;
            }
            return; // Không chạy logic game phía dưới khi đang ở Menu
        }
// 2. XỬ LÝ LOGIC KHI ĐANG Ở CÁC MÀN HÌNH PHỤ (Bấm ESC để quay lại)
        if (gameState == GameState.TUTORIAL || gameState == GameState.ABOUT_US || gameState == GameState.LEADERBOARD) {
            if (keyH.escapePressed) {
                gameState = GameState.MENU;
                keyH.escapePressed = false;
            }
            return;
        }

        // ===== THÊM ĐOẠN NÀY =====
        if (gameState == GameState.MAP_SELECTION) {
            if (keyH.leftPressed) {
                currentMapIndex--;
                if (currentMapIndex < 0) {
                    currentMapIndex = mapList.length - 1;
                }
                keyH.leftPressed = false;
            }

            if (keyH.rightPressed) {
                currentMapIndex++;
                if (currentMapIndex >= mapList.length) {
                    currentMapIndex = 0;
                }
                keyH.rightPressed = false;
            }

            if (keyH.enterPressed) {
                String selectedMapPath = "";

                if (currentMapIndex == 0) {
                    selectedMapPath = "/maps/map01.txt";
                } else if (currentMapIndex == 1) {
                    selectedMapPath = "/maps/map02.txt";
                } else if (currentMapIndex == 2) {
                    selectedMapPath = "/maps/map03.txt";
                } else if (currentMapIndex == 3) {
                    selectedMapPath = "/maps/map04.txt";
                }

                mapM.loadMap(selectedMapPath);
                // --- CHÈN CODE NHẬP TÊN VÀO ĐÂY ---
                String inputName = javax.swing.JOptionPane.showInputDialog(this, "playerName:");

                if (inputName != null && !inputName.trim().isEmpty()) {
                    playerName = inputName.trim();
                } else {
                    playerName = "Player"; // Gán tên mặc định nếu người chơi ấn Cancel hoặc để trống
                }

                gameState = GameState.PLAYING;
                keyH.enterPressed = false;
            }

            if (keyH.escapePressed) {
                gameState = GameState.MENU;
                keyH.escapePressed = false;
            }
            return;
        }
// ========================

        if (isGameOver || isVictory) {
            if (keyH.spacePressed) {
                // Tách riêng logic xử lý chuyển map
                if (isVictory) {
                    // CHỈ KHI CHIẾN THẮNG MỚI CHUYỂN SANG MAP TIẾP THEO
                    currentMapIndex++;
                    if (currentMapIndex >= mapList.length) {
                        currentMapIndex = 0; // Chơi lại từ đầu nếu đã vượt qua hết các map
                    }
                }
                // Nếu Game Over (isGameOver == true) thì bỏ qua bước tăng index -> Giữ nguyên map hiện tại 
                resetGame();
                keyH.spacePressed = false;
}
            if (keyH.escapePressed) {
                scoreBoard.insertScore(playerName, score); // Thêm dòng này để lưu điểm vào BXH trước
                isVictory = false; // Thêm dòng này để ép điểm về 0
                resetGame();
                gameState = GameState.MENU; // Chuyển về màn hình Menu
                keyH.escapePressed = false;
            }
            return;
        }

        if (gameState == GameState.PLAYING) {
            int nextPlayerX = playerX;
            int nextPlayerY = playerY;

            if (keyH.upPressed) {
                nextPlayerY -= playerSpeed;
                int targetX = ((playerX + tileSize / 2) / tileSize) * tileSize;
                if (playerX < targetX) {
                    nextPlayerX += Math.min(playerSpeed, targetX - playerX);
                } else if (playerX > targetX) {
                    nextPlayerX -= Math.min(playerSpeed, playerX - targetX);
                }
            } else if (keyH.downPressed) {
                nextPlayerY += playerSpeed; // ĐÃ SỬA LỖI Ở ĐÂY: Phải là cộng (+) để đi xuống
                int targetX = ((playerX + tileSize / 2) / tileSize) * tileSize;
                if (playerX < targetX) {
                    nextPlayerX += Math.min(playerSpeed, targetX - playerX);
                } else if (playerX > targetX) {
                    nextPlayerX -= Math.min(playerSpeed, playerX - targetX);
                }
            } else if (keyH.leftPressed) {
                nextPlayerX -= playerSpeed;
                int targetY = ((playerY + tileSize / 2) / tileSize) * tileSize;
                if (playerY < targetY) {
                    nextPlayerY += Math.min(playerSpeed, targetY - playerY);
                } else if (playerY > targetY) {
                    nextPlayerY -= Math.min(playerSpeed, playerY - targetY);
                }
            } else if (keyH.rightPressed) {
                nextPlayerX += playerSpeed;
                int targetY = ((playerY + tileSize / 2) / tileSize) * tileSize;
                if (playerY < targetY) {
                    nextPlayerY += Math.min(playerSpeed, targetY - playerY);
                } else if (playerY > targetY) {
                    nextPlayerY -= Math.min(playerSpeed, playerY - targetY);
                }
            }

            if (canMove(nextPlayerX, playerY)) {
                playerX = nextPlayerX;
            }
            if (canMove(playerX, nextPlayerY)) {
                playerY = nextPlayerY;
            }

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
                    for (Bomb b : bombList) {
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
            //Chỉ kiểm tra va chạm khi KHÔNG bất tử
            boolean invincible = System.currentTimeMillis() < invincibleUntil;
            //-----------------------
            if (!invincible) {
                for (Flame f : flameList) {
                    Rectangle flameHitbox = new Rectangle((int) f.getX() * tileSize + 5, (int) f.getY() * tileSize + 5, tileSize - 10, tileSize - 10);
                    if (playerHitbox.intersects(flameHitbox)) {
                        playerLives--;

                        if (playerLives <= 0) {
                            isGameOver = true;
                            scoreBoard.insertScore(playerName, score);
                            System.out.println("GAME OVER!");
                        } else {
                            // Hồi sinh
                            playerX = tileSize;
                            playerY = tileSize;
                            // thời gian được bất tử
                            invincibleUntil = System.currentTimeMillis() + 2000;
                        }

                        break;
                    }
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

                Rectangle enemyHitbox = new Rectangle((int) e.getX() + 5, (int) e.getY() + 5, e.getWidth() - 10, e.getHeight() - 10);

                if (!invincible && playerHitbox.intersects(enemyHitbox)) {

                    playerLives--;

                    if (playerLives <= 0) {
                        isGameOver = true;
                        scoreBoard.insertScore(playerName, score);
                    } else {
                        playerX = tileSize;
                        playerY = tileSize;
                        invincibleUntil = System.currentTimeMillis() + 2000;
                    }

                    break;
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
                    // THÊM DÒNG NÀY ĐỂ CỘNG ĐIỂM
                    score += 100;

                    System.out.println("ENEMY KILLED!" + playerName + "Score: " + score);
                }
            }
            // Nếu danh sách quái trống và chưa Game Over -> Chiến thắng
            if (enemyList.isEmpty() && !isGameOver) {
                isVictory = true;
                // Bỏ lệnh lưu điểm cũ và thêm điều kiện này:
                // Chỉ lưu điểm 1 lần duy nhất khi người chơi vượt qua Map cuối cùng
if (currentMapIndex == mapList.length - 1) {
                scoreBoard.insertScore(playerName, score); // Lưu điểm vào bảng xếp hạng
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

            // --- THÊM ĐOẠN CODE NÀY ĐỂ THOÁT VỀ MENU BẰNG ESC ---
            if (keyH.escapePressed) {
                scoreBoard.insertScore(playerName, score); // Thêm dòng này để lưu điểm vào BXH trước
                isVictory = false; // Thêm dòng này để ép điểm về 0
                resetGame(); // Đặt lại các thông số (vị trí, máu, điểm...) để không bị dính vào ván cũ
                gameState = GameState.MENU; // Đưa trạng thái về Menu chính
                keyH.escapePressed = false;
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

                if (nextCol < 0 || nextCol >= maxScreenCol || nextRow < 0 || nextRow >= maxScreenRow) {
                    break;
                }

                int tileType = currentMap[nextRow][nextCol];

                if (tileType == 1) {
                    break;
                }

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
        // Phân luồng vẽ dựa trên trạng thái hiện tại của Game
        if (gameState == GameState.MENU) {
            drawMenu(g2);
        } else if (gameState == GameState.MAP_SELECTION) {
            drawMapSelection(g2);
        } else if (gameState == GameState.TUTORIAL) {
            drawTutorial(g2);
        } else if (gameState == GameState.ABOUT_US) {
            drawAboutUs(g2);
        } else if (gameState == GameState.LEADERBOARD) {
            drawLeaderboard(g2);
        } else {
            mapM.render(g2);
if (gameState == GameState.PLAYING || gameState == GameState.PAUSE || isGameOver || isVictory) {

                // SỬA LỖI TÀNG HÌNH: Vẽ tất cả bom trong bombList thay vì chỉ lấy 1 cái từ bombQueue
                g2.setColor(Color.ORANGE);
                for (Bomb b : bombList) {
                    g2.fillOval((int) b.getX() * tileSize + 4, (int) b.getY() * tileSize + 4, tileSize - 8, tileSize - 8);
                }

                g2.setColor(Color.RED);
                for (Flame f : flameList) {
                    g2.fillRect((int) f.getX() * tileSize, (int) f.getY() * tileSize, tileSize, tileSize);
                }

                // ==================================================
                // SỬA LỖI VẼ QUÁI VẬT (CHOVY): Dùng vòng lặp enemyList
                // ==================================================
                for (Enemy e : enemyList) {
                    if (assetManager.getSprite("ENEMY") != null) {
                        // Lấy tọa độ của từng con quái trong danh sách để vẽ ảnh
                        g2.drawImage(assetManager.getSprite("ENEMY"), (int) e.getX(), (int) e.getY(), tileSize, tileSize, null);
                    } else {
                        e.render(g2);
                    }
                }

                // ==================================================
                // SỬA LỖI VẼ NHÂN VẬT (FAKER): Chỉ cần gọi 1 lần
                // ==================================================
                if (assetManager.getSprite("PLAYER") != null) {
                    boolean invincible = System.currentTimeMillis() < invincibleUntil;
                    if (!invincible || System.currentTimeMillis() / 100 % 2 == 0) {
                        g2.drawImage(assetManager.getSprite("PLAYER"), playerX, playerY, tileSize, tileSize, null);
                    }
                }

                // VẼ ĐIỂM SỐ và số mạng Ở GÓC PHẢI PHÍA TRÊN
                g2.setColor(Color.WHITE);
                g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));
                String scoreText = "Score: " + score;
                g2.drawString("Lives: " + playerLives, 20, 40);
                // Tọa độ X tính bằng cách lấy chiều rộng màn hình trừ đi độ dài chữ
                int stringWidth = g2.getFontMetrics().stringWidth(scoreText);
                g2.drawString(scoreText, screenWidth - stringWidth - 20, 40);
            }

            if (gameState == GameState.PAUSE && !isGameOver) {
                g2.setColor(new Color(0, 0, 0, 150));
                g2.fillRect(0, 0, screenWidth, screenHeight);
                g2.setColor(Color.WHITE);
                g2.setFont(g2.getFont().deriveFont(30f));
                String text = "GAME PAUSED";
                int x = screenWidth / 2 - g2.getFontMetrics().stringWidth(text) / 2;
g2.drawString(text, x, screenHeight / 2);

                // 2. Chữ Hướng dẫn (THÊM MỚI VÀO ĐÂY)
                g2.setFont(g2.getFont().deriveFont(20f)); // Giảm kích thước font chữ xuống 20
                String subText = "Press Esc to go back to the menu or P to continue";
                int subX = screenWidth / 2 - g2.getFontMetrics().stringWidth(subText) / 2;
                g2.drawString(subText, subX, screenHeight / 2 + 40); // Đẩy y xuống 40 pixel so với chữ gốc
            }

            if (isGameOver) {
                g2.setColor(new Color(0, 0, 0, 180));
                g2.fillRect(0, 0, screenWidth, screenHeight);

                // 1. Chữ GAME OVER (Đẩy lên cao hơn một chút)
                g2.setColor(Color.RED);
                g2.setFont(g2.getFont().deriveFont(50f));
                String textGameOver = "GAME OVER";
                int textX = screenWidth / 2 - g2.getFontMetrics().stringWidth(textGameOver) / 2;
                g2.drawString(textGameOver, textX, screenHeight / 2 - 40);

                // 2. BỔ SUNG: Chữ hiển thị Điểm số (Nằm ở chính giữa)
                g2.setColor(Color.WHITE);
                g2.setFont(g2.getFont().deriveFont(30f));
                String textScore = "Score: " + score;
                int textScoreX = screenWidth / 2 - g2.getFontMetrics().stringWidth(textScore) / 2;
                g2.drawString(textScore, textScoreX, screenHeight / 2);

                // 3. Chữ Hướng dẫn chơi lại (Đẩy xuống dưới)
                g2.setFont(g2.getFont().deriveFont(20f));
                String textPlayAgain = "Press SPACE to Play Again";
                int textPlayX = screenWidth / 2 - g2.getFontMetrics().stringWidth(textPlayAgain) / 2;
                g2.drawString(textPlayAgain, textPlayX, screenHeight / 2 + 40);

                // 4. Chữ Hướng dẫn về Menu (Đẩy xuống dưới cùng)
                String textMenu = "Press ESC to Back to Menu";
                int textMenuX = screenWidth / 2 - g2.getFontMetrics().stringWidth(textMenu) / 2;
                g2.drawString(textMenu, textMenuX, screenHeight / 2 + 70);
            } else if (isVictory) {
                g2.setColor(new Color(0, 0, 0, 180));
                g2.fillRect(0, 0, screenWidth, screenHeight);

                g2.setColor(Color.YELLOW);
                g2.setFont(g2.getFont().deriveFont(50f));
                String textVictory = "VICTORY";
                int textX = screenWidth / 2 - g2.getFontMetrics().stringWidth(textVictory) / 2;
                g2.drawString(textVictory, textX, screenHeight / 2 - 50);

                g2.setColor(Color.WHITE);
                g2.setFont(g2.getFont().deriveFont(30f));
                String textScore = "Score: " + score;
                int textScoreX = screenWidth / 2 - g2.getFontMetrics().stringWidth(textScore) / 2;
                g2.drawString(textScore, textScoreX, screenHeight / 2 - 10);
g2.setFont(g2.getFont().deriveFont(20f));
                String textPlayAgain = "Press SPACE to Play Again";
                int textPlayX = screenWidth / 2 - g2.getFontMetrics().stringWidth(textPlayAgain) / 2;
                g2.drawString(textPlayAgain, textPlayX, screenHeight / 2 + 30);

                String textMenu = "Press ESC to Back to Menu";
                int textMenuX = screenWidth / 2 - g2.getFontMetrics().stringWidth(textMenu) / 2;
                g2.drawString(textMenu, textMenuX, screenHeight / 2 + 60);
            }

            g2.dispose();
        }

    }
    // --- HÀM VẼ MENU CHÍNH ---

    private void drawMenu(Graphics2D g2) {
        // Vẽ nền tối cho Menu
        g2.setColor(new Color(20, 20, 30));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        // Vẽ Tiêu đề Game
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 48));
        g2.setColor(Color.YELLOW);
        String title = "BOMBERMAN CSD201";
        int x = screenWidth / 2 - g2.getFontMetrics().stringWidth(title) / 2;
        g2.drawString(title, x, 120);

        // Danh sách các tùy chọn nút
        String[] options = {"START GAME", "TUTORIAL", "ABOUT US", "LEADERBOARD", "QUIT"};
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 26));

        for (int i = 0; i < options.length; i++) {
            String optionText = options[i];
            int textX = screenWidth / 2 - g2.getFontMetrics().stringWidth(optionText) / 2;
            int textY = 240 + (i * 60);

            if (i == menuOption) {
                g2.setColor(Color.CYAN);
                // Vẽ mũi tên trỏ vào nút đang chọn (Ứng dụng tư duy con trỏ cấu trúc dữ liệu)
                g2.drawString("> " + optionText + " <", textX - 30, textY);
            } else {
                g2.setColor(Color.WHITE);
                g2.drawString(optionText, textX, textY);
            }
        }

        // Gợi ý điều khiển bên dưới
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 20));
        g2.setColor(Color.red);
        String hint = "Use W/S to Navigate | Press ENTER to Select | press Esc to exit";
        int hintX = screenWidth / 2 - g2.getFontMetrics().stringWidth(hint) / 2;
        g2.drawString(hint, hintX, screenHeight - 450);
    }

    // --- HÀM VẼ HƯỚNG DẪN (TUTORIAL) ---
    private void drawTutorial(Graphics2D g2) {
        g2.setColor(new Color(30, 40, 40));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 36));
        g2.setColor(Color.WHITE);
        g2.drawString("TUTORIAL", 50, 80);

        // VÙNG TRỐNG ĐỂ BẠN ĐIỀN THÔNG TIN CHI TIẾT
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 20));
        g2.setColor(Color.LIGHT_GRAY);

        g2.drawString("- Press W, A, S, D to Move the Player.", 70, 160);
g2.drawString("- Press SPACE to Place a Bomb.", 70, 210);
        g2.drawString("- Avoid Flame and Enemies to survive.", 70, 260);
        g2.drawString("- press P to pause the game.", 70, 310);

        // Nút bấm quay lại
        drawBackButtonHint(g2);
    }

    // --- HÀM VẼ THÔNG TIN (ABOUT US) ---
    private void drawAboutUs(Graphics2D g2) {
        g2.setColor(new Color(40, 30, 40));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 36));
        g2.setColor(Color.WHITE);
        g2.drawString("ABOUT US", 50, 80);

        // VÙNG TRỐNG ĐỂ BẠN ĐIỀN THÔNG TIN THÀNH VIÊN NHÓM
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 20));
        g2.setColor(Color.LIGHT_GRAY);

        g2.drawString("Course: Data Structures and Algorithms (CSD201)", 70, 160);
        g2.drawString("Institution: FPT University", 70, 210);
        g2.drawString("[CE200304 - Nguyễn Trần Khả Nhân - leader]", 70, 260);
        g2.drawString("[CE201492 - Lương Trung Hiếu]", 70, 310);
        g2.drawString("[CE201621 - Nguyễn Minh Phát]", 70, 360);
        g2.drawString("[CE201183 -Đỗ Trần Thiên Phúc]", 70, 410);
        g2.drawString("[CE201665 - Lê Nguyễn Thành Tài]", 70, 460);
        g2.drawString("[CE201233 -Trương Anh Tuấn]", 70, 510);
        drawBackButtonHint(g2);
    }

    // Hàm lưu điểm theo định dạng dấu phẩy của bạn
    public void saveScoreToLeaderboard() {
        try (java.io.FileWriter fw = new java.io.FileWriter("duong_dan_file_cua_ban.txt", true); java.io.PrintWriter pw = new java.io.PrintWriter(fw)) {
            pw.println(playerName + "," + score); // Định dạng Name,Score[cite: 4]
        } catch (java.io.IOException e) {
            System.out.println("Lỗi lưu điểm: " + e.getMessage());
        }
    }

    // --- HÀM VẼ BẢNG XẾP HẠNG (LEADERBOARD) ---
    private void drawLeaderboard(Graphics2D g2) {
        g2.setColor(new Color(20, 30, 20));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 36));
        g2.setColor(Color.blue);
        g2.drawString("TOP LEADERS", 50, 80);

        g2.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 20));
        g2.setColor(Color.WHITE);

        // Nơi bạn có thể duyệt cây ScoreBST hoặc hiển thị danh sách tĩnh mẫu
        String[] lines = scoreBoard.getLeaderboard().split("\n");

        int y = 130; // Đẩy danh sách lên trên (trước đây là 210)

        int count = 0;
        for (String line : lines) {
            // Loại bỏ các dòng trống
            if (line != null && !line.trim().isEmpty()) {
                g2.drawString(line, 80, y);
                y += 35; // Giảm khoảng cách giữa các dòng xuống 35
                count++;
            }
            
            // Ngắt vòng lặp nếu đã vẽ đủ top 10
if (count >= 10) {
                break; 
            }
        }

        drawBackButtonHint(g2);
    }

    private void drawMapSelection(Graphics2D g2) {
        g2.setColor(new Color(20, 20, 30));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setColor(Color.YELLOW);
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 48));
        String title = "SELECT MAP";
        int titleX = screenWidth / 2 - g2.getFontMetrics().stringWidth(title) / 2;
        g2.drawString(title, titleX, 150);

        g2.setColor(Color.CYAN);
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 36));
        String mapName = "<   " + mapList[currentMapIndex] + "   >";
        int mapX = screenWidth / 2 - g2.getFontMetrics().stringWidth(mapName) / 2;
        g2.drawString(mapName, mapX, screenHeight / 2);

        g2.setColor(Color.WHITE);
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 20));
        String progress = (currentMapIndex + 1) + " / " + mapList.length;
        int progX = screenWidth / 2 - g2.getFontMetrics().stringWidth(progress) / 2;
        g2.drawString(progress, progX, screenHeight / 2 + 50);

        // Hướng dẫn thao tác với phím A/D
        g2.setColor(Color.RED);
        String hint = "Use A/D to Choose | ENTER to Play | ESC to Return";
        int hintX = screenWidth / 2 - g2.getFontMetrics().stringWidth(hint) / 2;
        g2.drawString(hint, hintX, screenHeight - 80);
    }

    // Gợi ý phím thoát ở góc màn hình phụ
    private void drawBackButtonHint(Graphics2D g2) {
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.ITALIC, 16));
        g2.setColor(Color.ORANGE);
        g2.drawString("<- Press ESC to Return Menu", 40, screenHeight - 50);
    }

}
