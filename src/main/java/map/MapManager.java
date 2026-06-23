/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package map;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MapManager {

    private int[][] mapMatrix;
    private final int maxRow = 13;
    private final int maxCol = 15;
    private final int tileSize = 48;

    public MapManager() {
        mapMatrix = new int[maxRow][maxCol];

        loadMap("/maps/map01.txt");
    }

    public void loadMap(String filePath) {
        try {
            InputStream is = getClass().getResourceAsStream(filePath);

            // Bắt lỗi: Nếu sai đường dẫn, Java sẽ trả về null
            if (is == null) {
                System.out.println("Not found contextpath: " + filePath);
                return;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            int col = 0;
            int row = 0;

            while (col < maxCol && row < maxRow) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }

                // Fix lỗi khoảng trắng: Xóa khoảng trắng thừa 2 đầu và cắt theo 1 hoặc nhiều dấu cách
                String numbers[] = line.trim().split("\\s+");

                while (col < maxCol && col < numbers.length) {
                    mapMatrix[row][col] = Integer.parseInt(numbers[col]);
                    col++;
                }
                if (col == maxCol) {
                    col = 0;
                    row++;
                }
            }
            br.close();
            System.out.println("✅ ĐÃ NẠP MAP THÀNH CÔNG VÀO MA TRẬN!");

        } catch (Exception e) {
            System.out.println("❌ Lỗi trong quá trình đọc map: " + e.getMessage());
            e.printStackTrace(); // In chi tiết dòng bị lỗi ra console
        }
    }

    public int[][] getMapMatrix() {
        return mapMatrix;
    }

    public void destroySoftWall(int row, int col) {
        if (mapMatrix[row][col] == 2) {
            mapMatrix[row][col] = 0;
        }
    }

    public void render(Graphics2D g2) {
        int col = 0;
        int row = 0;
        int x = 0;
        int y = 0;

        while (col < maxCol && row < maxRow) {
            int tileNum = mapMatrix[row][col];

            if (tileNum == 1) {
                g2.setColor(Color.DARK_GRAY);
                g2.fillRect(x, y, tileSize, tileSize);
            } else if (tileNum == 2) {
                g2.setColor(new Color(139, 69, 19));
                g2.fillRect(x, y, tileSize, tileSize);
            }

            g2.setColor(Color.BLACK);
            g2.drawRect(x, y, tileSize, tileSize);

            col++;
            x += tileSize;

            if (col == maxCol) {
                col = 0;
                x = 0;
                row++;
                y += tileSize;
            }
        }
    }
}
