package model;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
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
    private final long duration = 400;   // Ngọn lửa sẽ cháy trong 400ms (Nửa giây)
    private String type;                 // Lưu loại tia lửa (CENTER, HORIZONTAL, VERTICAL, END)
    private boolean isBossFlame;         // CỜ ĐÁNH DẤU: Lửa của Boss hay của Player

    // =========================================================================
    // CÁC CONSTRUCTOR (ĐA HÌNH) - CHỐNG LỖI "CANNOT FIND SYMBOL" KHHI GỌI TỪ BOMBMANAGER
    // =========================================================================

    // 1. Constructor đầy đủ nhất
    public Flame(double X, double Y, int width, int height, IdObject id, String type, boolean isBossFlame) {
        super(X, Y, width, height, id);
        this.createTime = System.currentTimeMillis();
        this.type = type;
        this.isBossFlame = isBossFlame;
    }

    // 2. Mặc định hiểu là lửa của Player (isBossFlame = false)
    public Flame(double X, double Y, int width, int height, IdObject id, String type) {
        this(X, Y, width, height, id, type, false); 
    }

    // 3. Mặc định hiểu là ô CENTER
    public Flame(double X, double Y, int width, int height, IdObject id, boolean isBossFlame) {
        this(X, Y, width, height, id, "CENTER", isBossFlame);
    }

    // 4. Constructor tối giản
    public Flame(double X, double Y, int width, int height, IdObject id) {
        this(X, Y, width, height, id, "CENTER", false);
    }

    // =========================================================================
    // HÀM LOGIC CƠ BẢN
    // =========================================================================

    // Kiểm tra hết hạn hiển thị lửa (500ms)
    public boolean isExpired() {
        return System.currentTimeMillis() - createTime >= duration;
    }

    // Kiểm tra xuất xứ lửa
    public boolean isBossFlame() {
        return this.isBossFlame;
    }

    @Override
    public boolean update() {
        // Lửa chỉ đứng im một chỗ và chờ tắt, không di chuyển
        return true;
    }

    // =========================================================================
    // VẼ ĐỒ HỌA NGỌN LỬA PHAI MỜ TỰ ĐỘNG KHÔNG LỖI BIÊN DỊCH
    // =========================================================================
    @Override
    public boolean render(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        // Bật khử răng cưa siêu mịn
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Hiệu ứng phai màu (Fade Out) theo thời gian thực
        long elapsed = System.currentTimeMillis() - createTime;
        float alpha = 1.0f - ((float) elapsed / duration);
        if (alpha < 0) alpha = 0;
        if (alpha > 1) alpha = 1;
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        // =========================================================================
        // THU NHỎ TẦM HIỂN THỊ CỦA LỬA (PADDING & SCALE)
        // =========================================================================
        // Chỉnh scaleRatio: 0.82f nghĩa là lửa bằng 82% kích thước ô (giảm ~18%)
        float scaleRatio = 0.82f; 

        int rawW = getWidth();
        int rawH = getHeight();

        // Tính kích thước thu nhỏ và căn vào chính giữa ô
        int w = (int) (rawW * scaleRatio);
        int h = (int) (rawH * scaleRatio);
        int x = (int) getX() + (rawW - w) / 2;
        int y = (int) getY() + (rawH - h) / 2;

        if ("CENTER".equals(type)) {
            // =================================================================
            // TÁI TẠO CHUẨN ĐÁM MÂY NẤM HẠT NHÂN (DÀNH CHO TÂM VỤ NỔ)
            // =================================================================

            if (isBossFlame) {
                // === PHIÊN BẢN BOSS (MÀU TÍM/ĐEN ĐỘC HẠI) ===
                // Thân nấm viền tím nhạt
                g2.setColor(new Color(186, 85, 211)); 
                Path2D.Double stemOuter = new Path2D.Double();
                stemOuter.moveTo(x + w * 0.36, y + h * 0.45);
                stemOuter.curveTo(x + w * 0.32, y + h * 0.65, x + w * 0.36, y + h * 0.75, x + w * 0.34, y + h * 0.85);
                stemOuter.lineTo(x + w * 0.66, y + h * 0.85);
                stemOuter.curveTo(x + w * 0.64, y + h * 0.75, x + w * 0.68, y + h * 0.65, x + w * 0.64, y + h * 0.45);
                stemOuter.closePath();
                g2.fill(stemOuter);

                // Ruột nấm tím đậm độc tố
                g2.setColor(new Color(75, 0, 130)); 
                Path2D.Double stemInner = new Path2D.Double();
                stemInner.moveTo(x + w * 0.40, y + h * 0.45);
                stemInner.curveTo(x + w * 0.37, y + h * 0.65, x + w * 0.41, y + h * 0.75, x + w * 0.39, y + h * 0.82);
                stemInner.lineTo(x + w * 0.61, y + h * 0.82);
                stemInner.curveTo(x + w * 0.59, y + h * 0.75, x + w * 0.63, y + h * 0.65, x + w * 0.60, y + h * 0.45);
                stemInner.closePath();
                g2.fill(stemInner);

                // Tán nấm khổng lồ màu tím sáng
                g2.setColor(new Color(148, 0, 211));
                Path2D.Double hat = new Path2D.Double();
                hat.moveTo(x + w * 0.35, y + h * 0.48);
                hat.curveTo(x - w * 0.02, y + h * 0.45, x - w * 0.02, y + h * 0.20, x + w * 0.20, y + h * 0.18);
                hat.curveTo(x + w * 0.15, y + h * 0.02, x + w * 0.50, y + h * 0.02, x + w * 0.50, y + h * 0.10);
                hat.curveTo(x + w * 0.50, y + h * 0.02, x + w * 0.85, y + h * 0.02, x + w * 0.80, y + h * 0.18);
                hat.curveTo(x + w * 1.02, y + h * 0.20, x + w * 1.02, y + h * 0.45, x + w * 0.65, y + h * 0.48);
                hat.closePath();
                g2.fill(hat);

            } else {
                // === PHIÊN BẢN PLAYER (MÀU ĐỎ CAM/VÀNG CỔ ĐIỂN) ===
                // Thân nấm viền vàng dày bao quanh
                g2.setColor(new Color(254, 201, 62)); 
                Path2D.Double stemOuter = new Path2D.Double();
                stemOuter.moveTo(x + w * 0.36, y + h * 0.45);
                stemOuter.curveTo(x + w * 0.32, y + h * 0.65, x + w * 0.36, y + h * 0.75, x + w * 0.34, y + h * 0.85);
                stemOuter.lineTo(x + w * 0.66, y + h * 0.85);
                stemOuter.curveTo(x + w * 0.64, y + h * 0.75, x + w * 0.68, y + h * 0.65, x + w * 0.64, y + h * 0.45);
                stemOuter.closePath();
                g2.fill(stemOuter);

                // Ruột đỏ cam chồng lên
                g2.setColor(new Color(255, 96, 92)); 
                Path2D.Double stemInner = new Path2D.Double();
                stemInner.moveTo(x + w * 0.40, y + h * 0.45);
                stemInner.curveTo(x + w * 0.37, y + h * 0.65, x + w * 0.41, y + h * 0.75, x + w * 0.39, y + h * 0.82);
                stemInner.lineTo(x + w * 0.61, y + h * 0.82);
                stemInner.curveTo(x + w * 0.59, y + h * 0.75, x + w * 0.63, y + h * 0.65, x + w * 0.60, y + h * 0.45);
                stemInner.closePath();
                g2.fill(stemInner);

                // Tán nấm khổng lồ màu vàng rực rỡ
                g2.setColor(new Color(254, 201, 62));
                Path2D.Double hat = new Path2D.Double();
                hat.moveTo(x + w * 0.35, y + h * 0.48);
                hat.curveTo(x - w * 0.02, y + h * 0.45, x - w * 0.02, y + h * 0.20, x + w * 0.20, y + h * 0.18);
                hat.curveTo(x + w * 0.15, y + h * 0.02, x + w * 0.50, y + h * 0.02, x + w * 0.50, y + h * 0.10);
                hat.curveTo(x + w * 0.50, y + h * 0.02, x + w * 0.85, y + h * 0.02, x + w * 0.80, y + h * 0.18);
                hat.curveTo(x + w * 1.02, y + h * 0.20, x + w * 1.02, y + h * 0.45, x + w * 0.65, y + h * 0.48);
                hat.closePath();
                g2.fill(hat);
            }

            // Chi tiết vệt sáng trắng highlight trên tán nấm (Dùng chung cho cả 2 bên)
            g2.setColor(new Color(255, 255, 255, 180));
            g2.setStroke(new BasicStroke((float)(w * 0.03), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc((int)(x + w * 0.25), (int)(y + h * 0.08), (int)(w * 0.2), (int)(h * 0.06), 45, 120);
            g2.drawArc((int)(x + w * 0.80), (int)(y + h * 0.28), (int)(w * 0.08), (int)(h * 0.12), -45, 110);
            g2.drawArc((int)(x + w * 0.12), (int)(y + h * 0.38), (int)(w * 0.08), (int)(h * 0.06), 135, 110);

            // Bụi khói đen cuộn dày ở chân nấm đáy
            g2.setColor(new Color(63, 64, 65));
            Path2D.Double smokeBase = new Path2D.Double();
            smokeBase.moveTo(x + w * 0.05, y + h * 0.88);
            smokeBase.curveTo(x + w * 0.02, y + h * 0.78, x + w * 0.22, y + h * 0.75, x + w * 0.25, y + h * 0.80);
            smokeBase.curveTo(x + w * 0.22, y + h * 0.68, x + w * 0.48, y + h * 0.68, x + w * 0.48, y + h * 0.80);
            smokeBase.curveTo(x + w * 0.48, y + h * 0.70, x + w * 0.75, y + h * 0.70, x + w * 0.75, y + h * 0.80);
            smokeBase.curveTo(x + w * 0.78, y + h * 0.75, x + w * 0.98, y + h * 0.78, x + w * 0.95, y + h * 0.88);
            smokeBase.lineTo(x + w * 0.95, y + h * 0.92);
            smokeBase.curveTo(x + w * 0.95, y + h * 0.95, x + w * 0.05, y + h * 0.95, x + w * 0.05, y + h * 0.92);
            smokeBase.closePath();
            g2.fill(smokeBase);

        } else {
            // =================================================================
            // CÁC TIA TỎA RANG XUNG QUANH (HORIZONTAL, VERTICAL, END)
            // =================================================================
            Path2D.Double sideFlame = new Path2D.Double();
            sideFlame.moveTo(x + w * 0.5, y + h * 0.05);
            sideFlame.quadTo(x + w * 0.75, y + h * 0.35, x + w * 0.65, y + h * 0.45);
            sideFlame.quadTo(x + w * 0.85, y + h * 0.55, x + w * 0.75, y + h * 0.85);
            sideFlame.quadTo(x + w * 0.5, y + h * 1.0, x + w * 0.25, y + h * 0.85);
            sideFlame.quadTo(x + w * 0.1, y + h * 0.5, x + w * 0.35, y + h * 0.35);
            sideFlame.quadTo(x + w * 0.4, y + h * 0.2, x + w * 0.5, y + h * 0.05);
            sideFlame.closePath();

            Path2D.Double sideInner = new Path2D.Double();
            sideInner.moveTo(x + w * 0.5, y + h * 0.3);
            sideInner.quadTo(x + w * 0.65, y + h * 0.5, x + w * 0.55, y + h * 0.7);
            sideInner.quadTo(x + w * 0.5, y + h * 0.85, x + w * 0.45, y + h * 0.7);
            sideInner.quadTo(x + w * 0.35, y + h * 0.5, x + w * 0.5, y + h * 0.3);
            sideInner.closePath();

            GradientPaint gradOuter;
            GradientPaint gradInner;

            if (isBossFlame) {
                // Lửa Boss: Tím hồng
                gradOuter = new GradientPaint(x, y, new Color(148, 0, 211), x, y + h, new Color(75, 0, 130));
                gradInner = new GradientPaint(x, y + h * 0.4f, new Color(255, 105, 180), x, y + h, new Color(255, 0, 255));
            } else {
                // Lửa Player: Cam đỏ truyền thống
                gradOuter = new GradientPaint(x, y, new Color(255, 69, 0), x, y + h, new Color(255, 99, 71));
                gradInner = new GradientPaint(x, y + h * 0.4f, new Color(255, 215, 0), x, y + h, new Color(255, 140, 0));
            }

            g2.setPaint(gradOuter);
            g2.fill(sideFlame);

            g2.setPaint(gradInner);
            g2.fill(sideInner);
        }

        g2.dispose();
        return true;
    }
}