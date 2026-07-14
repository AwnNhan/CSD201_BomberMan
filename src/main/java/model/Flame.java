package model;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

public class Flame extends GameObject {
    private final long createTime;
    private final long duration = 500; 
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

        // Bật khử răng cưa siêu mịn
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Hiệu ứng phai màu theo thời gian
        long elapsed = System.currentTimeMillis() - createTime;
        float alpha = 1.0f - ((float) elapsed / duration);
        if (alpha < 0) alpha = 0;
        if (alpha > 1) alpha = 1;
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        int x = (int) getX();
        int y = (int) getY();
        int w = getWidth();
        int h = getHeight();

        if ("CENTER".equals(type)) {
            // ==========================================
            // TÁI TẠO CHUẨN ĐÁM MÂY NẤM HẠT NHÂN (CENTER)
            // ==========================================

            // LỚP 1: THÂN NẤM (Màu đỏ cam phẳng, viền vàng dày bao quanh)
            // Vẽ viền vàng trước (bằng cách vẽ thân nấm to hơn một chút)
            g2.setColor(new Color(254, 201, 62)); // Màu vàng nấm
            Path2D.Double stemOuter = new Path2D.Double();
            stemOuter.moveTo(x + w * 0.36, y + h * 0.45);
            stemOuter.curveTo(x + w * 0.32, y + h * 0.65, x + w * 0.36, y + h * 0.75, x + w * 0.34, y + h * 0.85);
            stemOuter.lineTo(x + w * 0.66, y + h * 0.85);
            stemOuter.curveTo(x + w * 0.64, y + h * 0.75, x + w * 0.68, y + h * 0.65, x + w * 0.64, y + h * 0.45);
            stemOuter.closePath();
            g2.fill(stemOuter);

            // Vẽ ruột đỏ cam chồng lên bên trong để lộ ra viền vàng hai bên
            g2.setColor(new Color(255, 96, 92)); // Màu đỏ cam
            Path2D.Double stemInner = new Path2D.Double();
            stemInner.moveTo(x + w * 0.40, y + h * 0.45);
            stemInner.curveTo(x + w * 0.37, y + h * 0.65, x + w * 0.41, y + h * 0.75, x + w * 0.39, y + h * 0.82);
            stemInner.lineTo(x + w * 0.61, y + h * 0.82);
            stemInner.curveTo(x + w * 0.59, y + h * 0.75, x + w * 0.63, y + h * 0.65, x + w * 0.60, y + h * 0.45);
            stemInner.closePath();
            g2.fill(stemInner);

            // Highlight màu trắng nhỏ trên thân nấm
            g2.setColor(new Color(255, 255, 255, 200));
            g2.fillRoundRect((int)(x + w * 0.42), (int)(y + h * 0.53), (int)(w * 0.03), (int)(h * 0.1), 4, 4);


            // LỚP 2: TÁN NẤM KHỔNG LỒ PHÍA TRÊN (Màu vàng - #FEC93E)
            g2.setColor(new Color(254, 201, 62));
            
            // Dựng hình khối mây bông xốp bằng các đường cong liên tiếp giống hệt ảnh
            Path2D.Double hat = new Path2D.Double();
            hat.moveTo(x + w * 0.35, y + h * 0.48);
            // Vòng cung bên trái dưới
            hat.curveTo(x - w * 0.02, y + h * 0.45, x - w * 0.02, y + h * 0.20, x + w * 0.20, y + h * 0.18);
            // Vòng cung đỉnh trái
            hat.curveTo(x + w * 0.15, y + h * 0.02, x + w * 0.50, y + h * 0.02, x + w * 0.50, y + h * 0.10);
            // Vòng cung đỉnh phải
            hat.curveTo(x + w * 0.50, y + h * 0.02, x + w * 0.85, y + h * 0.02, x + w * 0.80, y + h * 0.18);
            // Vòng cung bên phải dưới
            hat.curveTo(x + w * 1.02, y + h * 0.20, x + w * 1.02, y + h * 0.45, x + w * 0.65, y + h * 0.48);
            hat.closePath();
            g2.fill(hat);

            // Các đường nét đứt/bóng sáng (Highlight) màu trắng trên tán nấm vàng
            g2.setColor(new Color(255, 255, 255, 180));
            g2.setStroke(new BasicStroke((float)(w * 0.03), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc((int)(x + w * 0.25), (int)(y + h * 0.08), (int)(w * 0.2), (int)(h * 0.06), 45, 120);
            g2.drawArc((int)(x + w * 0.80), (int)(y + h * 0.28), (int)(w * 0.08), (int)(h * 0.12), -45, 110);
            g2.drawArc((int)(x + w * 0.12), (int)(y + h * 0.38), (int)(w * 0.08), (int)(h * 0.06), 135, 110);


            // LỚP 3: ĐÁM MÂY KHÓI ĐEN CUỘN Ở ĐÁY (Màu xám đen phẳng - #3F4041)
            g2.setColor(new Color(63, 64, 65));
            Path2D.Double smokeBase = new Path2D.Double();
            smokeBase.moveTo(x + w * 0.05, y + h * 0.88);
            // Cuộn khói trái ngoài
            smokeBase.curveTo(x + w * 0.02, y + h * 0.78, x + w * 0.22, y + h * 0.75, x + w * 0.25, y + h * 0.80);
            // Cuộn khói giữa trái
            smokeBase.curveTo(x + w * 0.22, y + h * 0.68, x + w * 0.48, y + h * 0.68, x + w * 0.48, y + h * 0.80);
            // Cuộn khói giữa phải
            smokeBase.curveTo(x + w * 0.48, y + h * 0.70, x + w * 0.75, y + h * 0.70, x + w * 0.75, y + h * 0.80);
            // Cuộn khói phải ngoài
            smokeBase.curveTo(x + w * 0.78, y + h * 0.75, x + w * 0.98, y + h * 0.78, x + w * 0.95, y + h * 0.88);
            // Đáy phẳng bo nhẹ góc
            smokeBase.lineTo(x + w * 0.95, y + h * 0.92);
            smokeBase.curveTo(x + w * 0.95, y + h * 0.95, x + w * 0.05, y + h * 0.95, x + w * 0.05, y + h * 0.92);
            smokeBase.closePath();
            g2.fill(smokeBase);

            // Hai nét vẽ trang trí màu xám nhạt nằm trong bụi khói đáy
            g2.setColor(new Color(116, 117, 118));
            g2.setStroke(new BasicStroke((float)(w * 0.025), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc((int)(x + w * 0.18), (int)(y + h * 0.80), (int)(w * 0.08), (int)(h * 0.04), 150, 150);
            g2.drawArc((int)(x + w * 0.58), (int)(y + h * 0.76), (int)(w * 0.12), (int)(h * 0.05), 30, 140);

        } else {
            // CÁC Ô TIA LỬA XUNG QUANH (Giữ nguyên tia lửa đứng/ngang ban đầu của bạn)
            Path2D.Double outerFlame = new Path2D.Double();
            outerFlame.moveTo(x + w * 0.5, y + h * 0.05);
            outerFlame.quadTo(x + w * 0.75, y + h * 0.35, x + w * 0.65, y + h * 0.45);
            outerFlame.quadTo(x + w * 0.85, y + h * 0.55, x + w * 0.75, y + h * 0.85);
            outerFlame.quadTo(x + w * 0.5, y + h * 1.0, x + w * 0.25, y + h * 0.85);
            outerFlame.quadTo(x + w * 0.1, y + h * 0.5, x + w * 0.35, y + h * 0.35);
            outerFlame.quadTo(x + w * 0.4, y + h * 0.2, x + w * 0.5, y + h * 0.05);
            outerFlame.closePath();

            g2.setColor(new Color(139, 35, 10)); // Màu lửa sẫm của bạn
            g2.fill(outerFlame);
            
            Path2D.Double innerFlame = new Path2D.Double();
            innerFlame.moveTo(x + w * 0.5, y + h * 0.3);
            innerFlame.quadTo(x + w * 0.65, y + h * 0.5, x + w * 0.55, y + h * 0.7);
            innerFlame.quadTo(x + w * 0.5, y + h * 0.85, x + w * 0.45, y + h * 0.7);
            innerFlame.quadTo(x + w * 0.35, y + h * 0.5, x + w * 0.5, y + h * 0.3);
            innerFlame.closePath();
            
            g2.setColor(new Color(210, 105, 30));
            g2.fill(innerFlame);
        }

        g2.dispose();
        return true;
    }
}