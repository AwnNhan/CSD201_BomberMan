package core;

import algorithm.CustomLinkedList;
import algorithm.GraphConverter;
import algorithm.ScoreBST;
import config.LevelConfig;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import map.MapManager;
import model.Bomb;
import model.Boss;
import model.Enemy;
import model.ExitDoor;
import model.Flame;
import model.GameObject;
import model.IdObject;
import model.Player;
import model.SmartEnemy;

public class GamePanel extends JPanel implements Runnable {

    // === CẤU HÌNH MÀN HÌNH ===
    final int originalTileSize = 16;
    final int scale = 3;
    public final int tileSize = originalTileSize * scale;
    public final int maxScreenCol = 15;
    public final int maxScreenRow = 13;

    public final int screenWidth = tileSize * maxScreenCol;
    public final int screenHeight = tileSize * maxScreenRow;

    // === TRẠNG THÁI GAME ===
    public GameState gameState = GameState.MENU;
    public boolean isGameOver = false;
    public boolean isVictory = false;
    public boolean hasSavedGame = false;    // Đánh dấu có trận đấu đang chơi dở
    public boolean isGameCompleted = false; // Đánh dấu đã phá đảo xong Map 3
    public int menuOption = 0;
    public int pauseOption = 0;             // 0: CONTINUE, 1: NEW GAME, 2: MAIN MENU
    public String playerName = "Player";

    // === HỆ THỐNG QUẢN LÝ THƯ VIỆN & ĐIỂM ===
    public AssetManager assetManager = new AssetManager();
    public UIManager uiManager = new UIManager();
    public ScoreBST scoreBoard = new ScoreBST();

    Thread gameThread;
    KeyHandler keyH = new KeyHandler();

    public int score = 0;
    public int playerLives = 3;
    public int checkpointScore = 0;
    public int checkpointLives = 3;
    private long invincibleUntil = 0;

    public String[] mapList = {"Map 1 (Default)", "Map 2 (Smart Enemy)", "Map 3 (Boss)"};
    public int currentMapIndex = 0;

    // === HỆ THỐNG QUẢN LÝ CORE ===
    public MapManager mapM;
    public GraphConverter graphConverter = new GraphConverter();
    public CollisionChecker cChecker;
    public SoundManager soundManager = new SoundManager();
    public CustomLinkedList objectList;
    public Player player;
    public BombManager bombManager;

    public boolean doorSpawned = false;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.addKeyListener(keyH);

        mapM = new MapManager();
        cChecker = new CollisionChecker(this);
        objectList = new CustomLinkedList();

        // Khởi tạo BombManager
        bombManager = new BombManager(this);

        // Nạp tài nguyên hình ảnh
        assetManager.loadImage("PLAYER", "/sprites/player.png");
        assetManager.loadImage("ENEMY", "/sprites/enemy.png");
        assetManager.loadImage("PLAYER_UP", "/sprites/player_up.png");
        assetManager.loadImage("PLAYER_DOWN", "/sprites/player_down.png");
        assetManager.loadImage("ENEMY_UP", "/sprites/enemy_up.png");
        assetManager.loadImage("ENEMY_DOWN", "/sprites/enemy_down.png");
        assetManager.loadImage("BOMB_COOL", "/sprites/atomic_bomb.png");
        assetManager.loadImage("DOOR", "/sprites/door.png");
        assetManager.loadImage("BOSS_DOWN", "/sprites/boss_down.png");
        assetManager.loadImage("BOSS_LEFT", "/sprites/boss_left.png");
        assetManager.loadImage("BOSS_RIGHT", "/sprites/boss_right.png");
        assetManager.loadImage("BOSS_UP", "/sprites/boss_up.png");
        assetManager.loadImage("BOSS_BOM", "/sprites/boss_bom.png");

        soundManager.playBGM(10);
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

    // Tạo game mới bắt đầu từ Map 1: Nhập tên -> Khởi tạo Map 1 -> Chơi luôn
    private void startNewGameFromBeginning() {
        hasSavedGame = false;
        String inputName = JOptionPane.showInputDialog(this, "Nhập tên người chơi:");
        playerName = (inputName != null && !inputName.trim().isEmpty()) ? inputName.trim() : "Player";

        currentMapIndex = 0; // Mặc định bắt đầu từ Map 1
        resetGame();
    }

    private void resetGame() {
        mapM = new MapManager();

        LevelConfig currentConfig = config.LevelManager.getLevel(currentMapIndex);
        mapM.loadMap(currentConfig.getMapFilePath());

        soundManager.changeTheme(currentMapIndex);

        cChecker = new CollisionChecker(this);
        graphConverter.updateGraph(mapM.getMapMatrix());

        objectList = new CustomLinkedList();
        bombManager.reset();
        doorSpawned = false;

        // --- LOGIC CHECKPOINT ĐIỂM SỐ & MẠNG SỐNG ---
        if (isGameOver) {
            playerLives = checkpointLives;
            score = checkpointScore;
        } else if (!isVictory) {
            playerLives = 3;
            score = 0;
            checkpointLives = 3;
            checkpointScore = 0;
        }

        isGameOver = false;
        isVictory = false;

        player = new Player(tileSize, tileSize, keyH, cChecker);
        objectList.addLast(player);

        int mapCols = mapM.getMaxCol();
        int mapRows = mapM.getMaxRow();
        int[][] matrix = mapM.getMapMatrix();
        List<int[]> emptyTiles = new ArrayList<>();

        for (int r = 0; r < mapRows; r++) {
            for (int c = 0; c < mapCols; c++) {
                if (matrix[r][c] == 0 && (r > 3 || c > 3)) {
                    emptyTiles.add(new int[]{r, c});
                }
            }
        }

        Random rand = new Random();
        int addedEnemies = 0;
        int enemyCount = currentConfig.getEnemyCount();
        int currentLevel = currentConfig.getLevelNumber();

        while (addedEnemies < enemyCount && !emptyTiles.isEmpty()) {
            int randomIndex = rand.nextInt(emptyTiles.size());
            int[] pos = emptyTiles.remove(randomIndex);

            int startX = pos[1] * tileSize;
            int startY = pos[0] * tileSize;

            if (currentLevel == 1) {
                objectList.addLast(new Enemy(startX, startY, currentConfig.getEnemySpeed()));
            } else if (currentLevel == 2) {
                objectList.addLast(new SmartEnemy(startX, startY, currentConfig.getEnemySpeed()));
            } else {
                objectList.addLast(new Boss(startX, startY, currentConfig.getEnemySpeed(), this.assetManager));
                break;
            }
            addedEnemies++;
        }

        gameState = GameState.PLAYING;
    }

    // Hàm lưu trận đấu dở dang và thoát về Menu chính
    private void returnToMainMenu() {
        scoreBoard.insertScore(playerName, score);
        isVictory = false;
        soundManager.stopBGM();

        hasSavedGame = true; // Đánh dấu có game lưu dở
        gameState = GameState.MENU;
        menuOption = 0;

        soundManager.playSFX(3);
        soundManager.playBGM(10);
    }

    public void update() {
        // --- LOGIC MENU CHÍNH ---
        if (gameState == GameState.MENU) {
            int maxMenuOption;
            if (hasSavedGame) {
                maxMenuOption = 5; // CONTINUE GAME, NEW GAME, TUTORIAL, ABOUT US, LEADERBOARD, QUIT
            } else if (isGameCompleted) {
                maxMenuOption = 5; // SELECT MAP, NEW GAME, TUTORIAL, ABOUT US, LEADERBOARD, QUIT
            } else {
                maxMenuOption = 4; // START GAME, TUTORIAL, ABOUT US, LEADERBOARD, QUIT
            }

            if (keyH.upPressed) {
                soundManager.playSFX(3);
                menuOption--;
                if (menuOption < 0) menuOption = maxMenuOption;
                keyH.upPressed = false;
            }
            if (keyH.downPressed) {
                soundManager.playSFX(3);
                menuOption++;
                if (menuOption > maxMenuOption) menuOption = 0;
                keyH.downPressed = false;
            }

            if (keyH.enterPressed) {
                soundManager.playSFX(3);

                if (hasSavedGame) {
                    // Trạng thái 1: Đang có game lưu dở
                    if (menuOption == 0) {
                        // CONTINUE GAME -> Quay lại trận cũ
                        gameState = GameState.PLAYING;
                        soundManager.changeTheme(currentMapIndex);
                    } else if (menuOption == 1) {
                        // NEW GAME -> Bỏ trận cũ, hỏi tên & vào thẳng Map 1
                        startNewGameFromBeginning();
                    } else if (menuOption == 2) gameState = GameState.TUTORIAL;
                    else if (menuOption == 3) gameState = GameState.ABOUT_US;
                    else if (menuOption == 4) gameState = GameState.LEADERBOARD;
                    else if (menuOption == 5) System.exit(0);

                } else if (isGameCompleted) {
                    // Trạng thái 2: Đã hoàn thành xong Map 3
                    if (menuOption == 0) {
                        // SELECT MAP -> Vào màn chọn tự do 3 Map
                        gameState = GameState.MAP_SELECTION;
                    } else if (menuOption == 1) {
                        // NEW GAME -> Tạo tài khoản mới & vào thẳng Map 1
                        startNewGameFromBeginning();
                    } else if (menuOption == 2) gameState = GameState.TUTORIAL;
                    else if (menuOption == 3) gameState = GameState.ABOUT_US;
                    else if (menuOption == 4) gameState = GameState.LEADERBOARD;
                    else if (menuOption == 5) System.exit(0);

                } else {
                    // Trạng thái 3: Mới mở game lần đầu
                    if (menuOption == 0) {
                        // START GAME -> Nhập tên & vào thẳng Map 1 luôn
                        startNewGameFromBeginning();
                    } else if (menuOption == 1) gameState = GameState.TUTORIAL;
                    else if (menuOption == 2) gameState = GameState.ABOUT_US;
                    else if (menuOption == 3) gameState = GameState.LEADERBOARD;
                    else if (menuOption == 4) System.exit(0);
                }

                keyH.enterPressed = false;
            }
            return;
        }

        if (gameState == GameState.TUTORIAL) {
            if (keyH.leftPressed) {
                soundManager.playSFX(3);
                uiManager.prevTutorialPage();
                keyH.leftPressed = false;
            }
            if (keyH.rightPressed) {
                soundManager.playSFX(3);
                uiManager.nextTutorialPage();
                keyH.rightPressed = false;
            }
            if (keyH.escapePressed || keyH.enterPressed) {
                soundManager.playSFX(3);
                gameState = GameState.MENU;
                keyH.escapePressed = false;
                keyH.enterPressed = false;
            }
            return;
        }

        if (gameState == GameState.ABOUT_US || gameState == GameState.LEADERBOARD) {
            if (keyH.escapePressed) {
                soundManager.playSFX(3);
                gameState = GameState.MENU;
                keyH.escapePressed = false;
            }
            return;
        }

        // --- MÀN HÌNH CHỌN MAP (Chỉ xuất hiện khi bấm SELECT MAP sau khi đã phá đảo Map 3) ---
        if (gameState == GameState.MAP_SELECTION) {
            if (keyH.rightPressed) {
                soundManager.playSFX(3);
                currentMapIndex++;
                if (currentMapIndex > config.LevelManager.getUnlockedLevelIndex()) {
                    currentMapIndex = 0;
                }
                keyH.rightPressed = false;
            }

            if (keyH.leftPressed) {
                soundManager.playSFX(3);
                currentMapIndex--;
                if (currentMapIndex < 0) {
                    currentMapIndex = config.LevelManager.getUnlockedLevelIndex();
                }
                keyH.leftPressed = false;
            }

            if (keyH.enterPressed) {
                soundManager.playSFX(3);
                resetGame(); // Nạp map đã chọn và bắt đầu chơi
                keyH.enterPressed = false;
            }
            if (keyH.escapePressed) {
                soundManager.playSFX(3);
                gameState = GameState.MENU;
                keyH.escapePressed = false;
            }
            return;
        }

        if (isGameOver || isVictory) {
            hasSavedGame = false;

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
                returnToMainMenu();
                keyH.escapePressed = false;
            }
            return;
        }

        // --- LOGIC TRONG TRẬN ĐẤU CHÍNH THỨC ---
        if (gameState == GameState.PLAYING) {
            bombManager.handlePlacingBomb(player, keyH);
            bombManager.updateBombs();

            int[][] mapWithBombs = bombManager.generateMapWithBombs();

            CustomLinkedList.Node current = objectList.head;
            int enemyCount = 0;
            boolean invincible = System.currentTimeMillis() < invincibleUntil;

            while (current != null) {
                CustomLinkedList.Node nextNode = current.next;
                GameObject obj = current.data;

                obj.update();

                if (obj.getId() == IdObject.ENEMY) {
                    enemyCount++;
                    int playerGridR = (int) (player.getY() / tileSize);
                    int playerGridC = (int) (player.getX() / tileSize);

                    if (obj instanceof Enemy) {
                        ((Enemy) obj).setRealData(mapWithBombs);
                    } else if (obj instanceof SmartEnemy) {
                        ((SmartEnemy) obj).setRealData(mapWithBombs, playerGridR, playerGridC);
                    } else if (obj instanceof Boss) {
                        Boss boss = (Boss) obj;
                        boss.setRealData(mapWithBombs, playerGridR, playerGridC);
                        boss.castSkill(bombManager.bombQueue, objectList, mapM.getMaxCol(), mapM.getMaxRow());
                    }

                    if (!invincible && cChecker.checkEntity(player.getHitbox(), obj.getHitbox())) {
                        killPlayer();
                    }
                } 
                // Xử lý Cửa qua màn
                else if (obj.getId() == IdObject.DOOR) {
                    if (doorSpawned && cChecker.checkEntity(player.getHitbox(), obj.getHitbox())) {
                        if (!isVictory) {
                            isVictory = true;
                            soundManager.stopBGM();
                            soundManager.playSFX(7); // Tiếng Victory

                            playerLives++;
                            checkpointLives = playerLives;
                            checkpointScore = score;

                            config.LevelManager.unlockNextLevel(currentMapIndex);

                            // NẾU THẮNG MAP 3 (MAP CUỐI) -> VỀ THẲNG MENU CHÍNH VỚI DÒNG "SELECT MAP"
                            if (currentMapIndex == mapList.length - 1) {
                                isGameCompleted = true;
                                hasSavedGame = false;
                                scoreBoard.insertScore(playerName, score);

                                gameState = GameState.MENU;
                                menuOption = 0; // Đặt con trỏ ở SELECT MAP
                                soundManager.playBGM(10); // Bật nhạc Menu
                            }
                        }
                    }
                } 
                // Xử lý Lửa nổ (Flame)
                else if (obj.getId() == IdObject.FLAME) {
                    Flame f = (Flame) obj;
                    if (f.isExpired()) {
                        objectList.removeNode(current);
                    } else {
                        if (!invincible && cChecker.checkEntity(player.getHitbox(), f.getHitbox())) {
                            killPlayer();
                        }

                        CustomLinkedList.Node inner = objectList.head;
                        while (inner != null) {
                            if (inner.data.getId() == IdObject.ENEMY && cChecker.checkEntity(f.getHitbox(), inner.data.getHitbox())) {
                                if (inner.data instanceof Boss) {
                                    Boss boss = (Boss) inner.data;
                                    if (!f.isBossFlame()) {
                                        boss.takeDamage();
                                        if (boss.getHp() <= 0) {
                                            objectList.removeNode(inner);
                                            score += 500;
                                            soundManager.playSFX(4);
                                        }
                                    }
                                } else {
                                    objectList.removeNode(inner);
                                    score += 100;
                                    soundManager.playSFX(4);
                                }
                            }
                            inner = inner.next;
                        }
                    }
                }
                current = nextNode;
            }

            if (enemyCount == 0 && !isGameOver) {
                if (!doorSpawned) {
                    LevelConfig currentConfig = config.LevelManager.getLevel(currentMapIndex);
                    double doorX = currentConfig.getDoorCol() * tileSize;
                    double doorY = currentConfig.getDoorRow() * tileSize;

                    ExitDoor door = new ExitDoor(doorX, doorY, this.assetManager);
                    objectList.addLast(door);
                    doorSpawned = true;
                }
            }

            if (keyH.pausePressed) {
                gameState = GameState.PAUSE;
                pauseOption = 0;
                keyH.pausePressed = false;
            }

        } else if (gameState == GameState.PAUSE) {
            // === LOGIC MENU PAUSE ===
            if (keyH.upPressed) {
                soundManager.playSFX(3);
                pauseOption--;
                if (pauseOption < 0) pauseOption = 2;
                keyH.upPressed = false;
            }
            if (keyH.downPressed) {
                soundManager.playSFX(3);
                pauseOption++;
                if (pauseOption > 2) pauseOption = 0;
                keyH.downPressed = false;
            }

            if (keyH.enterPressed) {
                soundManager.playSFX(3);
                if (pauseOption == 0) {
                    // 0: CONTINUE -> Chơi tiếp
                    gameState = GameState.PLAYING;
                } else if (pauseOption == 1) {
                    // 1: NEW GAME -> Hỏi tên mới & Bắt đầu lại từ Map 1
                    startNewGameFromBeginning();
                } else if (pauseOption == 2) {
                    // 2: MAIN MENU -> Thoát ra Menu chính & lưu trận cũ
                    returnToMainMenu();
                }
                keyH.enterPressed = false;
            }

            if (keyH.pausePressed) {
                gameState = GameState.PLAYING;
                keyH.pausePressed = false;
            }

            if (keyH.escapePressed) {
                returnToMainMenu();
                keyH.escapePressed = false;
            }
        }
    }

    private void killPlayer() {
        playerLives--;
        if (playerLives <= 0) {
            isGameOver = true;
            soundManager.stopBGM();
            soundManager.playSFX(5);
            scoreBoard.insertScore(playerName, score);
        } else {
            player.setX(tileSize);
            player.setY(tileSize);
            invincibleUntil = System.currentTimeMillis() + 2000;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (gameState == GameState.MENU) {
            uiManager.drawMenu(g2, menuOption, screenWidth, screenHeight, hasSavedGame, isGameCompleted);
        } else if (gameState == GameState.MAP_SELECTION) {
            uiManager.drawMapSelection(g2, mapList, currentMapIndex, screenWidth, screenHeight);
        } else if (gameState == GameState.TUTORIAL) {
            uiManager.drawTutorial(g2, screenWidth, screenHeight);
        } else if (gameState == GameState.ABOUT_US) {
            uiManager.drawAboutUs(g2, screenWidth, screenHeight);
        } else if (gameState == GameState.LEADERBOARD) {
            uiManager.drawLeaderboard(g2, scoreBoard.getLeaderboard(), screenWidth, screenHeight);
        } else {
            int cameraX = 0;
            int cameraY = 0;

            if (player != null) {
                cameraX = (int) player.getX() - (screenWidth / 2) + (tileSize / 2);
                cameraY = (int) player.getY() - (screenHeight / 2) + (tileSize / 2);

                int mapPixelWidth = mapM.getMaxCol() * tileSize;
                int mapPixelHeight = mapM.getMaxRow() * tileSize;

                if (cameraX < 0) cameraX = 0;
                if (cameraY < 0) cameraY = 0;
                if (cameraX > mapPixelWidth - screenWidth) cameraX = mapPixelWidth - screenWidth;
                if (cameraY > mapPixelHeight - screenHeight) cameraY = mapPixelHeight - screenHeight;
            }

            g2.translate(-cameraX, -cameraY);

            mapM.render(g2);
            if (gameState == GameState.PLAYING || gameState == GameState.PAUSE || isGameOver || isVictory) {
                renderGameObjects(g2);
            }

            g2.translate(cameraX, cameraY);

            if (gameState == GameState.PLAYING || gameState == GameState.PAUSE || isGameOver || isVictory) {
                uiManager.drawHUD(g2, playerLives, score, screenWidth);
            }

            if (gameState == GameState.PAUSE && !isGameOver) {
                uiManager.drawPauseScreen(g2, screenWidth, screenHeight, pauseOption);
            } else if (isGameOver || isVictory) {
                uiManager.drawEndGameScreen(g2, screenWidth, screenHeight, score, isVictory);
            }
        }
        g2.dispose();
    }

    private void renderGameObjects(Graphics2D g2) {
        CustomLinkedList.Node current = objectList.head;
        while (current != null) {
            GameObject obj = current.data;

            if (obj.getId() == IdObject.PLAYER) {
                if (System.currentTimeMillis() > invincibleUntil || System.currentTimeMillis() / 100 % 2 == 0) {
                    String dir = player.getDirection();

                    if ("UP".equalsIgnoreCase(dir)) {
                        g2.drawImage(assetManager.getSprite("PLAYER_UP"), (int) player.getX(), (int) player.getY(), tileSize, tileSize, null);
                    } else if ("DOWN".equalsIgnoreCase(dir)) {
                        g2.drawImage(assetManager.getSprite("PLAYER_DOWN"), (int) player.getX(), (int) player.getY(), tileSize, tileSize, null);
                    } else if ("LEFT".equalsIgnoreCase(dir)) {
                        g2.drawImage(assetManager.getSprite("PLAYER"), (int) player.getX() + tileSize, (int) player.getY(), -tileSize, tileSize, null);
                    } else {
                        if (assetManager.getSprite("PLAYER") != null) {
                            g2.drawImage(assetManager.getSprite("PLAYER"), (int) player.getX(), (int) player.getY(), tileSize, tileSize, null);
                        } else {
                            obj.render(g2);
                        }
                    }
                }
            } else if (obj.getId() == IdObject.ENEMY) {
                String direction = "DOWN";
                if (obj instanceof Enemy) {
                    direction = ((Enemy) obj).getDirection();
                } else if (obj instanceof SmartEnemy) {
                    direction = ((SmartEnemy) obj).getDirection();
                } else if (obj instanceof Boss) {
                    direction = ((Boss) obj).getDirection();
                }

                if (!(obj instanceof Boss)) {
                    if ("UP".equalsIgnoreCase(direction)) {
                        if (assetManager.getSprite("ENEMY_UP") != null) {
                            g2.drawImage(assetManager.getSprite("ENEMY_UP"), (int) obj.getX(), (int) obj.getY(), tileSize, tileSize, null);
                        } else if (assetManager.getSprite("ENEMY") != null) {
                            g2.drawImage(assetManager.getSprite("ENEMY"), (int) obj.getX(), (int) obj.getY(), tileSize, tileSize, null);
                        } else {
                            obj.render(g2);
                        }
                    } else if ("DOWN".equalsIgnoreCase(direction)) {
                        if (assetManager.getSprite("ENEMY_DOWN") != null) {
                            g2.drawImage(assetManager.getSprite("ENEMY_DOWN"), (int) obj.getX(), (int) obj.getY(), tileSize, tileSize, null);
                        } else if (assetManager.getSprite("ENEMY") != null) {
                            g2.drawImage(assetManager.getSprite("ENEMY"), (int) obj.getX(), (int) obj.getY(), tileSize, tileSize, null);
                        } else {
                            obj.render(g2);
                        }
                    } else if ("LEFT".equalsIgnoreCase(direction)) {
                        g2.drawImage(assetManager.getSprite("ENEMY"), (int) obj.getX() + tileSize, (int) obj.getY(), -tileSize, tileSize, null);
                    } else {
                        if (assetManager.getSprite("ENEMY") != null) {
                            g2.drawImage(assetManager.getSprite("ENEMY"), (int) obj.getX(), (int) obj.getY(), tileSize, tileSize, null);
                        } else {
                            obj.render(g2);
                        }
                    }
                } else {
                    obj.render(g2);
                }
            } else if (obj.getId() == IdObject.BOMB) {
                Bomb b = (Bomb) obj;
                long currentTime = System.currentTimeMillis();
                long timeLeft = b.getTimeToExplode() - currentTime;

                boolean shouldDraw = true;
                if (timeLeft > 0) {
                    if (timeLeft < 1000) {
                        shouldDraw = (currentTime / 100) % 2 == 0;
                    } else if (timeLeft < 2000) {
                        shouldDraw = (currentTime / 200) % 2 == 0;
                    }
                }

                if (shouldDraw) {
                    if (b.isBossBomb()) {
                        if (assetManager.getSprite("BOSS_BOM") != null) {
                            g2.drawImage(assetManager.getSprite("BOSS_BOM"), (int) obj.getX(), (int) obj.getY(), tileSize, tileSize, null);
                        } else {
                            obj.render(g2);
                        }
                    } else {
                        if (assetManager.getSprite("BOMB_COOL") != null) {
                            int bombHeight = (int) (tileSize * 1.2);
                            g2.drawImage(assetManager.getSprite("BOMB_COOL"), (int) obj.getX(), (int) obj.getY() - (bombHeight - tileSize) + 6, tileSize, bombHeight, null);
                        } else {
                            obj.render(g2);
                        }
                    }
                }
            } else if (obj.getId() == IdObject.FLAME || obj.getId() == IdObject.DOOR) {
                obj.render(g2);
            }
            current = current.next;
        }
    }
}