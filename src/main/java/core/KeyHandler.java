/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core;

/**
 *
 * @author LENOVO
 */
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

    public boolean upPressed, downPressed, leftPressed, rightPressed, pausePressed;
    // BỔ SUNG: Biến kiểm soát trạng thái phím Space của bạn
    public boolean spacePressed; 

    @Override
    public void keyTyped(KeyEvent e) {
        // Không dùng hàm này cho game thời gian thực
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode(); // Lấy mã của phím vừa bấm

        if (code == KeyEvent.VK_W) {
            upPressed = true;
        }
        if (code == KeyEvent.VK_S) {
            downPressed = true;
        }
        if (code == KeyEvent.VK_A) {
            leftPressed = true;
        }
        if (code == KeyEvent.VK_D) {
            rightPressed = true;
        }

        if (code == KeyEvent.VK_P) {
            pausePressed = true;
        }

        // BỔ SUNG: Bắt sự kiện phím Space cho Kỹ sư cháy nổ
        if (code == KeyEvent.VK_SPACE) {
            spacePressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode(); // Lấy mã của phím vừa nhả ra

        // Khi nhả phím ra, phải tắt công tắc đi
        if (code == KeyEvent.VK_W) {
            upPressed = false;
        }
        if (code == KeyEvent.VK_S) {
            downPressed = false;
        }
        if (code == KeyEvent.VK_A) {
            leftPressed = false;
        }
        if (code == KeyEvent.VK_D) {
            rightPressed = false;
        }

        if (code == KeyEvent.VK_P) {
            pausePressed = false;
        }

        // BỔ SUNG: Khi người chơi nhả phím Space ra thì reset lại công tắc
        if (code == KeyEvent.VK_SPACE) {
            spacePressed = false;
        }
    }
}