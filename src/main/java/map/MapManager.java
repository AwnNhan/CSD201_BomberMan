package map;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MapManager {

    private int[][] mapMatrix;
    private final int maxRow = 13;
    private final int maxCol = 25; 
    private final int tileSize = 48;

    public MapManager() {
        mapMatrix = new int[maxRow][maxCol];
    }

    public void loadMap(String filePath) {
        try {
            InputStream is = getClass().getResourceAsStream(filePath);

            if (is == null) {
                System.out.println("❌ LỖI NGHIÊM TRỌNG: Không tìm thấy file map -> " + filePath);
                return;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            // Đọc dữ liệu ma trận 13x25
            for (int row = 0; row < maxRow; row++) {
                String line = br.readLine();
                if (line == null) break; 

                String[] numbers = line.trim().split("\\s+");

                for (int col = 0; col < maxCol && col < numbers.length; col++) {
                    mapMatrix[row][col] = Integer.parseInt(numbers[col]);
                }
            }
            
            br.close();
            System.out.println("✅ ĐÃ NẠP MAP THÀNH CÔNG: " + filePath);

        } catch (Exception e) {
            System.out.println("❌ Lỗi trong quá trình đọc map: " + e.getMessage());
            e.printStackTrace(); 
        }
    }

    public int[][] getMapMatrix() {
        return mapMatrix;
    }

    public void destroySoftWall(int row, int col) {
        if (row >= 0 && row < maxRow && col >= 0 && col < maxCol) {
            if (mapMatrix[row][col] == 2) {
                mapMatrix[row][col] = 0;
            }
        }
    }

    public void render(Graphics2D g2) {
        int x = 0;
        int y = 0;

        for (int row = 0; row < maxRow; row++) {
            for (int col = 0; col < maxCol; col++) {
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

                x += tileSize;
            }
            x = 0;
            y += tileSize;
        }
    }

    public int getMaxRow() {
        return maxRow;
    }

    public int getMaxCol() {
        return maxCol;
    }
}