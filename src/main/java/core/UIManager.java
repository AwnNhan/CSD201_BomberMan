package core;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;

public class UIManager {

    private final Font titleFont = new Font("Arial", Font.BOLD, 42);
    private final Font menuFont = new Font("Arial", Font.BOLD, 22);
    private final Font subTitleFont = new Font("Arial", Font.BOLD, 36);
    private final Font regularFont = new Font("Arial", Font.PLAIN, 20);
    private final Font hudFont = new Font("Arial", Font.BOLD, 24);
    private final Font italicFont = new Font("Arial", Font.ITALIC, 16);
    private final Font largeFont = new Font("Arial", Font.BOLD, 50);
    private final Font mediumFont = new Font("Arial", Font.BOLD, 30);

    private int tutorialPage = 0;
    private final int maxTutorialPage = 3;

    private Image tutorialGif1;
    private Image tutorialGif2;
    private Image tutorialGif3;
    private Image tutorialGif4;

    public UIManager() {
        try {
            tutorialGif1 = new javax.swing.ImageIcon(getClass().getResource("/sprites/tutorial_move.gif")).getImage();
            tutorialGif2 = new javax.swing.ImageIcon(getClass().getResource("/sprites/tutorial_bom.gif")).getImage();
            tutorialGif3 = new javax.swing.ImageIcon(getClass().getResource("/sprites/tutorial_pause.gif")).getImage();
            tutorialGif4 = new javax.swing.ImageIcon(getClass().getResource("/sprites/tutorial_door.gif")).getImage();
        } catch (Exception e) {
            System.out.println("Lỗi: Không tìm thấy đầy đủ 4 file GIF trong thư mục sprites!");
            e.printStackTrace();
        }
    }

    public void prevTutorialPage() {
        if (tutorialPage > 0) {
            tutorialPage--;
        } else {
            tutorialPage = maxTutorialPage;
        }
    }

    public void nextTutorialPage() {
        if (tutorialPage < maxTutorialPage) {
            tutorialPage++;
        } else {
            tutorialPage = 0;
        }
    }

    // =========================================================================
    // 1. VẼ MÀN HÌNH MENU CHÍNH (ĐÃ ĐỒNG BỘ DYNAMIC MENU THAM SỐ MỚI)
    // =========================================================================
    public void drawMenu(Graphics2D g2, int menuOption, int screenWidth, int screenHeight, boolean hasSavedGame, boolean isGameCompleted) {
        // --- NỀN MENU XÁM TỐI GIỐNG HÌNH GỐC CỦA BẠN ---
        g2.setColor(new Color(20, 20, 30));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        // --- TIÊU ĐỀ: BOMBERMAN CSD201 ---
        g2.setFont(titleFont);
        String titleText = "BOMBERMAN CSD201";
        int titleX = getXforCenteredText(g2, titleText, screenWidth);
        int titleY = 160;

        // Vẽ bóng xám phía sau chữ tiêu đề
        g2.setColor(new Color(60, 60, 60));
        g2.drawString(titleText, titleX + 3, titleY + 3);
        // Chữ vàng chính
        g2.setColor(Color.YELLOW);
        g2.drawString(titleText, titleX, titleY);

        // --- DANH SÁCH CÁC TÙY CHỌN MENU THEO TRẠNG THÁI ---
        String[] options;
        if (hasSavedGame) {
            // Khi đang chơi dở và Pause ra Main Menu
            options = new String[]{"CONTINUE GAME", "NEW GAME", "TUTORIAL", "ABOUT US", "LEADERBOARD", "QUIT"};
        } else if (isGameCompleted) {
            // Khi đã thắng Map 3 phá đảo Game
            options = new String[]{"SELECT MAP", "NEW GAME", "TUTORIAL", "ABOUT US", "LEADERBOARD", "QUIT"};
        } else {
            // Mặc định ban đầu
            options = new String[]{"START GAME", "TUTORIAL", "ABOUT US", "LEADERBOARD", "QUIT"};
        }

        g2.setFont(menuFont);
        int startY = 260;

        for (int i = 0; i < options.length; i++) {
            String optionText = options[i];
            int optionX = getXforCenteredText(g2, optionText, screenWidth);
            int optionY = startY + (i * 42);

            if (i == menuOption) {
                // Mục đang chọn hiển thị màu xanh cyan nhẹ và thêm hai dấu > <
                g2.setColor(Color.CYAN);
                g2.drawString("> " + optionText + " <", optionX - 25, optionY);
            } else {
                g2.setColor(Color.WHITE);
                g2.drawString(optionText, optionX, optionY);
            }
        }

        // --- DÒNG HƯỚNG DẪN MÀU ĐỎ BÊN DƯỚI CÙNG ---
        g2.setColor(Color.RED);
        g2.setFont(new Font("Arial", Font.BOLD, 15));
        String hintText = "Use W/S to Navigate | Press ENTER to Select | Press ESC to Exit";
        int hintX = getXforCenteredText(g2, hintText, screenWidth);
        g2.drawString(hintText, hintX, screenHeight - 60);
    }

    // =========================================================================
    // 2. VẼ MÀN HÌNH CHỌN MAP
    // =========================================================================
    public void drawMapSelection(Graphics2D g2, String[] mapList, int currentMapIndex, int screenWidth, int screenHeight) {
        g2.setColor(new Color(20, 20, 30));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setColor(Color.YELLOW);
        g2.setFont(titleFont);
        g2.drawString("SELECT MAP", screenWidth / 2 - 140, 150);

        g2.setColor(Color.CYAN);
        g2.setFont(subTitleFont);
        g2.drawString("<    " + mapList[currentMapIndex] + "    >", screenWidth / 2 - 200, screenHeight / 2);

        g2.setColor(Color.WHITE);
        g2.setFont(regularFont);
        g2.drawString((currentMapIndex + 1) + " / " + mapList.length, screenWidth / 2 - 30, screenHeight / 2 + 50);

        g2.setColor(Color.RED);
        g2.drawString("Use A/D to Choose | ENTER to Play | ESC to Return", screenWidth / 2 - 230, screenHeight - 80);
    }

    // =========================================================================
    // 3. VẼ MÀN HÌNH HƯỚNG DẪN (TUTORIAL)
    // =========================================================================
    public void drawTutorial(Graphics2D g2, int screenWidth, int screenHeight) {
        g2.setColor(new Color(30, 40, 40));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setColor(Color.WHITE);
        g2.setFont(subTitleFont);
        g2.drawString("TUTORIAL (" + (tutorialPage + 1) + "/" + (maxTutorialPage + 1) + ")", 50, 80);

        g2.setFont(regularFont);

        int gifWidth = 280;
        int gifHeight = 210;
        int gifX = (screenWidth - gifWidth) / 2;
        int gifY = 110;

        int textX = 120;
        int titleY = 360;
        int lineStartY = 405;
        int lineSpacing = 35;

        if (tutorialPage == 0) {
            if (tutorialGif1 != null) {
                g2.drawImage(tutorialGif1, gifX, gifY, gifWidth, gifHeight, null);
            }
            g2.setColor(Color.YELLOW);
            g2.drawString("PAGE 1: PLAYER MOVEMENT", textX, titleY);

            g2.setColor(Color.LIGHT_GRAY);
            g2.drawString("- Press W to move up.", textX, lineStartY);
            g2.drawString("- Press S to move down.", textX, lineStartY + lineSpacing);
            g2.drawString("- Press A to move left.", textX, lineStartY + 2 * lineSpacing);
            g2.drawString("- Press D to move right.", textX, lineStartY + 3 * lineSpacing);
        } else if (tutorialPage == 1) {
            if (tutorialGif2 != null) {
                g2.drawImage(tutorialGif2, gifX, gifY, gifWidth, gifHeight, null);
            }
            g2.setColor(Color.YELLOW);
            g2.drawString("PAGE 2: HOW TO USE BOMBS", textX, titleY);

            g2.setColor(Color.LIGHT_GRAY);
            g2.drawString("- Press SPACEBAR to place a bomb.", textX, lineStartY);
            g2.drawString("- Bombs will explode after 2 seconds.", textX, lineStartY + lineSpacing);
            g2.drawString("- Run away quickly from the explosion range!", textX, lineStartY + 2 * lineSpacing);
            g2.drawString("- Destroy soft bricks to clear the path.", textX, lineStartY + 3 * lineSpacing);
        } else if (tutorialPage == 2) {
            if (tutorialGif3 != null) {
                g2.drawImage(tutorialGif3, gifX, gifY, gifWidth, gifHeight, null);
            }
            g2.setColor(Color.YELLOW);
            g2.drawString("PAGE 3: PAUSE", textX, titleY);

            g2.setColor(Color.LIGHT_GRAY);
            g2.drawString("- Press P to pause game.", textX, lineStartY);
            g2.drawString("- Press P again to continue.", textX, lineStartY + lineSpacing);
            g2.drawString("- Press Esc to return menu screen.", textX, lineStartY + 2 * lineSpacing);
        } else if (tutorialPage == 3) {
            if (tutorialGif4 != null) {
                g2.drawImage(tutorialGif4, gifX, gifY, gifWidth, gifHeight, null);
            }
            g2.setColor(Color.YELLOW);
            g2.drawString("PAGE 4: NEXT LEVEL AND MECHANICS", textX, titleY);

            g2.setColor(Color.LIGHT_GRAY);
            g2.drawString("- Go to the trophy to move on to the next level.", textX, lineStartY);
            g2.drawString("- When you pass the level, you'll get an extra life.", textX, lineStartY + lineSpacing);
            g2.drawString("- Points and lives are only kept if the player clears the level.", textX, lineStartY + 2 * lineSpacing);
            g2.drawString("- If you choose map, points and life will reset to default.", textX, lineStartY + 3 * lineSpacing);
        }

        g2.setFont(italicFont);
        g2.setColor(Color.CYAN);
        String pageHint = "Use A / D keys to flip pages ->";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(pageHint, screenWidth - fm.stringWidth(pageHint) - 40, screenHeight - 50);

        drawBackButtonHint(g2, screenHeight);
    }

    // =========================================================================
    // 4. VẼ MÀN HÌNH GIỚI THIỆU (ABOUT US)
    // =========================================================================
    public void drawAboutUs(Graphics2D g2, int screenWidth, int screenHeight) {
        g2.setColor(new Color(40, 30, 40));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setColor(Color.WHITE);
        g2.setFont(subTitleFont);
        g2.drawString("ABOUT US", 50, 80);

        g2.setFont(regularFont);
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("Course: Data Structures and Algorithms (CSD201)", 70, 160);
        g2.drawString("Institution: FPT University", 70, 210);
        g2.drawString("[CE200304 - Nguyễn Trần Khả Nhân - leader]", 70, 260);
        g2.drawString("[CE201492 - Lương Trung Hiếu]", 70, 310);
        g2.drawString("[CE201621 - Nguyễn Minh Phát]", 70, 360);
        g2.drawString("[CE201183 - Đỗ Trần Thiên Phúc]", 70, 410);
        g2.drawString("[CE201665 - Lê Nguyễn Thành Tài]", 70, 460);
        g2.drawString("[CE201233 - Trương Anh Tuấn]", 70, 510);

        drawBackButtonHint(g2, screenHeight);
    }

    // =========================================================================
    // 5. VẼ BẢNG XẾP HẠNG (LEADERBOARD)
    // =========================================================================
    public void drawLeaderboard(Graphics2D g2, String leaderboardData, int screenWidth, int screenHeight) {
        g2.setColor(new Color(20, 30, 20));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setColor(Color.BLUE);
        g2.setFont(subTitleFont);
        g2.drawString("TOP LEADERS", 50, 80);

        g2.setFont(regularFont);
        g2.setColor(Color.WHITE);

        if (leaderboardData != null && !leaderboardData.isEmpty()) {
            String[] lines = leaderboardData.split("\n");
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
        } else {
            g2.drawString("No records yet. Be the first to win!", 80, 130);
        }

        drawBackButtonHint(g2, screenHeight);
    }

    // =========================================================================
    // 6. VẼ HUD TRONG GAME (ĐIỂM SỐ & MẠNG SỐNG)
    // =========================================================================
    public void drawHUD(Graphics2D g2, int playerLives, int score, int screenWidth) {
        g2.setColor(Color.WHITE);
        g2.setFont(hudFont);
        g2.drawString("Lives: " + playerLives, 20, 40);

        String scoreText = "Score: " + score;
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(scoreText, screenWidth - fm.stringWidth(scoreText) - 20, 40);
    }

    // =========================================================================
    // 7. VẼ MÀN HÌNH TẠM DỪNG (PAUSE SCREEN)
    // =========================================================================
    public void drawPauseScreen(Graphics2D g2, int screenWidth, int screenHeight, int pauseOption) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 40F));
        String text = "PAUSED";
        int x = getXforCenteredText(g2, text, screenWidth);
        int y = screenHeight / 4 + 20;

        g2.setColor(Color.GRAY);
        g2.drawString(text, x + 3, y + 3);
        g2.setColor(Color.WHITE);
        g2.drawString(text, x, y);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 22F));
        String[] options = {"CONTINUE", "NEW GAME", "MAIN MENU"};
        int startY = screenHeight / 2 - 10;

        for (int i = 0; i < options.length; i++) {
            String optionText = options[i];
            int optionX = getXforCenteredText(g2, optionText, screenWidth);
            int optionY = startY + (i * 45);

            if (i == pauseOption) {
                g2.setColor(Color.YELLOW);
                g2.drawString("> " + optionText + " <", optionX - 25, optionY);
            } else {
                g2.setColor(Color.WHITE);
                g2.drawString(optionText, optionX, optionY);
            }
        }
    }

    private int getXforCenteredText(Graphics2D g2, String text, int screenWidth) {
        int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return screenWidth / 2 - length / 2;
    }

    // =========================================================================
    // 8. VẼ MÀN HÌNH KẾT THÚC (GAME OVER / VICTORY)
    // =========================================================================
    public void drawEndGameScreen(Graphics2D g2, int screenWidth, int screenHeight, int score, boolean isVictory) {
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setColor(isVictory ? Color.YELLOW : Color.RED);
        g2.setFont(largeFont);
        g2.drawString(isVictory ? "VICTORY" : "GAME OVER", screenWidth / 2 - (isVictory ? 100 : 130), screenHeight / 2 - 40);

        g2.setColor(Color.WHITE);
        g2.setFont(mediumFont);
        g2.drawString("Score: " + score, screenWidth / 2 - 70, screenHeight / 2);

        g2.setFont(regularFont);
        g2.drawString("Press SPACE to " + (isVictory ? "Continue" : "Play Again"), screenWidth / 2 - 120, screenHeight / 2 + 40);
        g2.drawString("Press ESC to Back to Menu", screenWidth / 2 - 125, screenHeight / 2 + 70);
    }

    // =========================================================================
    // HÀM BỔ TRỢ: VẼ NÚT QUAY LẠI
    // =========================================================================
    private void drawBackButtonHint(Graphics2D g2, int screenHeight) {
        g2.setFont(italicFont);
        g2.setColor(Color.ORANGE);
        g2.drawString("<- Press ESC to Return Menu", 40, screenHeight - 50);
    }
}