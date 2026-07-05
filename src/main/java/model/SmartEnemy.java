/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import algorithm.GridPoint;
import algorithm.PathFinder;
import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

/**
 *
 * @author ADMIN
 */


public class SmartEnemy extends GameObject {

    private double speed;
    private static final int TILE_SIZE = 48;
    private int[][] currentMap;
    private PathFinder pathFinder;
    private List<GridPoint> currentPath;

    private GridPoint targetPos;
    private String direction = "DOWN";

    public SmartEnemy(double startX, double startY, int customSpeed) {
        super(startX, startY, TILE_SIZE, TILE_SIZE, IdObject.ENEMY);
        this.speed = customSpeed + 0.5; // Nhanh hơn một chút so với Enemy thường ở cùng level
        this.pathFinder = new PathFinder();
    }

    public void setRealData(int[][] map, int playerGridR, int playerGridC) {
        this.currentMap = map;
        this.targetPos = new GridPoint(playerGridR, playerGridC);
    }

    @Override
    public boolean update() {
        if (currentMap == null || targetPos == null) return true;

        if ((int) this.X % TILE_SIZE == 0 && (int) this.Y % TILE_SIZE == 0) {
            GridPoint myPos = new GridPoint((int) (this.Y / TILE_SIZE), (int) (this.X / TILE_SIZE));
            currentPath = pathFinder.bfsSearch(myPos, targetPos, currentMap);
        }

        if (currentPath != null && !currentPath.isEmpty()) {
            GridPoint nextStep = currentPath.get(0);

            double targetX = nextStep.c * TILE_SIZE;
            double targetY = nextStep.r * TILE_SIZE;

            double nextX = this.X;
            double nextY = this.Y;

            if (Math.abs(this.X - targetX) > 0) {
                if (Math.abs(this.X - targetX) <= speed) {
                    nextX = targetX;
                } else if (this.X < targetX) {
                    nextX += speed;
                    direction = "RIGHT";
                } else {
                    nextX -= speed;
                    direction = "LEFT";
                }
            } else if (Math.abs(this.Y - targetY) > 0) {
                if (Math.abs(this.Y - targetY) <= speed) {
                    nextY = targetY;
                } else if (this.Y < targetY) {
                    nextY += speed;
                    direction = "DOWN";
                } else {
                    nextY -= speed;
                    direction = "UP";
                }
            }

            this.setX(nextX);
            this.setY(nextY);
        }

        return true;
    }

    public String getDirection() {
        return direction;
    }

    @Override
    public boolean render(Graphics g) {
        g.setColor(new Color(255, 140, 0)); // Màu Cam đậm
        g.fillRect((int) getX(), (int) getY(), getWidth(), getHeight());
        return true;
    }
}