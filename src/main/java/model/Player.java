package model;

import core.KeyHandler;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Player extends GameObject {

    private KeyHandler keyH;
    private double speed;
    private BufferedImage sprite; // Chứa hình ảnh nhân vật
    private static final int TILE_SIZE = 48;

    // Dữ liệu bản đồ và bom để xét va chạm
    private int[][] currentMap;
    private ArrayList<Bomb> bombList;

    public Player(double startX, double startY, KeyHandler keyH, BufferedImage sprite) {
        super(startX, startY, TILE_SIZE, TILE_SIZE, IdObject.PLAYER);
        this.keyH = keyH;
        this.sprite = sprite;
        this.speed = 4.0;
    }

    // Nhận dữ liệu từ GamePanel truyền sang
    public void setRealData(int[][] map, ArrayList<Bomb> bombs) {
        this.currentMap = map;
        this.bombList = bombs;
    }

    // Di dời thuật toán lách tường và xuyên bom từ GamePanel vào đây
    private boolean canMove(int nextX, int nextY) {
        if (currentMap == null || bombList == null) return false;

        int margin = 12; 
        int leftCol = (nextX + margin) / TILE_SIZE;
        int rightCol = (nextX + TILE_SIZE - margin - 1) / TILE_SIZE;
        int topRow = (nextY + margin) / TILE_SIZE;
        int bottomRow = (nextY + TILE_SIZE - margin - 1) / TILE_SIZE;

        if (leftCol < 0 || rightCol >= 15 || topRow < 0 || bottomRow >= 13) return false;

        if (currentMap[topRow][leftCol] != 0 || currentMap[topRow][rightCol] != 0 || 
            currentMap[bottomRow][leftCol] != 0 || currentMap[bottomRow][rightCol] != 0) {
            return false;
        }

        Rectangle nextHitbox = new Rectangle(nextX + margin, nextY + margin, TILE_SIZE - 2 * margin, TILE_SIZE - 2 * margin);
        Rectangle currentHitbox = new Rectangle((int)this.X + margin, (int)this.Y + margin, TILE_SIZE - 2 * margin, TILE_SIZE - 2 * margin);

        for (Bomb b : bombList) {
            Rectangle bombHitbox = new Rectangle((int)b.getX() * TILE_SIZE, (int)b.getY() * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            if (nextHitbox.intersects(bombHitbox)) {
                if (!currentHitbox.intersects(bombHitbox)) {
                    return false; 
                }
            }
        }
        return true;
    }

    @Override
    public boolean update() {
        int nextPlayerX = (int)this.X;
        int nextPlayerY = (int)this.Y;

        // Thuật toán Auto-align (Trượt góc)
        if (keyH.upPressed) {
            nextPlayerY -= speed;
            int targetX = (((int)this.X + TILE_SIZE / 2) / TILE_SIZE) * TILE_SIZE;
            if (this.X < targetX) nextPlayerX += Math.min(speed, targetX - this.X);
            else if (this.X > targetX) nextPlayerX -= Math.min(speed, this.X - targetX);
        } 
        else if (keyH.downPressed) {
            nextPlayerY += speed;
            int targetX = (((int)this.X + TILE_SIZE / 2) / TILE_SIZE) * TILE_SIZE;
            if (this.X < targetX) nextPlayerX += Math.min(speed, targetX - this.X);
            else if (this.X > targetX) nextPlayerX -= Math.min(speed, this.X - targetX);
        } 
        else if (keyH.leftPressed) {
            nextPlayerX -= speed;
            int targetY = (((int)this.Y + TILE_SIZE / 2) / TILE_SIZE) * TILE_SIZE;
            if (this.Y < targetY) nextPlayerY += Math.min(speed, targetY - this.Y);
            else if (this.Y > targetY) nextPlayerY -= Math.min(speed, this.Y - targetY);
        } 
        else if (keyH.rightPressed) {
            nextPlayerX += speed;
            int targetY = (((int)this.Y + TILE_SIZE / 2) / TILE_SIZE) * TILE_SIZE;
            if (this.Y < targetY) nextPlayerY += Math.min(speed, targetY - this.Y);
            else if (this.Y > targetY) nextPlayerY -= Math.min(speed, this.Y - targetY);
        }

        if (canMove(nextPlayerX, (int)this.Y)) this.setX(nextPlayerX);
        if (canMove((int)this.X, nextPlayerY)) this.setY(nextPlayerY);

        return true;
    }

    @Override
    public boolean render(Graphics g) {
        g.drawImage(sprite, (int)getX(), (int)getY(), getWidth(), getHeight(), null);
        return true;
    }
}