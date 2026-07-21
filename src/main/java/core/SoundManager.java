package core;

import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 * Lớp SoundManager quản lý toàn bộ âm thanh (SFX & Nhạc nền BGM) trong Game.
 */
public class SoundManager {

    private Clip bgmClip;
    private URL[] soundURL = new URL[15]; // Tăng kích thước mảng để chứa thêm nhạc Theme

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
    soundURL[6] = getClass().getResource("/sounds/theme1.wav");      // Nhạc nền mặc định (Classic)
    soundURL[7] = getClass().getResource("/sounds/victory.wav");     // Nhạc chiến thắng khi qua màn

    // ==========================================
    // 3. THÊM CÁC THEME NHẠC NỀN DÀNH CHO CÁC MÀN KHÁC
    // ==========================================
    soundURL[8] = getClass().getResource("/sounds/theme2.wav");      // Theme Núi lửa (Map 2)
    soundURL[9] = getClass().getResource("/sounds/theme3.wav");      // Theme Đánh Boss (Map 3)
    
    // ==========================================
    // 4. NHẠC NỀN CHO MENU GAME
    // ==========================================
    soundURL[10] = getClass().getResource("/sounds/menu.wav");       // <--- THÊM DÒNG NÀY (Index 10)
}

    // ==========================================
    // HÀM CHUYỂN THEME NHẠC THEO INDEX MÀN CHƠI
    // ==========================================
    public void changeTheme(int mapIndex) {
        switch (mapIndex) {
            case 0: // Map 1 (Default / Classic)
                playBGM(6); 
                break;
            case 1: // Map 2 (Smart Enemy / Volcano)
                playBGM(soundURL[8] != null ? 8 : 6); 
                break;
            case 2: // Map 3 (Boss)
                playBGM(soundURL[9] != null ? 9 : 6); 
                break;
            default:
                playBGM(6);
                break;
        }
    }

    public void playBGM(int index) {
        stopBGM();
        try {
            if (soundURL[index] == null) {
                System.err.println("Cảnh báo: Chưa tìm thấy file âm thanh ở index " + index);
                return;
            }
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
            if (soundURL[index] == null) return;
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