package model;

import java.awt.Color;
import java.awt.Graphics;

public class Bomb extends GameObject {
    private long timeToExplode; // Thời gian hệ thống (ms) lúc quả bom sẽ nổ
    private boolean isBossBomb; // Cờ đánh dấu bom của Boss

    // Constructor 1: Dành cho Player (Mặc định isBossBomb = false)
    public Bomb(double X, double Y, int width, int height, IdObject id, long timeToExplode) {
        super(X, Y, width, height, id); // Truyền đủ 5 tham số lên lớp cha
        this.timeToExplode = timeToExplode;
        this.isBossBomb = false;
    }

    // Constructor 2: Dành cho Boss (Cho phép truyền cờ isBossBomb = true)
    public Bomb(double X, double Y, int width, int height, IdObject id, long timeToExplode, boolean isBossBomb) {
        super(X, Y, width, height, id); 
        this.timeToExplode = timeToExplode;
        this.isBossBomb = isBossBomb;
    }

    public long getTimeToExplode() {
        return timeToExplode;
    }

    public boolean isBossBomb() {
        return isBossBomb;
    }

    // BẮT BUỘC: Triển khai hàm update từ lớp cha abstract
    @Override
    public boolean update() {
        return true;
    }

    // BẮT BUỘC: Triển khai hàm render từ lớp cha abstract
    @Override
    public boolean render(Graphics g) {
        // Vẽ bom của Boss bằng màu Tím, bom người chơi màu Cam
        if (isBossBomb) {
            g.setColor(new Color(138, 43, 226)); 
        } else {
            g.setColor(Color.ORANGE);
        }
        
        g.fillOval((int) X + 4, (int) Y + 4, width - 8, height - 8);
        return true;
    }
    
    
}