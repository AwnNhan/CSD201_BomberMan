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

    public String[] mapList = {"Map 1 (Default)", "Map 2 (Smart Enemy)", "Map 3 (Boss)", "Map 4"};
    public int currentMapIndex = 0;

    MapManager mapM;
    GraphConverter graphConverter = new GraphConverter();
    public CollisionChecker cChecker;
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
        cChecker = new CollisionChecker(mapM);
        graphConverter.updateGraph(mapM.getMapMatrix());

        objectList = new CustomLinkedList();
        bombManager = new BombManager(this);

        assetManager.loadImage("PLAYER", "/sprites/player.png");
        assetManager.loadImage("ENEMY", "/sprites/enemy.png");
        assetManager.loadImage("PLAYER_UP", "/sprites/player_up.png");
        assetManager.loadImage("PLAYER_DOWN", "/sprites/player_down.png");
        assetManager.loadImage("ENEMY_UP", "/sprites/enemy_up.png");
        assetManager.loadImage("ENEMY_DOWN", "/sprites/enemy_down.png");
        assetManager.loadImage("BOMB_COOL", "/sprites/atomic_bomb.png");
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

        LevelConfig currentConfig = config.LevelManager.getLevel(currentMapIndex);
        mapM.loadMap(currentConfig.getMapFilePath());

        cChecker = new CollisionChecker(mapM);
        graphConverter.updateGraph(mapM.getMapMatrix());

        objectList = new CustomLinkedList();
        bombManager.reset();
        doorSpawned = false;

        if (!isVictory) {
            playerLives = 2;
            score = 0;
        }
        isGameOver = false;
        isVictory = false;

        player = new Player(tileSize, tileSize, keyH, cChecker);
        objectList.addLast(player);

        // --- TÍCH HỢP: SINH QUÁI VẬT NGẪU NHIÊN VÀ THEO LEVEL ---
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
            } 
            else if (currentLevel == 2) {
                objectList.addLast(new SmartEnemy(startX, startY, currentConfig.getEnemySpeed()));
            } 
            else { 
                objectList.addLast(new Boss(startX, startY, currentConfig.getEnemySpeed()));
                break; // Màn Boss thì chỉ sinh 1 Boss duy nhất
            }
            addedEnemies++;
        }

        gameState = GameState.PLAYING;
    }

    public void update() {
        if (gameState == GameState.MENU) {
            if (keyH.upPressed) {
                menuOption--;
                if (menuOption < 0) menuOption = 4;
                keyH.upPressed = false;
            }
            if (keyH.downPressed) {
                menuOption++;
                if (menuOption > 4) menuOption = 0;
                keyH.downPressed = false;
            }
            if (keyH.enterPressed) {
                if (menuOption == 0) gameState = GameState.MAP_SELECTION;
                else if (menuOption == 1) gameState = GameState.TUTORIAL;
                else if (menuOption == 2) gameState = GameState.ABOUT_US;
                else if (menuOption == 3) gameState = GameState.LEADERBOARD;
                else if (menuOption == 4) System.exit(0);
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
                if (currentMapIndex < 0) currentMapIndex = mapList.length - 1;
                keyH.leftPressed = false;
            }
            if (keyH.rightPressed) {
                currentMapIndex++;
                if (currentMapIndex > config.LevelManager.getUnlockedLevelIndex()) {
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
                    if (currentMapIndex >= mapList.length) currentMapIndex = 0;
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
                else if (obj.getId() == IdObject.DOOR) {
                    // Logic Cửa: Đã spawn cửa VÀ nhân vật chạm vào cửa -> MỚI THẮNG
                    if (doorSpawned && cChecker.checkEntity(player.getHitbox(), obj.getHitbox())) {
                        isVictory = true;
                        config.LevelManager.unlockNextLevel(currentMapIndex); 
                        if (currentMapIndex == mapList.length - 1) {
                            scoreBoard.insertScore(playerName, score);
                        }
                    }
                } 
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
                                        }
                                    }
                                } else {
                                    objectList.removeNode(inner); 
                                    score += 100;
                                }
                            }
                            inner = inner.next;
                        }
                    }
                }
                current = nextNode; 
            }

            // --- ĐÃ SỬA LỖI Ở ĐÂY ---
            // Hiện cửa khi diệt hết quái (enemyCount == 0)
            // LƯU Ý: Tuyệt đối KHÔNG gán isVictory = true ở chỗ này nữa. 
            // isVictory = true CHỈ được gọi khi chạm vào cửa (logic ở dòng 313).
            if (enemyCount == 0 && !isGameOver) {
                if (!doorSpawned) {
                    LevelConfig currentConfig = config.LevelManager.getLevel(currentMapIndex);
                    double doorX = currentConfig.getDoorCol() * tileSize;
                    double doorY = currentConfig.getDoorRow() * tileSize;

                    ExitDoor door = new ExitDoor(doorX, doorY);
                    objectList.addLast(door);
                    doorSpawned = true;
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

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (gameState == GameState.MENU) {
            uiManager.drawMenu(g2, menuOption, screenWidth, screenHeight);
        } else if (gameState == GameState.MAP_SELECTION) {
            uiManager.drawMapSelection(g2, mapList, currentMapIndex, screenWidth, screenHeight);
        } else if (gameState == GameState.TUTORIAL) {
            uiManager.drawTutorial(g2, screenWidth, screenHeight);
        } else if (gameState == GameState.ABOUT_US) {
            uiManager.drawAboutUs(g2, screenWidth, screenHeight);
        } else if (gameState == GameState.LEADERBOARD) {
            uiManager.drawLeaderboard(g2, scoreBoard.getLeaderboard(), screenWidth, screenHeight);
        } else {
            // ==========================================
            // LOGIC CAMERA CỦA BẠN EM
            // ==========================================
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
            // ==========================================

            if (gameState == GameState.PLAYING || gameState == GameState.PAUSE || isGameOver || isVictory) {
                uiManager.drawHUD(g2, playerLives, score, screenWidth);
            }

            if (gameState == GameState.PAUSE && !isGameOver) {
                uiManager.drawPauseScreen(g2, screenWidth, screenHeight);
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
                        if (assetManager.getSprite("PLAYER") != null) g2.drawImage(assetManager.getSprite("PLAYER"), (int) player.getX(), (int) player.getY(), tileSize, tileSize, null);
                        else obj.render(g2);
                    }
                } else {
                    obj.render(g2);
                }
            } else if (obj.getId() == IdObject.ENEMY) {
                String direction = "DOWN"; 
                if (obj instanceof Enemy) direction = ((Enemy) obj).getDirection();
                else if (obj instanceof SmartEnemy) direction = ((SmartEnemy) obj).getDirection();
                else if (obj instanceof Boss) direction = ((Boss) obj).getDirection();

                if (!(obj instanceof Boss)) {
                    if ("UP".equalsIgnoreCase(direction)) {
                        if (assetManager.getSprite("ENEMY_UP") != null) g2.drawImage(assetManager.getSprite("ENEMY_UP"), (int) obj.getX(), (int) obj.getY(), tileSize, tileSize, null);
                        else if (assetManager.getSprite("ENEMY") != null) g2.drawImage(assetManager.getSprite("ENEMY"), (int) obj.getX(), (int) obj.getY(), tileSize, tileSize, null);
                        else obj.render(g2);
                    } else if ("DOWN".equalsIgnoreCase(direction)) {
                        if (assetManager.getSprite("ENEMY_DOWN") != null) g2.drawImage(assetManager.getSprite("ENEMY_DOWN"), (int) obj.getX(), (int) obj.getY(), tileSize, tileSize, null);
                        else if (assetManager.getSprite("ENEMY") != null) g2.drawImage(assetManager.getSprite("ENEMY"), (int) obj.getX(), (int) obj.getY(), tileSize, tileSize, null);
                        else obj.render(g2);
                    } else if ("LEFT".equalsIgnoreCase(direction)) {
                        g2.drawImage(assetManager.getSprite("ENEMY"), (int) obj.getX() + tileSize, (int) obj.getY(), -tileSize, tileSize, null);
                    } else {
                        if (assetManager.getSprite("ENEMY") != null) g2.drawImage(assetManager.getSprite("ENEMY"), (int) obj.getX(), (int) obj.getY(), tileSize, tileSize, null);
                        else obj.render(g2);
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
                    if (timeLeft < 1000) shouldDraw = (currentTime / 100) % 2 == 0;
                    else if (timeLeft < 2000) shouldDraw = (currentTime / 200) % 2 == 0;
                }

                if (shouldDraw) {
                    if (!b.isBossBomb() && assetManager.getSprite("BOMB_COOL") != null) {
                        int bombHeight = (int) (tileSize * 1.2);
                        g2.drawImage(assetManager.getSprite("BOMB_COOL"), (int) obj.getX(), (int) obj.getY() - (bombHeight - tileSize) + 6, tileSize, bombHeight, null);
                    } else {
                        obj.render(g2); 
                    }
                }
            } else if (obj.getId() == IdObject.FLAME || obj.getId() == IdObject.DOOR) {
                obj.render(g2);
            }
            current = current.next;
        }
    }
}