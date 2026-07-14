package model;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Enemy extends GameObject {

    // Giữ cấu hình protected để SmartEnemy và Boss có thể kế thừa và sử dụng
    protected int speed = 2; // Tốc độ di chuyển (Nên để số nguyên chẵn như 2 để khớp Grid 48)
    protected int currentDir = -1; // Hướng hiện tại: 0=Lên, 1=Xuống, 2=Trái, 3=Phải
    protected String direction = "DOWN";
    protected static final int TILE_SIZE = 48;
    protected int[][] currentMap;
    protected Random random = new Random();
    
    // Tọa độ mục tiêu (Player) để dành cho AI nâng cao (SmartEnemy, Boss)
    protected int targetR = -1; 
    protected int targetC = -1; 

    // Constructor 1: Mặc định (2 tham số)
    public Enemy(double startX, double startY) {
        super(startX, startY, TILE_SIZE, TILE_SIZE, IdObject.ENEMY);
    }

    // Constructor 2: Có chỉnh tốc độ (3 tham số) -> Giải quyết việc sinh quái theo cấu hình Level
    public Enemy(double startX, double startY, int customSpeed) {
        super(startX, startY, TILE_SIZE, TILE_SIZE, IdObject.ENEMY);
        this.speed = customSpeed;
    }

    public void setRealData(int[][] map) {
        this.currentMap = map;
    }

    public void setTarget(int r, int c) {
        this.targetR = r;
        this.targetC = c;
    }

    @Override
    public boolean update() {
        if (currentMap == null) return true;

        // Chỉ đưa ra quyết định chuyển hướng khi Quái vật nằm VỪA KHÍT trong 1 ô vuông lưới
        if ((int) this.X % TILE_SIZE == 0 && (int) this.Y % TILE_SIZE == 0) {
            List<Integer> validDirs = getValidDirections();

            // Bị kẹt cứng 4 bề thì đứng im
            if (validDirs.isEmpty()) {
                return true;
            }

            // Đổi hướng ngẫu nhiên nếu: 1. Mới sinh ra, 2. Đường cũ là tường vấp mặt, 3. Tỉ lệ ngẫu nhiên thích rẽ (1/5)
            if (currentDir == -1 || !validDirs.contains(currentDir) || random.nextInt(5) == 0) {
                currentDir = validDirs.get(random.nextInt(validDirs.size()));
            }
        }

        // Cứ thế tiếp tục bước đi theo hướng đã chọn
        if (currentDir == 0) {
            this.setY(this.Y - speed); // Lên
            direction = "UP";
        } else if (currentDir == 1) {
            this.setY(this.Y + speed); // Xuống
            direction = "DOWN";
        } else if (currentDir == 2) {
            this.setX(this.X - speed); // Trái
            direction = "LEFT";
        } else if (currentDir == 3) {
            this.setX(this.X + speed); // Phải
            direction = "RIGHT";
        }

        return true; 
    }

    // Hàm check 4 hướng bao quanh, nếu là ô trống (số 0) thì quái mới được đi vào
    protected List<Integer> getValidDirections() {
        List<Integer> dirs = new ArrayList<>();
        int col = (int) (this.X / TILE_SIZE);
        int row = (int) (this.Y / TILE_SIZE);

        if (row > 0 && currentMap[row - 1][col] == 0) {
            dirs.add(0); // Lên
        }
        if (row < currentMap.length - 1 && currentMap[row + 1][col] == 0) {
            dirs.add(1); // Xuống
        }
        if (col > 0 && currentMap[row][col - 1] == 0) {
            dirs.add(2); // Trái
        }
        if (col < currentMap[0].length - 1 && currentMap[row][col + 1] == 0) {
            dirs.add(3); // Phải
        }
        return dirs;
    }

    @Override
    public boolean render(Graphics g) {
        // Hàm này sẽ được GamePanel ghi đè bằng hình ảnh vẽ đè lên, để tạm màu đỏ phòng hờ
        g.setColor(Color.RED);
        g.fillRect((int) getX(), (int) getY(), getWidth(), getHeight());
        return true;
    }

    public String getDirection() {
        return direction;
    }
}