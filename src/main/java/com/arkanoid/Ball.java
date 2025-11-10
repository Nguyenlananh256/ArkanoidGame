package com.arkanoid;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

import java.util.ArrayDeque;
import java.util.Deque;

public class Ball extends GameObject {
    // Chuyển động và trạng thái
    private double dx;
    private double dy;
    private double radius;
    private double speed;
    private boolean strong;

    private static final Image BALL_IMG =
            new Image(Ball.class.getResourceAsStream(GameConstants.BALL_IMAGE_PATH),
                    0, 0, true, true);

    // Bật/tắt vẽ bằng ảnh
    private boolean useImage = true;

    // Fallback gradient nếu không dùng ảnh
    private final RadialGradient gradient;

    // Đuôi bóng: lưu lịch sử vị trí (mới nhất ở đầu deque)
    private static final int TRAIL_LEN = 16;
    private static final double TRAIL_BASE_ALPHA = 0.6;
    private static final double TRAIL_SHRINK_FACTOR = 0.9;
    private final Deque<Point2D> trail = new ArrayDeque<>(TRAIL_LEN);

    public Ball(double x, double y, double radius) {
        super(x, y);
        this.radius = radius;
        this.dx = GameConstants.INITIAL_DX;
        this.dy = GameConstants.INITIAL_DY;
        this.speed = GameConstants.BALL_START_SPEED;
        this.strong = false;

        Stop[] stops = new Stop[] {
                new Stop(0.0, Color.rgb(255, 150, 150)),
                new Stop(0.5, Color.rgb(255, 100, 100)),
                new Stop(1.0, Color.rgb(200, 50, 50))
        };
        this.gradient = new RadialGradient(
                0, 0, 0.3, 0.3, 0.5, true, CycleMethod.NO_CYCLE, stops
        );

        // Trail bắt đầu tại vị trí khởi tạo
        trail.addFirst(new Point2D(x, y));
    }

    // Cập nhật vị trí + bật tường (trái/phải/trần). Không bật đáy (để GameEngine xử lý trừ mạng).
    public void update(double width, double height) {
        x += dx * speed;
        y += dy * speed;

        if (x - radius <= 0) {
            x = radius;
            dx = -dx;
        } else if (x + radius >= width) {
            x = width - radius;
            dx = -dx;
        }

        if (y - radius <= 0) {
            y = radius;
            dy = -dy;
        }

        // Cập nhật lịch sử vị trí
        trail.addFirst(new Point2D(x, y));
        if (trail.size() > TRAIL_LEN) {
            trail.removeLast();
        }
    }

    public boolean isOutOfBounds(double height) {
        return (y - radius) > height;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.save();

        // Nếu bóng đang đứng yên (khi gắn trên paddle), giữ trail chỉ còn đúng 1 điểm hiện tại
        boolean stationary = Math.abs(dx) + Math.abs(dy) < GameConstants.STATIONARY_THRESHOLD;
        if (stationary) {
            resetTrailToCurrent();
        } else {
            // Vẽ đuôi trước: alpha giảm và kích thước thu nhỏ dần
            int i = 0;
            for (Point2D p : trail) {
                double t = (double) i / TRAIL_LEN;           // 0 -> 1
                double alpha = TRAIL_BASE_ALPHA * (1.0 - t); // mờ dần
                double scale = 1.0 - (1.0 - TRAIL_SHRINK_FACTOR) * t;
                double r = radius * scale;

                gc.setGlobalAlpha(alpha);
                if (useImage && BALL_IMG != null) {
                    gc.drawImage(BALL_IMG, p.getX() - r, p.getY() - r, r * 2, r * 2);
                } else {
                    gc.setFill(gradient);
                    gc.fillOval(p.getX() - r, p.getY() - r, r * 2, r * 2);
                }
                i++;
            }
            gc.setGlobalAlpha(1.0);
        }

        // Vẽ bóng chính (nét nhất)
        if (useImage && BALL_IMG != null) {
            gc.drawImage(BALL_IMG, x - radius, y - radius, radius * 2, radius * 2);
        } else {
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.rgb(0, 0, 0, 0.5));
            shadow.setRadius(GameConstants.BALL_RADIUS);
            shadow.setOffsetX(3);
            shadow.setOffsetY(3);
            gc.setEffect(shadow);

            gc.setFill(gradient);
            gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
            gc.setEffect(null);
        }

        gc.restore();
    }

    // Đồng bộ trail về đúng vị trí hiện tại (dùng khi gắn bóng lên paddle hoặc cần reset)
    public void resetTrailToCurrent() {
        if (trail.size() != 1 || !trail.peekFirst().equals(new Point2D(x, y))) {
            trail.clear();
            trail.addFirst(new Point2D(x, y));
        }
    }

    // Helpers
    public void reverseY() {
        dy = -dy;
    }

    public void reverseX() {
        dx = -dx;
    }

    // Getters / Setters


    public double getDx() {
        return dx;
    }

    public void setDx(double dx) {
        this.dx = dx;
    }

    public double getDy() {
        return dy;
    }

    public void setDy(double dy) {
        this.dy = dy;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public boolean isStrong() {
        return strong;
    }

    public void setStrong(boolean strong) {
        this.strong = strong;
    }

    public boolean isUseImage() {
        return useImage;
    }

    public void setUseImage(boolean useImage) {
        this.useImage = useImage;
    }
}
