/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.csd201_bommerman;

import core.GamePanel;
import javax.swing.JFrame;

/**
 *
 * @author LENOVO
 */
public class CSD201_BommerMan {

    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("CSD201 - Bomberman");

        // Gắn động cơ vào khung xe
        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);
        window.pack(); // Ép JFrame bó sát vào kích thước GamePanel

        window.setLocationRelativeTo(null); // Hiển thị ở giữa màn hình
        window.setVisible(true);

        gamePanel.requestFocus();
        // KÍCH NỔ ĐỘNG CƠ!
        gamePanel.startGameThread();
    }
}
