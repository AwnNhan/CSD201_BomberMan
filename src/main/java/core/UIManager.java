/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Admin
 */
public class UIManager extends JLabel{
    private CardLayout cardLayout;
    private JPanel mainContainer;

    public UIManager() {
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // 1. Giao diện Start Game
        JPanel startGamePanel = new JPanel();
        startGamePanel.add(new JLabel("Start Game Screen"));

        // 2. Giao diện About Us (Có thể code thêm phần đọc file text tại đây)
        JPanel aboutUsPanel = new JPanel();
        aboutUsPanel.add(new JLabel("About Us Screen"));

        // 3. Giao diện Leaderboard
        JPanel leaderboardPanel = new JPanel();
        leaderboardPanel.add(new JLabel("Leaderboard Screen"));

        // Thêm các giao diện vào CardLayout
        mainContainer.add(startGamePanel, "START_GAME");
        mainContainer.add(aboutUsPanel, "ABOUT_US");
        mainContainer.add(leaderboardPanel, "LEADERBOARD");

        this.setLayout(new BorderLayout());
        this.add(mainContainer, BorderLayout.CENTER);
    }

    // Gọi hàm này để chuyển đổi giữa các màn hình Menu
    public void switchScreen(String screenName) {
        cardLayout.show(mainContainer, screenName);
    }
}
