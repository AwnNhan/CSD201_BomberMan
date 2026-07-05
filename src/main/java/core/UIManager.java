/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 *
 * @author Admin
 */
public class UIManager {

    // Khai báo sẵn các Font để tái sử dụng, tránh tạo mới (new Font) 60 lần/giây gây tốn RAM
    private final Font titleFont = new Font("Arial", Font.BOLD, 48);
    private final Font menuFont = new Font("Arial", Font.BOLD, 26);
    private final Font subTitleFont = new Font("Arial", Font.BOLD, 36);
    private final Font regularFont = new Font("Arial", Font.PLAIN, 20);
    private final Font hudFont = new Font("Arial", Font.BOLD, 24);
    private final Font italicFont = new Font("Arial", Font.ITALIC, 16);
    private final Font largeFont = new Font("Arial", Font.BOLD, 50);
    private final Font mediumFont = new Font("Arial", Font.BOLD, 30);

    public UIManager() {
        // Constructor trống, sẵn sàng cho việc quản lý UI
    }

    // =========================================================================
    // 1. VẼ MÀN HÌNH MENU CHÍNH
    // =========================================================================
    public void drawMenu(Graphics2D g2, int menuOption, int screenWidth, int screenHeight) {
        g2.setColor(new Color(20, 20, 30));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setFont(titleFont);
        g2.setColor(Color.YELLOW);
        g2.drawString("BOMBERMAN CSD201", screenWidth / 2 - 250, 120);

        String[] options = {"START GAME", "TUTORIAL", "ABOUT US", "LEADERBOARD", "QUIT"};
        g2.setFont(menuFont);
        for (int i = 0; i < options.length; i++) {
            if (i == menuOption) {
                g2.setColor(Color.CYAN);
                g2.drawString("> " + options[i] + " <", screenWidth / 2 - 120, 240 + (i * 60));
            } else {
                g2.setColor(Color.WHITE);
                g2.drawString(options[i], screenWidth / 2 - 90, 240 + (i * 60));
            }
        }

        g2.setFont(regularFont);
        g2.setColor(Color.RED);
        g2.drawString("Use W/S to Navigate | Press ENTER to Select | Press ESC to Exit", screenWidth / 2 - 270, screenHeight - 50);
    }

    // =========================================================================
    // 2. VẼ MÀN HÌNH CHỌN MAP
    // =========================================================================
    public void drawMapSelection(Graphics2D g2, String[] mapList, int currentMapIndex, int screenWidth, int screenHeight) {
        g2.setColor(new Color(20, 20, 30));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setColor(Color.YELLOW);
        g2.setFont(titleFont);
        g2.drawString("SELECT MAP", screenWidth / 2 - 150, 150);

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
        g2.drawString("TUTORIAL", 50, 80);

        g2.setFont(regularFont);
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("- Press W, A, S, D to Move the Player.", 70, 160);
        g2.drawString("- Press SPACE to Place a Bomb.", 70, 210);
        g2.drawString("- Avoid Flame and Enemies to survive.", 70, 260);
        g2.drawString("- Press P to Pause the game.", 70, 310);

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
    public void drawPauseScreen(Graphics2D g2, int screenWidth, int screenHeight) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setColor(Color.WHITE);
        g2.setFont(mediumFont);
        g2.drawString("GAME PAUSED", screenWidth / 2 - 100, screenHeight / 2);

        g2.setFont(regularFont);
        g2.drawString("Press ESC to go back to the menu or P to continue", screenWidth / 2 - 230, screenHeight / 2 + 40);
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
    // HÀM BỔ TRỢ: VẼ NÚT QUAY LẠI (DÙNG CHUNG CHO TUTORIAL, ABOUT US, LEADERBOARD)
    // =========================================================================
    private void drawBackButtonHint(Graphics2D g2, int screenHeight) {
        g2.setFont(italicFont);
        g2.setColor(Color.ORANGE);
        g2.drawString("<- Press ESC to Return Menu", 40, screenHeight - 50);
    }
}