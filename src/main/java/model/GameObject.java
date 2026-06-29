/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.awt.Graphics;
import java.awt.Rectangle;

/**
 *
 * @author LENOVO
 */
public abstract class GameObject {

    protected double X;
    protected double Y;
    protected int width;
    protected int height;
    protected IdObject id;
    protected Rectangle hitbox;

    public int solidAreaDefaultX = 0;
    public int solidAreaDefaultY = 0;

    public GameObject(double X, double Y, int width, int height, IdObject id) {
        this.X = X;
        this.Y = Y;
        this.width = width;
        this.height = height;
        this.id = id;
        this.hitbox = new Rectangle((int) X, (int) Y, width, height);
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public double getX() {
        return X;
    }

    public double getY() {
        return Y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public IdObject getId() {
        return id;
    }

    public void setX(double X) {
        this.X = X;
        if (hitbox != null) {
            hitbox.x = (int) X;
        }
    }

    public void setY(double Y) {
        this.Y = Y;
        if (hitbox != null) {
            hitbox.y = (int) Y;
        }
    }

    public void setId(IdObject id) {
        this.id = id;
    }

    public abstract boolean update();

    public abstract boolean render(Graphics g);
}
