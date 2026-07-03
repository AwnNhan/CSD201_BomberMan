package core;

import algorithm.CustomLinkedList;
import algorithm.GraphConverter;
import algorithm.MinHeapQueue;
import algorithm.ScoreBST;
import config.LevelConfig;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
import map.MapManager;
import model.Bomb;
import model.Enemy;
import model.Flame;
import model.GameObject;
import model.IdObject;
import model.Player;

public class GamePanel extends JPanel implements Runnable {

    final int originalTileSize = 16;
    final int scale = 3;
    public final int tileSize = originalTileSize * scale;
    public final int maxScreenCol = 15;
    public final int maxScreenRow = 13;
    public final int screenWidth = tileSize * maxScreenCol;
    public final int screenHeight = tileSize * maxScreenRow;

    public GameState gameState = GameState.MENU;
    public boolean isGameOver = false;
    public int menuOption = 0;

    public AssetManager assetManager = new AssetManager();
    public UIManager uiManager = new UIManager();
    public ScoreBST scoreBoard = new ScoreBST();
    Thread gameThread;
    KeyHandler keyH = new KeyHandler();

    public int score = 0;
    public int playerLives = 2;
    private long invincibleUntil = 0;
    public boolean isVictory = false;
    public String playerName = "Player";

    public String[] mapList = {"Map 1 (Default)", "Map 2 (Coming Soon)", "Map 3", "Map 4"};
    public int currentMapIndex = 0;

    // === HỆ THỐNG QUẢN LÝ MỚI: XÓA ARRAYLIST VÀ BIẾN RỜI RẠC ===
    MapManager mapM;
    GraphConverter graphConverter = new GraphConverter();
    public CollisionChecker cChecker;
    public CustomLinkedList objectList; // Băng chuyền O(1)
    public Player player;

    private MinHeapQueue bombQueue;
    private long lastBombTime = 0;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.addKeyListener(keyH);

        mapM = new MapManager();
        cChecker = new CollisionChecker(mapM);
        graphConverter.updateGraph(mapM.getMapMatrix());

        objectList = new CustomLinkedList();
        bombQueue = new MinHeapQueue();

        assetManager.loadImage("PLAYER", "/sprites/player.png");
        assetManager.loadImage("ENEMY", "/sprites/enemy.png");
        assetManager.loadImage("PLAYER_UP", "/sprites/player_up.png");
        assetManager.loadImage("PLAYER_DOWN", "/sprites/player_down.png");
        assetManager.loadImage("ENEMY_UP", "/sprites/enemy_up.png");
        assetManager.loadImage("ENEMY_DOWN", "/sprites/enemy_down.png");
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
        mapM = new MapManager();

        // 1. LẤY CẤU HÌNH CỦA LEVEL HIỆN TẠI TỪ PACKAGE CONFIG
        LevelConfig currentConfig = config.LevelManager.getLevel(currentMapIndex);

        // 2. NẠP FILE MAP TƯƠNG ỨNG VỚI LEVEL ĐÓ
        mapM.loadMap(currentConfig.getMapFilePath());

        cChecker = new CollisionChecker(mapM);
        graphConverter.updateGraph(mapM.getMapMatrix());

        // Reset hệ thống
        objectList = new CustomLinkedList();
        bombQueue = new MinHeapQueue();
        lastBombTime = 0;

        if (!isVictory) {
            playerLives = 2;
            score = 0;
        }
        isGameOver = false;
        isVictory = false;

        // Khởi tạo Nhân vật và nạp lên băng chuyền
        player = new Player(tileSize, tileSize, keyH, cChecker);
        objectList.addLast(player);

        // 3. SINH QUÁI VẬT TỰ ĐỘNG DỰA TRÊN SỐ LƯỢNG VÀ TỐC ĐỘ CỦA LEVEL
        // Thay vì addLast 3 con quái cố định, ta dùng vòng lặp theo Config:
        int addedEnemies = 0;
        int[][] matrix = mapM.getMapMatrix();

        for (int r = maxScreenRow - 1; r >= 0 && addedEnemies < currentConfig.getEnemyCount(); r--) {
            for (int c = maxScreenCol - 1; c >= 0 && addedEnemies < currentConfig.getEnemyCount(); c--) {
                // Chỉ sinh quái ở những ô đường trống (giá trị 0) và xa nhân vật
                if (matrix[r][c] == 0 && (r > 3 || c > 3)) {
                    objectList.addLast(new Enemy(c * tileSize, r * tileSize, currentConfig.getEnemySpeed()));
                    addedEnemies++;
                }
            }
        }

        gameState = GameState.PLAYING;
    }

    public void update() {
        // --- LOGIC MENU & ĐIỀU HƯỚNG MÀN HÌNH ---
        if (gameState == GameState.MENU) {
            if (keyH.upPressed) {
                menuOption--;
                if (menuOption < 0) {
                    menuOption = 4;
                }
                keyH.upPressed = false;
            }
            if (keyH.downPressed) {
                menuOption++;
                if (menuOption > 4) {
                    menuOption = 0;
                }
                keyH.downPressed = false;
            }
            if (keyH.enterPressed) {
                if (menuOption == 0) {
                    gameState = GameState.MAP_SELECTION;
                } else if (menuOption == 1) {
                    gameState = GameState.TUTORIAL;
                } else if (menuOption == 2) {
                    gameState = GameState.ABOUT_US;
                } else if (menuOption == 3) {
                    gameState = GameState.LEADERBOARD;
                } else if (menuOption == 4) {
                    System.exit(0);
                }
                keyH.enterPressed = false;
            }
            return;
        }

        if (gameState == GameState.TUTORIAL || gameState == GameState.ABOUT_US || gameState == GameState.LEADERBOARD) {
            if (keyH.escapePressed) {
                gameState = GameState.MENU;
                keyH.escapePressed = false;
            }
            return;
        }

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
                String inputName = javax.swing.JOptionPane.showInputDialog(this, "Nhập tên người chơi:");
                playerName = (inputName != null && !inputName.trim().isEmpty()) ? inputName.trim() : "Player";
                resetGame();
                keyH.enterPressed = false;
            }
            if (keyH.escapePressed) {
                gameState = GameState.MENU;
                keyH.escapePressed = false;
            }
            return;
        }

        if (isGameOver || isVictory) {
            if (keyH.spacePressed) {
                if (isVictory) {
                    currentMapIndex++;
                    if (currentMapIndex >= mapList.length) {
                        currentMapIndex = 0;
                    }
                }
                resetGame();
                keyH.spacePressed = false;
            }
            if (keyH.escapePressed) {
                scoreBoard.insertScore(playerName, score);
                isVictory = false;
                gameState = GameState.MENU;
                keyH.escapePressed = false;
            }
            return;
        }

        // --- LOGIC PLAYING CHÍNH THỨC ---
        if (gameState == GameState.PLAYING) {
            long currentTimeMs = System.currentTimeMillis();

            // 1. ĐẶT BOM
            if (keyH.spacePressed && currentTimeMs - lastBombTime >= 500) {
                int bombX = ((int) player.getX() + tileSize / 2) / tileSize * tileSize;
                int bombY = ((int) player.getY() + tileSize / 2) / tileSize * tileSize;

                boolean hasBombHere = false;
                CustomLinkedList.Node temp = objectList.head;
                while (temp != null) {
                    if (temp.data.getId() == IdObject.BOMB && temp.data.getX() == bombX && temp.data.getY() == bombY) {
                        hasBombHere = true;
                    }
                    temp = temp.next;
                }

                if (!hasBombHere) {
                    Bomb b = new Bomb(bombX, bombY, tileSize, tileSize, IdObject.BOMB, currentTimeMs + 3000);
                    bombQueue.enqueue(b);
                    objectList.addLast(b);
                    lastBombTime = currentTimeMs;
                }
                keyH.spacePressed = false;
            }

            // 2. KÍCH NỔ BOM
            if (!bombQueue.isEmpty() && currentTimeMs >= bombQueue.peek().getTimeToExplode()) {
                Bomb b = bombQueue.dequeue();

                // Gỡ bom khỏi băng chuyền và thả lửa
                CustomLinkedList.Node temp = objectList.head;
                while (temp != null) {
                    if (temp.data == b) {
                        objectList.removeNode(temp);
                        break;
                    }
                    temp = temp.next;
                }
                executeExplosion(b);
            }

            // Tạo map phụ chứa bom để quái né
            int[][] mapWithBombs = new int[maxScreenRow][maxScreenCol];
            int[][] originalMap = mapM.getMapMatrix();
            for (int r = 0; r < maxScreenRow; r++) {
                System.arraycopy(originalMap[r], 0, mapWithBombs[r], 0, maxScreenCol);
            }

            CustomLinkedList.Node t = objectList.head;
            while (t != null) {
                if (t.data.getId() == IdObject.BOMB) {
                    mapWithBombs[(int) t.data.getY() / tileSize][(int) t.data.getX() / tileSize] = 1;
                }
                t = t.next;
            }

            // =================================================================
            // 3. VÒNG LẶP UPDATE THẦN THÁNH: O(1) CHO MỌI THAO TÁC XÓA
            // =================================================================
            CustomLinkedList.Node current = objectList.head;
            int enemyCount = 0;
            boolean invincible = System.currentTimeMillis() < invincibleUntil;

            while (current != null) {
                CustomLinkedList.Node nextNode = current.next; // Lưu mắt xích tiếp theo
                GameObject obj = current.data;

                obj.update(); // Mọi vật thể tự update vị trí

                // Xử lý Quái
                if (obj.getId() == IdObject.ENEMY) {
                    ((Enemy) obj).setRealData(mapWithBombs);
                    enemyCount++;
                    if (!invincible && cChecker.checkEntity(player.getHitbox(), obj.getHitbox())) {
                        killPlayer();
                    }
                } // Xử lý Lửa
                else if (obj.getId() == IdObject.FLAME) {
                    Flame f = (Flame) obj;
                    if (f.isExpired()) {
                        objectList.removeNode(current); // XÓA LỬA TỨC THỜI (O(1))
                    } else {
                        if (!invincible && cChecker.checkEntity(player.getHitbox(), f.getHitbox())) {
                            killPlayer();
                        }

                        // Kiểm tra lửa đốt quái
                        CustomLinkedList.Node inner = objectList.head;
                        while (inner != null) {
                            if (inner.data.getId() == IdObject.ENEMY && cChecker.checkEntity(f.getHitbox(), inner.data.getHitbox())) {
                                objectList.removeNode(inner); // TIÊU DIỆT QUÁI (O(1))
                                score += 100;
                            }
                            inner = inner.next;
                        }
                    }
                }
                current = nextNode; // Bước tiếp trên băng chuyền
            }

            // Kiểm tra Win
            if (enemyCount == 0 && !isGameOver) {
                isVictory = true;
                if (currentMapIndex == mapList.length - 1) {
                    scoreBoard.insertScore(playerName, score);
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
            if (keyH.escapePressed) {
                scoreBoard.insertScore(playerName, score);
                isVictory = false;
                resetGame();
                gameState = GameState.MENU;
                keyH.escapePressed = false;
            }
        }
    }

    private void killPlayer() {
        playerLives--;
        if (playerLives <= 0) {
            isGameOver = true;
            scoreBoard.insertScore(playerName, score);
        } else {
            player.setX(tileSize);
            player.setY(tileSize);
            invincibleUntil = System.currentTimeMillis() + 2000;
        }
    }

    private void executeExplosion(Bomb bomb) {
        int bx = (int) bomb.getX();
        int by = (int) bomb.getY();
        objectList.addLast(new Flame(bx, by, tileSize, tileSize, IdObject.FLAME));

        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        int[][] map = mapM.getMapMatrix();

        for (int[] dir : dirs) {
            for (int i = 1; i <= 2; i++) { // Flame length = 2
                int nextCol = bx / tileSize + dir[1] * i;
                int nextRow = by / tileSize + dir[0] * i;

                if (nextCol < 0 || nextCol >= maxScreenCol || nextRow < 0 || nextRow >= maxScreenRow) {
                    break;
                }

                int tileType = map[nextRow][nextCol];
                if (tileType == 1) {
                    break; // Kẹt Tường
                }
                if (tileType == 2) {
                    objectList.addLast(new Flame(nextCol * tileSize, nextRow * tileSize, tileSize, tileSize, IdObject.FLAME));
                    mapM.destroySoftWall(nextRow, nextCol); // Phá gạch
                    break;
                }
                objectList.addLast(new Flame(nextCol * tileSize, nextRow * tileSize, tileSize, tileSize, IdObject.FLAME));
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

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

                // --- DUYỆT RENDER TOÀN BỘ VẬT THỂ TRÊN BĂNG CHUYỀN ---
                CustomLinkedList.Node current = objectList.head;
                while (current != null) {
                    GameObject obj = current.data;

                    if (obj.getId() == IdObject.PLAYER) {
                        if (System.currentTimeMillis() > invincibleUntil || System.currentTimeMillis() / 100 % 2 == 0) {
                            String dir = player.getDirection();

                            if ("UP".equals(dir)) {
                                g2.drawImage(assetManager.getSprite("PLAYER_UP"),
                                        (int) player.getX(), (int) player.getY(), tileSize, tileSize, null);

                            } else if ("DOWN".equals(dir)) {
                                g2.drawImage(assetManager.getSprite("PLAYER_DOWN"),
                                        (int) player.getX(), (int) player.getY(), tileSize, tileSize, null);

                            } else {
                                // Xử lý riêng cho đi ngang (trái/phải) dùng ảnh "PLAYER"
                                if (player.isFacingLeft()) {
                                    g2.drawImage(assetManager.getSprite("PLAYER"),
                                            (int) player.getX() + tileSize, (int) player.getY(),
                                            -tileSize, tileSize, null);
                                } else {
                                    g2.drawImage(assetManager.getSprite("PLAYER"),
                                            (int) player.getX(), (int) player.getY(),
                                            tileSize, tileSize, null);
                                }
                            }
                        } else {
                            obj.render(g2);
                        }
                    } else if (obj.getId() == IdObject.ENEMY) {
                        Enemy e = (Enemy) obj;
                        String direction = e.getDirection();

                        // Dùng equalsIgnoreCase để đảm bảo luôn khớp dù là "up" hay "UP"
                        if ("UP".equalsIgnoreCase(direction)) {

                            g2.drawImage(assetManager.getSprite("ENEMY_UP"),
                                    (int) obj.getX(), (int) obj.getY(), tileSize, tileSize, null);

                        } else if ("DOWN".equalsIgnoreCase(direction)) {

                            g2.drawImage(assetManager.getSprite("ENEMY_DOWN"),
                                    (int) obj.getX(), (int) obj.getY(), tileSize, tileSize, null);

                        } else if ("LEFT".equalsIgnoreCase(direction)) {

                            // Lật ảnh ENEMY mặc định cho hướng trái
                            g2.drawImage(assetManager.getSprite("ENEMY"),
                                    (int) obj.getX() + tileSize, (int) obj.getY(),
                                    -tileSize, tileSize, null);

                        } else {

                            // Hướng phải (right) hoặc mặc định
                            if (assetManager.getSprite("ENEMY") != null) {
                                g2.drawImage(assetManager.getSprite("ENEMY"),
                                        (int) obj.getX(), (int) obj.getY(),
                                        tileSize, tileSize, null);
                            } else {
                                obj.render(g2);
                            }
                        }
                    } else if (obj.getId() == IdObject.BOMB) {
                        g2.setColor(Color.ORANGE);
                        g2.fillOval((int) obj.getX() + 4, (int) obj.getY() + 4, tileSize - 8, tileSize - 8);
                    } else if (obj.getId() == IdObject.FLAME) {
                        g2.setColor(Color.RED);
                        g2.fillRect((int) obj.getX(), (int) obj.getY(), tileSize, tileSize);
                    }
                    current = current.next;
                }

                // Render UI (Điểm & Mạng)
                g2.setColor(Color.WHITE);
                g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));
                g2.drawString("Lives: " + playerLives, 20, 40);
                String scoreText = "Score: " + score;
                g2.drawString(scoreText, screenWidth - g2.getFontMetrics().stringWidth(scoreText) - 20, 40);
            }

            // Giao diện Game Over / Pause / Victory (Giữ nguyên của bạn)
            if (gameState == GameState.PAUSE && !isGameOver) {
                g2.setColor(new Color(0, 0, 0, 150));
                g2.fillRect(0, 0, screenWidth, screenHeight);
                g2.setColor(Color.WHITE);
                g2.setFont(g2.getFont().deriveFont(30f));
                g2.drawString("GAME PAUSED", screenWidth / 2 - 100, screenHeight / 2);
                g2.setFont(g2.getFont().deriveFont(20f));
                g2.drawString("Press Esc to go back to the menu or P to continue", screenWidth / 2 - 230, screenHeight / 2 + 40);
            } else if (isGameOver || isVictory) {
                g2.setColor(new Color(0, 0, 0, 180));
                g2.fillRect(0, 0, screenWidth, screenHeight);
                g2.setColor(isVictory ? Color.YELLOW : Color.RED);
                g2.setFont(g2.getFont().deriveFont(50f));
                g2.drawString(isVictory ? "VICTORY" : "GAME OVER", screenWidth / 2 - (isVictory ? 100 : 130), screenHeight / 2 - 40);

                g2.setColor(Color.WHITE);
                g2.setFont(g2.getFont().deriveFont(30f));
                g2.drawString("Score: " + score, screenWidth / 2 - 70, screenHeight / 2);

                g2.setFont(g2.getFont().deriveFont(20f));
                g2.drawString("Press SPACE to " + (isVictory ? "Continue" : "Play Again"), screenWidth / 2 - 120, screenHeight / 2 + 40);
                g2.drawString("Press ESC to Back to Menu", screenWidth / 2 - 125, screenHeight / 2 + 70);
            }
        }
        g2.dispose();
    }

    // --- CÁC HÀM VẼ GIAO DIỆN (Đã sửa lỗi khai báo lửng lơ vòng lặp) ---
    private void drawMenu(Graphics2D g2) {
        g2.setColor(new Color(20, 20, 30));
        g2.fillRect(0, 0, screenWidth, screenHeight);
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 48));
        g2.setColor(Color.YELLOW);
        g2.drawString("BOMBERMAN CSD201", screenWidth / 2 - 250, 120);

        String[] options = {"START GAME", "TUTORIAL", "ABOUT US", "LEADERBOARD", "QUIT"};
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 26));
        for (int i = 0; i < options.length; i++) {
            if (i == menuOption) {
                g2.setColor(Color.CYAN);
                g2.drawString("> " + options[i] + " <", screenWidth / 2 - 120, 240 + (i * 60));
            } else {
                g2.setColor(Color.WHITE);
                g2.drawString(options[i], screenWidth / 2 - 90, 240 + (i * 60));
            }
        }
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 20));
        g2.setColor(Color.red);
        g2.drawString("Use W/S to Navigate | Press ENTER to Select | press Esc to exit", screenWidth / 2 - 270, screenHeight - 50);
    }

    private void drawMapSelection(Graphics2D g2) {
        g2.setColor(new Color(20, 20, 30));
        g2.fillRect(0, 0, screenWidth, screenHeight);
        g2.setColor(Color.YELLOW);
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 48));
        g2.drawString("SELECT MAP", screenWidth / 2 - 150, 150);
        g2.setColor(Color.CYAN);
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 36));
        g2.drawString("<   " + mapList[currentMapIndex] + "   >", screenWidth / 2 - 200, screenHeight / 2);
        g2.setColor(Color.WHITE);
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 20));
        g2.drawString((currentMapIndex + 1) + " / " + mapList.length, screenWidth / 2 - 30, screenHeight / 2 + 50);
        g2.setColor(Color.RED);
        g2.drawString("Use A/D to Choose | ENTER to Play | ESC to Return", screenWidth / 2 - 230, screenHeight - 80);
    }

    private void drawTutorial(Graphics2D g2) {
        g2.setColor(new Color(30, 40, 40));
        g2.fillRect(0, 0, screenWidth, screenHeight);
        g2.setColor(Color.WHITE);
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 36));
        g2.drawString("TUTORIAL", 50, 80);
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 20));
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("- Press W, A, S, D to Move the Player.", 70, 160);
        g2.drawString("- Press SPACE to Place a Bomb.", 70, 210);
        g2.drawString("- Avoid Flame and Enemies to survive.", 70, 260);
        g2.drawString("- press P to pause the game.", 70, 310);
        drawBackButtonHint(g2);
    }

    private void drawAboutUs(Graphics2D g2) {
        g2.setColor(new Color(40, 30, 40));
        g2.fillRect(0, 0, screenWidth, screenHeight);
        g2.setColor(Color.WHITE);
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 36));
        g2.drawString("ABOUT US", 50, 80);
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

    private void drawLeaderboard(Graphics2D g2) {
        g2.setColor(new Color(20, 30, 20));
        g2.fillRect(0, 0, screenWidth, screenHeight);
        g2.setColor(Color.blue);
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 36));
        g2.drawString("TOP LEADERS", 50, 80);
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 20));
        g2.setColor(Color.WHITE);

        String[] lines = scoreBoard.getLeaderboard().split("\n");
        int y = 130;
        int count = 0;
        for (String line : lines) {
            if (line != null && !line.trim().isEmpty()) {
                g2.drawString(line, 80, y);
                y += 35;
                count++;
            }
            if (count >= 10) {
                break;
            }
        }
        drawBackButtonHint(g2);
    }

    private void drawBackButtonHint(Graphics2D g2) {
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.ITALIC, 16));
        g2.setColor(Color.ORANGE);
        g2.drawString("<- Press ESC to Return Menu", 40, screenHeight - 50);
    }
}
