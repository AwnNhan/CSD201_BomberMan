/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;
import algorithm.PathFinder;
import algorithm.GridPoint;
/**
 *
 * @author ADMIN
 */
public class Enemy extends GameObject {
    
    private int hp;
    private double speed;
    private PathFinder pathFinder;
    private List<GridPoint> currentPath;

    // Kích thước chuẩn từ GamePanel của Người số 1 (16 x 3 = 48)
    private static final int TILE_SIZE = 48; 

    // Constructor: Truyền đúng 5 tham số mà lớp cha GameObject yêu cầu
    public Enemy(double startX, double startY) {
        // Truyền: X, Y, width, height, id
        super(startX, startY, TILE_SIZE, TILE_SIZE, IdObject.ENEMY);
        
        this.hp = 100;
        this.speed = 1.5; // Tốc độ di chuyển
        this.pathFinder = new PathFinder();
    }

    @Override
    public boolean update() {
        // --- 1. LẤY DỮ LIỆU TỪ CÁC KỸ SƯ KHÁC ---
        GridPoint playerPos = getPlayerGridPosition(); 
        int[][] currentMap = getMapFromPerson3();      

        // Tính tọa độ Grid hiện tại của Quái vật
        // Chú ý dùng biến X, Y viết hoa kế thừa từ GameObject
        GridPoint myPos = new GridPoint((int)(this.Y / TILE_SIZE), (int)(this.X / TILE_SIZE));

        // --- 2. TÌM ĐƯỜNG BFS ---
        currentPath = pathFinder.bfsSearch(myPos, playerPos, currentMap);

        // --- 3. DI CHUYỂN ---
        if (currentPath != null && !currentPath.isEmpty()) {
            GridPoint nextStep = currentPath.get(0); 
            
            double targetX = nextStep.c * TILE_SIZE;
            double targetY = nextStep.r * TILE_SIZE;

            // Tính toán X, Y mới
            double nextX = this.X;
            double nextY = this.Y;

            if (this.X < targetX) nextX += speed;
            else if (this.X > targetX) nextX -= speed;
            
            if (this.Y < targetY) nextY += speed;
            else if (this.Y > targetY) nextY -= speed;

            // SỬ DỤNG HÀM CỦA NGƯỜI SỐ 1: setX và setY sẽ tự động cập nhật luôn cả Hitbox!
            this.setX(nextX);
            this.setY(nextY);
        }

        return true; // Trả về true báo hiệu update thành công (Quái vật vẫn còn sống)
    }

    @Override
    public boolean render(Graphics g) {
        // Tạm thời vẽ khối vuông màu đỏ đại diện cho Quái vật
        g.setColor(Color.RED);
        // Lấy X, Y, width, height từ các hàm get của GameObject
        g.fillRect((int)getX(), (int)getY(), getWidth(), getHeight());
        
        return true; // Báo hiệu render thành công
    }

    // ==========================================
    // CÁC HÀM MOCK - CẦN GHÉP NỐI VỚI TEAM ĐỂ CÓ DỮ LIỆU THẬT
    // ==========================================
    private GridPoint getPlayerGridPosition() { 
        // Thay bằng hàm lấy tọa độ thực tế của Player
        return new GridPoint(2, 2); 
    }
    
    private int[][] getMapFromPerson3() { 
        // Trả về ma trận 13 dòng x 15 cột theo đúng kích thước GamePanel
        return new int[13][15]; 
    }
}
