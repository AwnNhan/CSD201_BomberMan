package model;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

public class Flame extends GameObject {
    private final long createTime;
    private final long duration = 500; // Thời gian lửa tồn tại (ms)
    private final String type; // "CENTER", "HORIZONTAL", "VERTICAL", "END"

    public Flame(double X, double Y, int width, int height, IdObject id, String type) {
        super(X, Y, width, height, id);
        this.createTime = System.currentTimeMillis();
        this.type = type;
    }

    @Override
    public boolean update() {
        return true;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - createTime >= duration;
    }

    @Override
    public boolean render(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        // 1. Kích hoạt khử răng cưa giúp các đường cong ngọn lửa mịn màng, không bị răng cưa mờ mờ
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        long elapsed = System.currentTimeMillis() - createTime;
        float alpha = 1.0f - ((float) elapsed / duration);
        if (alpha < 0) alpha = 0;
        if (alpha > 1) alpha = 1;

        // Áp dụng hiệu ứng phai màu theo thời gian
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        int x = (int) getX();
        int y = (int) getY();
        int w = getWidth();
        int h = getHeight();

        // --- VẼ NGỌN LỬA GIỐNG IMAGE_FA2005.PNG BẰNG PATH ---
        
        // Tạo hình dáng ngọn lửa lớn phía sau (Lớp Đỏ - Cam)
        Path2D.Double outerFlame = new Path2D.Double();
        outerFlame.moveTo(x + w * 0.5, y + h * 0.05); // Đỉnh nhọn ngọn lửa
        // Uốn lượn mạn phải
        outerFlame.quadTo(x + w * 0.75, y + h * 0.35, x + w * 0.65, y + h * 0.45);
        outerFlame.quadTo(x + w * 0.85, y + h * 0.55, x + w * 0.75, y + h * 0.85);
        // Bo tròn phần đáy giống hình ảnh
        outerFlame.quadTo(x + w * 0.5, y + h * 1.0, x + w * 0.25, y + h * 0.85);
        // Uốn lượn mạn trái quay về đỉnh
        outerFlame.quadTo(x + w * 0.1, y + h * 0.5, x + w * 0.35, y + h * 0.35);
        outerFlame.quadTo(x + w * 0.4, y + h * 0.2, x + w * 0.5, y + h * 0.05);
        outerFlame.closePath();

        // Tạo hình dáng ngọn lửa nhỏ sáng hơn ở phía trước (Lớp Lõi Vàng)
        Path2D.Double innerFlame = new Path2D.Double();
        innerFlame.moveTo(x + w * 0.5, y + h * 0.4); // Đỉnh lõi vàng
        innerFlame.quadTo(x + w * 0.65, y + h * 0.55, x + w * 0.6, y + h * 0.65);
        innerFlame.quadTo(x + w * 0.7, y + h * 0.75, x + w * 0.65, y + h * 0.85);
        innerFlame.quadTo(x + w * 0.5, y + h * 0.98, x + w * 0.35, y + h * 0.85);
        innerFlame.quadTo(x + w * 0.3, y + h * 0.7, x + w * 0.4, y + h * 0.6);
        innerFlame.quadTo(x + w * 0.45, y + h * 0.5, x + w * 0.5, y + h * 0.4);
        innerFlame.closePath();

        // 2. Đổ màu Gradient chuyển sắc từ đỏ cam (đỉnh) sang cam vàng rực (đáy) cho lớp ngoài
        GradientPaint gradOuter = new GradientPaint(x, y, new Color(255, 69, 0), x, y + h, new Color(255, 99, 71));
        g2.setPaint(gradOuter);
        g2.fill(outerFlame);

        // 3. Đổ màu Gradient sáng rực (Vàng chanh -> Cam sáng) cho lớp lõi phía trước
        GradientPaint gradInner = new GradientPaint(x, y + h * 0.4f, new Color(255, 215, 0), x, y + h, new Color(255, 140, 0));
        g2.setPaint(gradInner);
        g2.fill(innerFlame);

        g2.dispose();
        return true;
    }
}