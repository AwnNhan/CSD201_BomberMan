/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core;

import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 *
 * @author LENOVO
 */
public class SoundManager {

    private Clip bgmClip;
    private URL[] soundURL = new URL[10];

    public SoundManager() {
        // ==========================================
        // 1. CÁC HIỆU ỨNG ÂM THANH (Phát 1 lần - SFX)
        // ==========================================
        soundURL[0] = getClass().getResource("/sounds/explosion.wav");   // Tiếng bom nổ
        soundURL[1] = getClass().getResource("/sounds/hit.wav");         // Tiếng nhân vật trúng đòn
        soundURL[2] = getClass().getResource("/sounds/place_bomb.wav");  // Tiếng đặt bom
        soundURL[3] = getClass().getResource("/sounds/select.wav");      // Tiếng tít khi bấm Menu
        soundURL[4] = getClass().getResource("/sounds/enemy_die.wav");   // Tiếng quái vật bị tiêu diệt

        // ==========================================
        // 2. NHẠC TRẠNG THÁI VÀ NHẠC NỀN (BGM)
        // ==========================================
        soundURL[5] = getClass().getResource("/sounds/game_over.wav");   // Nhạc nền khi thua cuộc (Hết mạng)
        soundURL[6] = getClass().getResource("/sounds/bgm_level.wav");   // Nhạc nền lặp vô tận khi đang chơi game
        soundURL[7] = getClass().getResource("/sounds/victory.wav");     // Nhạc chiến thắng khi qua màn
    }

    public void playBGM(int index) {
        stopBGM();
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[index]);
            bgmClip = AudioSystem.getClip();
            bgmClip.open(ais);

            setVolume(bgmClip, -10.0f);

            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgmClip.start();
        } catch (Exception e) {
            System.err.println("Lỗi phát nhạc nền: " + e.getMessage());
        }
    }

    public void stopBGM() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
            bgmClip.close();
        }
    }

    public void playSFX(int index) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[index]);
            Clip sfxClip = AudioSystem.getClip();
            sfxClip.open(ais);
            sfxClip.start();
        } catch (Exception e) {
            System.err.println("Lỗi phát hiệu ứng âm thanh số " + index + ": " + e.getMessage());
        }
    }

    private void setVolume(Clip clip, float volume) {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(volume);
        }
    }
}
