package model;

import java.awt.Graphics;

public class Bomb extends GameObject {
    private long timeToExplode; // Thời gian hệ thống (ms) lúc quả bom sẽ nổ

    // Thay đổi constructor để khớp hoàn toàn với lớp cha GameObject
    public Bomb(double X, double Y, int width, int height, IdObject id, long timeToExplode) {
        super(X, Y, width, height, id); // Truyền đủ 5 tham số lên lớp cha
        this.timeToExplode = timeToExplode;
    }

    public long getTimeToExplode() {
        return timeToExplode;
    }

    // BẮT BUỘC: Triển khai hàm update từ lớp cha abstract
    @Override
    public boolean update() {
        // Hiện tại bom chưa cần xử lý di chuyển gì trong hàm update
        return true;
    }

    // BẮT BUỘC: Triển khai hàm render từ lớp cha abstract
    @Override
    public boolean render(Graphics g) {
        // Hàm này tạm thời để trống hoặc nhóm bạn gọi vẽ hình ảnh sau
        return true;
    }
    
    
}