package model;

import java.awt.Graphics;

public class Flame extends GameObject {
    private long idCreatedTime;
    private final long duration = 500; // Ngọn lửa tồn tại trong 500ms

    // Thay đổi constructor để khớp hoàn toàn với lớp cha GameObject
    public Flame(double X, double Y, int width, int height, IdObject id) {
        super(X, Y, width, height, id); // Truyền đủ 5 tham số lên lớp cha
        this.idCreatedTime = System.currentTimeMillis();
    }

    // Kiểm tra xem tia lửa này đã hết thời gian hiển thị chưa để xóa
    public boolean isExpired() {
        return System.currentTimeMillis() - idCreatedTime >= duration;
    }

    // BẮT BUỘC: Triển khai hàm update từ lớp cha abstract
    @Override
    public boolean update() {
        return true;
    }

    // BẮT BUỘC: Triển khai hàm render từ lớp cha abstract
    @Override
    public boolean render(Graphics g) {
        return true;
    }
}