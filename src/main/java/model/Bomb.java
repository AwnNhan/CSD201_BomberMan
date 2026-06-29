package model;

import java.awt.Color;
import java.awt.Graphics;

public class Bomb extends GameObject {
    private long timeToExplode; 

    public Bomb(double X, double Y, int width, int height, IdObject id, long timeToExplode) {
        super(X, Y, width, height, id); 
        this.timeToExplode = timeToExplode;
    }

    public long getTimeToExplode() {
        return timeToExplode;
    }

    @Override
    public boolean update() {
        return true;
    }

    // ĐÃ CHUYỂN CODE VẼ BOM VÀO ĐÂY
    @Override
    public boolean render(Graphics g) {
        g.setColor(Color.ORANGE);
        // Tọa độ X, Y lưu dưới dạng ô lưới (grid), cần nhân với width (tileSize) để vẽ pixel
        g.fillOval((int) X * width + 4, (int) Y * height + 4, width - 8, height - 8);
        return true;
    }
}