/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import core.CollisionChecker;
import core.KeyHandler;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 *
 * @author Nguyen Minh Phat - CE201621
 */
public class Player extends GameObject {

    private KeyHandler keyH;
    private CollisionChecker cChecker;
    private double speed;

    private static final int TILE_SIZE = 48;

    public Player(double startX, double startY, KeyHandler keyH, CollisionChecker cChecker) {
        super(startX, startY, TILE_SIZE, TILE_SIZE, IdObject.PLAYER);
        this.keyH = keyH;
        this.cChecker = cChecker;
        this.speed = 4.0;

        this.solidAreaDefaultX = 8;
        this.solidAreaDefaultY = 16;
        this.hitbox = new Rectangle((int) startX + solidAreaDefaultX, (int) startY + solidAreaDefaultY, 32, 32);
    }

    @Override
    public boolean update() {
        double nextX = getX();
        double nextY = getY();
        boolean isMoving = false;

        if (keyH.upPressed) {
            nextY -= speed;
            isMoving = true;
        } else if (keyH.downPressed) {
            nextY += speed;
            isMoving = true;
        } else if (keyH.leftPressed) {
            nextX -= speed;
            isMoving = true;
        } else if (keyH.rightPressed) {
            nextX += speed;
            isMoving = true;
        }

        if (isMoving) {
            // ==========================================
            // THUẬT TOÁN AUTO-CENTERING (HÚT VÀO TÂM)
            // ==========================================
            // Tính toán xem nhân vật đang nằm ở Cột và Dòng nào trên bản đồ
            int currentCol = (int) (getX() + TILE_SIZE / 2) / TILE_SIZE;
            int currentRow = (int) (getY() + TILE_SIZE / 2) / TILE_SIZE;

            // Tọa độ chuẩn mực (Pixel) mà nhân vật CẦN phải đứng để ở chính giữa ô
            double perfectX = currentCol * TILE_SIZE;
            double perfectY = currentRow * TILE_SIZE;

            // Nếu đang đi DỌC (Lên/Xuống) -> Ép trục NGANG (X) trôi dần về perfectX
            if (keyH.upPressed || keyH.downPressed) {
                if (getX() < perfectX) {
                    nextX += speed; // Trôi nhẹ sang phải để vào giữa
                    if (nextX > perfectX) {
                        nextX = perfectX; // Khóa chặt không cho đi lố
                    }
                } else if (getX() > perfectX) {
                    nextX -= speed; // Trôi nhẹ sang trái để vào giữa
                    if (nextX < perfectX) {
                        nextX = perfectX;
                    }
                }
            }

            // Nếu đang đi NGANG (Trái/Phải) -> Ép trục DỌC (Y) trôi dần về perfectY
            if (keyH.leftPressed || keyH.rightPressed) {
                if (getY() < perfectY) {
                    nextY += speed;
                    if (nextY > perfectY) {
                        nextY = perfectY;
                    }
                } else if (getY() > perfectY) {
                    nextY -= speed;
                    if (nextY < perfectY) {
                        nextY = perfectY;
                    }
                }
            }

            // 2. GỌI TRỌNG TÀI KIỂM TRA TỌA ĐỘ SAU KHI ĐÃ HÚT TÂM
            Rectangle nextHitbox = new Rectangle(
                    (int) nextX + solidAreaDefaultX,
                    (int) nextY + solidAreaDefaultY,
                    this.hitbox.width,
                    this.hitbox.height
            );

            if (!cChecker.checkTile(nextHitbox)) {
                this.setX(nextX);
                this.setY(nextY);
            }
        }
        return true;
    }

    @Override
    public boolean render(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect((int) getX(), (int) getY(), getWidth(), getHeight());

        g.setColor(Color.GREEN);
        if (hitbox != null) {
            g.drawRect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);
        }
        return true;
    }
}
