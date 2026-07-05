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
        } else if (keyH.leftPressed) {
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

        if (movingX) {
            Rectangle hitboxX = new Rectangle((int) nextX + margin, (int) nextY + margin,
                    getWidth() - 2 * margin, getHeight() - 2 * margin);

            if (!cChecker.checkTile(hitboxX)) {
                this.setX(nextX);
            } else {
                double centerOfTileY = Math.round(getY() / TILE_SIZE) * TILE_SIZE;
                double offset = Math.abs(getY() - centerOfTileY);

                if (offset > 0 && offset <= assistThreshold) {
                    if (getY() < centerOfTileY) {
                        Rectangle slideBox = new Rectangle((int) getX() + margin, (int) getY() + margin,
                                getWidth() - 2 * margin, getHeight() - 2 * margin);
                        if (!cChecker.checkTile(slideBox)) {
                            this.setY(getY() + Math.min(speed, centerOfTileY - getY()));
                        }
                    } else if (getY() > centerOfTileY) {
                        Rectangle slideBox = new Rectangle((int) getX() + margin, (int) (getY() - speed) + margin,
                                getWidth() - 2 * margin, getHeight() - 2 * margin);
                        if (!cChecker.checkTile(slideBox)) {
                            this.setY(getY() - Math.min(speed, getY() - centerOfTileY));
                        }
                    }
                }
            }
        }

        if (movingY) {
            Rectangle hitboxY = new Rectangle((int) getX() + margin, (int) nextY + margin, getWidth() - 2 * margin, getHeight() - 2 * margin);

            if (!cChecker.checkTile(hitboxY)) {
                this.setY(nextY); // Dọc trống -> Đi dọc bình thường
            } else {
                double centerOfTileX = Math.round(getX() / TILE_SIZE) * TILE_SIZE;
                double offset = Math.abs(getX() - centerOfTileX);

                // Nếu lệch trong ngưỡng cho phép -> Tự động trượt trái/phải để lọt khe
                if (offset > 0 && offset <= assistThreshold) {
                    if (getX() < centerOfTileX) {
                        Rectangle slideBox = new Rectangle((int) (getX() + speed) + margin, (int) getY() + margin, getWidth() - 2 * margin, getHeight() - 2 * margin);
                        if (!cChecker.checkTile(slideBox)) {
                            this.setX(getX() + Math.min(speed, centerOfTileX - getX()));
                        }
                    } else if (getX() > centerOfTileX) {
                        Rectangle slideBox = new Rectangle((int) (getX() - speed) + margin, (int) getY() + margin, getWidth() - 2 * margin, getHeight() - 2 * margin);
                        if (!cChecker.checkTile(slideBox)) {
                            this.setX(getX() - Math.min(speed, getX() - centerOfTileX));
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean render(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect((int) getX(), (int) getY(), getWidth(), getHeight());

        g.setColor(Color.GREEN);
        g.drawRect(getHitbox().x, getHitbox().y, getHitbox().width, getHitbox().height);

        return true;
    }

    public String getDirection() {
        return direction;
    }

    public boolean isFacingLeft() {
        return facingLeft;
    }
}
