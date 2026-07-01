package model;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Enemy extends GameObject {
    
    private int speed = 2; // Tốc độ di chuyển (Nên để số nguyên chẵn như 2 để khớp Grid 48)
    private int currentDir = -1; // Hướng hiện tại: 0=Lên, 1=Xuống, 2=Trái, 3=Phải
    public String direction = "right";
    private static final int TILE_SIZE = 48; 
    private int[][] currentMap;
    private Random random = new Random();

    public Enemy(double startX, double startY) {
        super(startX, startY, TILE_SIZE, TILE_SIZE, IdObject.ENEMY);
    }

    public void setRealData(int[][] map) {
        this.currentMap = map;
    }

    @Override
    public boolean update() {
        if (currentMap == null) return true;

        // Chỉ đưa ra quyết định chuyển hướng khi Quái vật nằm VỪA KHÍT trong 1 ô vuông lưới
        if ((int)this.X % TILE_SIZE == 0 && (int)this.Y % TILE_SIZE == 0) {
            List<Integer> validDirs = getValidDirections();
            
            // Bị kẹt cứng 4 bề
            if (validDirs.isEmpty()) return true;

            // Đổi hướng ngẫu nhiên nếu: 1. Mới sinh ra, 2. Đường cũ là tường vấp mặt, 3. Tỉ lệ ngẫu nhiên thích rẽ (1/5)
            if (currentDir == -1 || !validDirs.contains(currentDir) || random.nextInt(5) == 0) {
                currentDir = validDirs.get(random.nextInt(validDirs.size()));
            }
        }

        // Cứ thế tiếp tục bước đi theo hướng đã chọn
        if (currentDir == 0) {this.setY(this.Y - speed); // Lên
        direction = "up";  }        // THÊM DÒNG NÀY
        else if (currentDir == 1) {this.setY(this.Y + speed); // Xuống
                direction = "down"; }       // THÊM DÒNG NÀY
        else if (currentDir == 2) {this.setX(this.X - speed); // Trái
        direction = "left";}
        else if (currentDir == 3) {this.setX(this.X + speed); // Phải
         direction = "right";}
        return true; 
    }

    // Hàm check 4 hướng, nếu là ô trống (số 0) thì quái mới được đi vào
    private List<Integer> getValidDirections() {
        List<Integer> dirs = new ArrayList<>();
        int col = (int)(this.X / TILE_SIZE);
        int row = (int)(this.Y / TILE_SIZE);

        if (row > 0 && currentMap[row - 1][col] == 0) dirs.add(0); // Lên
        if (row < currentMap.length - 1 && currentMap[row + 1][col] == 0) dirs.add(1); // Xuống
        if (col > 0 && currentMap[row][col - 1] == 0) dirs.add(2); // Trái  
        if (col < currentMap[0].length - 1 && currentMap[row][col + 1] == 0) dirs.add(3); // Phải 
       return dirs;
    }

    @Override
    public boolean render(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect((int)getX(), (int)getY(), getWidth(), getHeight());
        return true; 
    }
}