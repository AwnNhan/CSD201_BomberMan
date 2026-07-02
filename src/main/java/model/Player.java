/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import core.KeyHandler;
import core.CollisionChecker;
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

        if (keyH.upPressed) {
             direction = "UP";
            nextY -= speed;
        } else if (keyH.downPressed) {
             direction = "DOWN";
            nextY += speed;
        } else if (keyH.leftPressed) {
            direction = "LEFT";
             facingLeft = true;
            nextX -= speed;
        } else if (keyH.rightPressed) {
            direction = "RIGHT";
             facingLeft = false;
            nextX += speed;
        }

        Rectangle nextHitbox = new Rectangle((int) nextX, (int) nextY, getWidth(), getHeight());

        if (!cChecker.checkTile(nextHitbox)) {

            this.setX(nextX);
            this.setY(nextY);
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
    public String getDirection()
{
    return direction;
}


public boolean isFacingLeft()
{
    return facingLeft;
}
}
