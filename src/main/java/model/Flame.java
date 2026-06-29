package model;

import java.awt.Color;
import java.awt.Graphics;

public class Flame extends GameObject {
    private long idCreatedTime;
    private final long duration = 500; 

    public Flame(double X, double Y, int width, int height, IdObject id) {
        super(X, Y, width, height, id);
        this.idCreatedTime = System.currentTimeMillis();
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - idCreatedTime >= duration;
    }

    @Override
    public boolean update() {
        return true;
    }

    // ĐÃ CHUYỂN CODE VẼ LỬA VÀO ĐÂY
    @Override
    public boolean render(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect((int) X * width, (int) Y * height, width, height);
        return true;
    }
}