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

        // --- ĐỌC DỮ LIỆU ĐIỀU KHIỂN TỪ BÀN PHÍM ---
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

        // Cấu hình viền hitbox thu nhỏ để di chuyển mượt hơn
        int margin = 10;
        int assistThreshold = 18;

        // Tạo Hitbox HIỆN TẠI phục vụ việc kiểm tra trạng thái thoát khỏi quả bom vừa đặt
        Rectangle currentHitbox = new Rectangle((int) getX() + margin, (int) getY() + margin,
                getWidth() - 2 * margin, getHeight() - 2 * margin);

        // =====================================================================
        // XỬ LÝ DI CHUYỂN TRÊN TRỤC X
        // =====================================================================
        if (movingX) {
            Rectangle hitboxX = new Rectangle((int) nextX + margin, (int) getY() + margin,
                    getWidth() - 2 * margin, getHeight() - 2 * margin);

            // Nếu ô tiếp theo không phải tường VÀ không bị vướng Bom -> Cho phép bước đi
            if (!cChecker.checkTile(hitboxX) && !cChecker.checkBomb(currentHitbox, hitboxX)) {
                this.setX(nextX);
            } else {
                // Hỗ trợ cơ chế tự động lách khe dọc khi đi ngang bị vướng góc tường/bom
                double centerOfTileY = Math.round(getY() / TILE_SIZE) * TILE_SIZE;
                double offset = Math.abs(getY() - centerOfTileY);

                if (offset > 0 && offset <= assistThreshold) {
                    if (getY() < centerOfTileY) {
                        Rectangle slideBox = new Rectangle((int) getX() + margin, (int) (getY() + speed) + margin,
                                getWidth() - 2 * margin, getHeight() - 2 * margin);
                        
                        if (!cChecker.checkTile(slideBox) && !cChecker.checkBomb(currentHitbox, slideBox)) {
                            this.setY(getY() + Math.min(speed, centerOfTileY - getY()));
                        }
                    } else if (getY() > centerOfTileY) {
                        Rectangle slideBox = new Rectangle((int) getX() + margin, (int) (getY() - speed) + margin,
                                getWidth() - 2 * margin, getHeight() - 2 * margin);
                        
                        if (!cChecker.checkTile(slideBox) && !cChecker.checkBomb(currentHitbox, slideBox)) {
                            this.setY(getY() - Math.min(speed, getY() - centerOfTileY));
                        }
                    }
                }
            }
        }

        // =====================================================================
        // XỬ LÝ DI CHUYỂN TRÊN TRỤC Y
        // =====================================================================
        if (movingY) {
            Rectangle hitboxY = new Rectangle((int) getX() + margin, (int) nextY + margin,
                    getWidth() - 2 * margin, getHeight() - 2 * margin);

            // Nếu ô tiếp theo không phải tường VÀ không bị vướng Bom -> Cho phép bước đi
            if (!cChecker.checkTile(hitboxY) && !cChecker.checkBomb(currentHitbox, hitboxY)) {
                this.setY(nextY);
            } else {
                // Hỗ trợ cơ chế tự động lách khe ngang khi đi dọc bị vướng góc tường/bom
                double centerOfTileX = Math.round(getX() / TILE_SIZE) * TILE_SIZE;
                double offset = Math.abs(getX() - centerOfTileX);

                if (offset > 0 && offset <= assistThreshold) {
                    if (getX() < centerOfTileX) {
                        Rectangle slideBox = new Rectangle((int) (getX() + speed) + margin, (int) getY() + margin,
                                getWidth() - 2 * margin, getHeight() - 2 * margin);
                        
                        if (!cChecker.checkTile(slideBox) && !cChecker.checkBomb(currentHitbox, slideBox)) {
                            this.setX(getX() + Math.min(speed, centerOfTileX - getX()));
                        }
                    } else if (getX() > centerOfTileX) {
                        Rectangle slideBox = new Rectangle((int) (getX() - speed) + margin, (int) getY() + margin,
                                getWidth() - 2 * margin, getHeight() - 2 * margin);
                        
                        if (!cChecker.checkTile(slideBox) && !cChecker.checkBomb(currentHitbox, slideBox)) {
                            this.setX(getX() - Math.min(speed, getX() - centerOfTileX));
                        }
                    }
                }
            }
        }

        // Đồng bộ lại vị trí hitbox chính xác của lớp cha GameObject để quét sát thương
        this.hitbox.x = (int) this.X;
        this.hitbox.y = (int) this.Y;

        return true;
    }

    @Override
    public boolean render(Graphics g) {
        // Vẽ được xử lý gián tiếp bằng Sprite qua AssetManager trong GamePanel
        return true;
    }

    public String getDirection() {
        return direction;
    }

    public boolean isFacingLeft() {
        return facingLeft;
    }
}