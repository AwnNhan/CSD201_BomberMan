package model;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

/**
 * Lớp Flame quản lý các tia lửa sinh ra khi bom nổ.
 * Có tính năng tự động phai mờ (Fade out) và phân biệt lửa của Player/Boss.
 */
public class Flame extends GameObject {
    private final long createTime;       // Lưu lại thời điểm ngọn lửa vừa bùng lên
    private final long duration = 500;   // Ngọn lửa sẽ cháy trong 500ms (Nửa giây)
    private String type;                 // Lưu loại tia lửa (CENTER, HORIZONTAL, VERTICAL, END) để vẽ hình sau này
    private boolean isBossFlame;         // CỜ QUAN TRỌNG: Đánh dấu xem lửa này là của Boss ném ra hay của Player

    // =========================================================================
    // CÁC CONSTRUCTOR (ĐA HÌNH) - CHỐNG LỖI "CANNOT FIND SYMBOL"
    // =========================================================================

    // 1. Constructor siêu đầy đủ (Dùng cho BombManager khi gọi lửa Boss)
    public Flame(double X, double Y, int width, int height, IdObject id, String type, boolean isBossFlame) {
        super(X, Y, width, height, id);
        this.createTime = System.currentTimeMillis();
        this.type = type;
        this.isBossFlame = isBossFlame;
    }

    // 2. Constructor thiếu cờ Boss (Mặc định hiểu là lửa của Player -> isBossFlame = false)
    public Flame(double X, double Y, int width, int height, IdObject id, String type) {
        this(X, Y, width, height, id, type, false); 
    }

    // 3. Constructor thiếu type (Mặc định hiểu là CENTER)
    public Flame(double X, double Y, int width, int height, IdObject id, boolean isBossFlame) {
        this(X, Y, width, height, id, "CENTER", isBossFlame);
    }

    // 4. Constructor tối giản nhất (Mặc định type = CENTER, isBossFlame = false)
    public Flame(double X, double Y, int width, int height, IdObject id) {
        this(X, Y, width, height, id, "CENTER", false);
    }

    // =========================================================================
    // CÁC HÀM GETTER VÀ LOGIC CƠ BẢN
    // =========================================================================

    // Hàm này được GamePanel gọi liên tục để xem lửa đã cháy đủ 500ms chưa. Nếu rồi thì xóa.
    public boolean isExpired() {
        return System.currentTimeMillis() - createTime >= duration;
    }

    // Hàm này giúp GamePanel kiểm tra xem lửa này có phải của Boss không, 
    // để tránh việc Boss bước vào lửa của chính mình mà bị mất máu.
    public boolean isBossFlame() {
        return this.isBossFlame;
    }

    @Override
    public boolean update() {
        // Lửa chỉ đứng im một chỗ và chờ tắt, không có logic di chuyển
        return true;
    }

    // =========================================================================
    // VẼ ĐỒ HỌA NGỌN LỬA (CÓ HIỆU ỨNG VÀ MÀU SẮC RIÊNG BIỆT)
    // =========================================================================

    @Override
    public boolean render(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        // Bật khử răng cưa giúp hình vẽ mềm mại, không bị vỡ hạt ở viền
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Tính toán độ mờ (Alpha) dựa trên thời gian đã cháy để tạo hiệu ứng tắt dần
        long elapsed = System.currentTimeMillis() - createTime;
        float alpha = 1.0f - ((float) elapsed / duration);
        if (alpha < 0) alpha = 0;
        if (alpha > 1) alpha = 1;
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        int x = (int) getX();
        int y = (int) getY();
        int w = getWidth();
        int h = getHeight();

        // 1. TẠO KHUNG NGOÀI CỦA NGỌN LỬA BẰNG PATH2D (Vẽ bằng code thay vì dùng ảnh)
        Path2D.Double outerFlame = new Path2D.Double();
        outerFlame.moveTo(x + w * 0.5, y + h * 0.05); // Chấm điểm đỉnh trên cùng
        outerFlame.quadTo(x + w * 0.75, y + h * 0.35, x + w * 0.65, y + h * 0.45); // Uốn phải
        outerFlame.quadTo(x + w * 0.85, y + h * 0.55, x + w * 0.75, y + h * 0.85); // Phình to ra
        outerFlame.quadTo(x + w * 0.5, y + h * 1.0, x + w * 0.25, y + h * 0.85);   // Bo tròn đáy
        outerFlame.quadTo(x + w * 0.1, y + h * 0.5, x + w * 0.35, y + h * 0.35);   // Uốn trái
        outerFlame.quadTo(x + w * 0.4, y + h * 0.2, x + w * 0.5, y + h * 0.05);    // Trở về đỉnh
        outerFlame.closePath();

        // 2. TẠO LÕI SÁNG BÊN TRONG CỦA NGỌN LỬA
        Path2D.Double innerFlame = new Path2D.Double();
        innerFlame.moveTo(x + w * 0.5, y + h * 0.4);
        innerFlame.quadTo(x + w * 0.65, y + h * 0.55, x + w * 0.6, y + h * 0.65);
        innerFlame.quadTo(x + w * 0.7, y + h * 0.75, x + w * 0.65, y + h * 0.85);
        innerFlame.quadTo(x + w * 0.5, y + h * 0.98, x + w * 0.35, y + h * 0.85);
        innerFlame.quadTo(x + w * 0.3, y + h * 0.7, x + w * 0.4, y + h * 0.6);
        innerFlame.quadTo(x + w * 0.45, y + h * 0.5, x + w * 0.5, y + h * 0.4);
        innerFlame.closePath();

        // 3. ĐỔ MÀU GRADIENT TÙY THEO LOẠI LỬA (LỬA BOSS MÀU TÍM, LỬA PLAYER MÀU CAM)
        GradientPaint gradOuter;
        GradientPaint gradInner;

        if (isBossFlame) {
            // Lửa của Boss: Từ Tím nhạt -> Tím đậm
            gradOuter = new GradientPaint(x, y, new Color(148, 0, 211), x, y + h, new Color(75, 0, 130));
            // Lõi của Boss: Từ Hồng rực -> Tím Magenta
            gradInner = new GradientPaint(x, y + h * 0.4f, new Color(255, 105, 180), x, y + h, new Color(255, 0, 255));
        } else {
            // Lửa của Player: Từ Cam Đỏ -> Cà Chua
            gradOuter = new GradientPaint(x, y, new Color(255, 69, 0), x, y + h, new Color(255, 99, 71));
            // Lõi của Player: Từ Vàng Chanh -> Cam Rực
            gradInner = new GradientPaint(x, y + h * 0.4f, new Color(255, 215, 0), x, y + h, new Color(255, 140, 0));
        }

        // Thực hiện tô màu
        g2.setPaint(gradOuter);
        g2.fill(outerFlame);

        g2.setPaint(gradInner);
        g2.fill(innerFlame);

        g2.dispose();
        return true;
    }
}