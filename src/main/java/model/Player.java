package model;

import core.CollisionChecker;
import core.KeyHandler;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * @author Nguyen Minh Phat - CE201621
 */
public class Player extends GameObject {

    private String direction = "DOWN";
    private boolean facingLeft = false;
    private KeyHandler keyH;
    private CollisionChecker cChecker;
    private double speed;

    private static final int TILE_SIZE = 48;

    public Player(double startX, double startY, KeyHandler keyH, CollisionChecker cChecker) {
        super(startX, startY, TILE_SIZE, TILE_SIZE, IdObject.PLAYER);
        this.keyH = keyH;
        this.cChecker = cChecker;
        this.speed = 4.0;
    }

    @Override
    public boolean update() {
        double nextX = getX();
        double nextY = getY();

        boolean movingX = false;
        boolean movingY = false;

        if (keyH.upPressed) {
            direction = "UP";
            nextY -= speed;
            movingY = true;
        } else if (keyH.downPressed) {
            direction = "DOWN";
            nextY += speed;
            movingY = true;
        }

        if (keyH.leftPressed) {
            direction = "LEFT";
            facingLeft = true;
            nextX -= speed;
            movingX = true;
        } else if (keyH.rightPressed) {
            direction = "RIGHT";
            facingLeft = false;
            nextX += speed;
            movingX = true;
        }

        int margin = 6;
        int assistThreshold = 16;

        // --- TRỤC X: Kiểm tra độc lập với tọa độ Y hiện tại (getY()) ---
        int margin = 10;
        int assistThreshold = 18;

        // 🌟 BƯỚC QUAN TRỌNG: Tạo Hitbox HIỆN TẠI đã được thu nhỏ margin để biết khi nào thực sự thoát khỏi bom
        Rectangle currentHitbox = new Rectangle((int) getX() + margin, (int) getY() + margin,
                getWidth() - 2 * margin, getHeight() - 2 * margin);

        // --- TRỤC X ---
        if (movingX) {
            Rectangle hitboxX = new Rectangle((int) nextX + margin, (int) getY() + margin,
                    getWidth() - 2 * margin, getHeight() - 2 * margin);

            if (!cChecker.checkTile(hitboxX)) {
                this.setX(nextX);
            } else {
                // Cơ chế tự động lách khe dọc khi đi ngang vướng góc tường
            // Cập nhật gọi checkBomb với currentHitbox
            if (!cChecker.checkTile(hitboxX) && !cChecker.checkBomb(currentHitbox, hitboxX)) {
                this.setX(nextX);
            } else {
                double centerOfTileY = Math.round(getY() / TILE_SIZE) * TILE_SIZE;
                double offset = Math.abs(getY() - centerOfTileY);

                if (offset > 0 && offset <= assistThreshold) {
                    if (getY() < centerOfTileY) {
                        Rectangle slideBox = new Rectangle((int) getX() + margin, (int) (getY() + speed) + margin,
                                getWidth() - 2 * margin, getHeight() - 2 * margin);
                        if (!cChecker.checkTile(slideBox)) {
                        if (!cChecker.checkTile(slideBox) && !cChecker.checkBomb(currentHitbox, slideBox)) {
                            this.setY(getY() + Math.min(speed, centerOfTileY - getY()));
                        }
                    } else if (getY() > centerOfTileY) {
                        Rectangle slideBox = new Rectangle((int) getX() + margin, (int) (getY() - speed) + margin,
                                getWidth() - 2 * margin, getHeight() - 2 * margin);
                        if (!cChecker.checkTile(slideBox)) {
                        if (!cChecker.checkTile(slideBox) && !cChecker.checkBomb(currentHitbox, slideBox)) {
                            this.setY(getY() - Math.min(speed, getY() - centerOfTileY));
                        }
                    }
                }
            }
        }

        // --- TRỤC Y: Kiểm tra độc lập với tọa độ X hiện tại (getX()) ---
        // --- TRỤC Y ---
        if (movingY) {
            Rectangle hitboxY = new Rectangle((int) getX() + margin, (int) nextY + margin,
                    getWidth() - 2 * margin, getHeight() - 2 * margin);

            if (!cChecker.checkTile(hitboxY)) {
                this.setY(nextY);
            } else {
                // Cơ chế tự động lách khe ngang khi đi dọc vướng góc tường
            // Cập nhật gọi checkBomb với currentHitbox
            if (!cChecker.checkTile(hitboxY) && !cChecker.checkBomb(currentHitbox, hitboxY)) {
                this.setY(nextY);
            } else {
                double centerOfTileX = Math.round(getX() / TILE_SIZE) * TILE_SIZE;
                double offset = Math.abs(getX() - centerOfTileX);

                if (offset > 0 && offset <= assistThreshold) {
                    if (getX() < centerOfTileX) {
                        Rectangle slideBox = new Rectangle((int) (getX() + speed) + margin, (int) getY() + margin,
                                getWidth() - 2 * margin, getHeight() - 2 * margin);
                        if (!cChecker.checkTile(slideBox)) {
                        if (!cChecker.checkTile(slideBox) && !cChecker.checkBomb(currentHitbox, slideBox)) {
                            this.setX(getX() + Math.min(speed, centerOfTileX - getX()));
                        }
                    } else if (getX() > centerOfTileX) {
                        Rectangle slideBox = new Rectangle((int) (getX() - speed) + margin, (int) getY() + margin,
                                getWidth() - 2 * margin, getHeight() - 2 * margin);
                        if (!cChecker.checkTile(slideBox)) {
                        if (!cChecker.checkTile(slideBox) && !cChecker.checkBomb(currentHitbox, slideBox)) {
                            this.setX(getX() - Math.min(speed, getX() - centerOfTileX));
                        }
                    }
                }
            }
        }

        // Đồng bộ lại hitbox chính xác của lớp cha GameObject để phục vụ quét sát thương thực thể
        this.hitbox.x = (int) this.X;
        this.hitbox.y = (int) this.Y;

        return true;
    }

    @Override
    public boolean render(Graphics g) {
        return true;
    }

    public String getDirection() {
        return direction;
    }

    public boolean isFacingLeft() {
        return facingLeft;
    }

    public String getDirection() {
        return direction;
    }

    public boolean isFacingLeft() {
        return facingLeft;
    }
}
