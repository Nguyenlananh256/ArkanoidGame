package com.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient; // Giữ để các lớp con vẫn gán được super.gradient

import java.util.Random;

public abstract class PowerUp {

    private double x;
    private double y;
    private double radius;
    private int type;
    private final long duration;
    private boolean expired;
    private boolean destroyed;
    private boolean isApplied;
    private long startTime;

    // Giữ để tương thích với các lớp con (ExtraLife, FastBall, ...)
    public Color color;
    public RadialGradient gradient;

    public PowerUp(double x, double y, double radius, long duration, int type) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.duration = duration;
        this.type = type;
        this.expired = false;
        this.destroyed = false;
        this.isApplied = false;
    }

    public static PowerUp randomPowerUp(double x, double y) {
        Random random = new Random();
        int r = random.nextInt(100);
        if (r < 10) return new ExpandPaddle(x, y);
        else if (r < 20) return new FastBall(x, y);
        else if (r < 25) return new ExtraLife(x, y);
        else if (r < 35) return new ExtraBall(x, y);
        else if (r < 40) return new StrongBall(x, y);
        else return null;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getRadius() { return radius; }
    public int getType() { return type; }
    public long getDuration() { return duration; }
    public boolean isDestroyed() { return destroyed; }
    public boolean getExpired() { return expired; }
    public boolean isApplied() { return isApplied; }
    public long getStartTime() { return startTime; }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setExpired(boolean expired) { this.expired = expired; }
    public void setApplied(boolean applied) { isApplied = applied; }
    public void setStartTime() { this.startTime = System.currentTimeMillis(); }

    // Rơi xuống; ra khỏi đáy màn thì hủy
    public void update(double height, GameEngine gameEngine) {
        y += 2;
        if (y + radius > height) {
            destroyed = true;
        }
    }

    // Vẽ hộp quà xanh lá thống nhất (không dùng gradient nữa)
    public void draw(GraphicsContext gc) {
        if (isDestroyed()) return;

        double w = radius * 2;
        double h = radius * 2;
        double lidH = Math.max(6, h * 0.25);
        double arc = Math.min(8, radius * 0.6);

        Color base   = Color.rgb(80, 180, 120);  // thân hộp
        Color ribbon = Color.rgb(30, 140, 70);   // nơ/ruy băng

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.35));
        shadow.setRadius(6);
        shadow.setOffsetX(2);
        shadow.setOffsetY(3);
        gc.setEffect(shadow);

        // Thân hộp
        gc.setFill(base.brighter());
        gc.fillRoundRect(x - w / 2, y - h / 2, w, h, arc, arc);

        // Nắp
        gc.setFill(base.darker());
        gc.fillRoundRect(x - w / 2, y - h / 2, w, lidH, arc, arc);

        // Ruy băng dọc + ngang
        gc.setFill(ribbon);
        gc.fillRect(x - 2, y - h / 2, 4, h);
        gc.fillRect(x - w / 2, y - 1, w, 2);

        // Nơ hai cánh
        gc.setFill(ribbon.brighter());
        double bowW = Math.max(6, w * 0.25);
        double bowH = Math.max(5, lidH * 0.7);
        gc.fillPolygon(new double[]{x - 4, x - 4 - bowW, x - 4},
                new double[]{y - h / 2 + lidH, y - h / 2 + lidH + bowH, y - h / 2 + lidH + bowH}, 3);
        gc.fillPolygon(new double[]{x + 4, x + 4 + bowW, x + 4},
                new double[]{y - h / 2 + lidH, y - h / 2 + lidH + bowH, y - h / 2 + lidH + bowH}, 3);

        // Viền
        gc.setStroke(base.darker().darker());
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(x - w / 2, y - h / 2, w, h, arc, arc);

        gc.setEffect(null);
    }

    // Sửa điều kiện va chạm Y (trước đó nhầm y - radius < y + paddleH)
    public boolean checkCollision(Paddle paddle) {
        double paddleX = paddle.getX();
        double paddleY = paddle.getY();
        double paddleW = paddle.getWidth();
        double paddleH = paddle.getHeight();

        boolean hit =
                (x + radius > paddleX) &&
                        (x - radius < paddleX + paddleW) &&
                        (y + radius > paddleY) &&
                        (y - radius < paddleY + paddleH);

        if (hit) {
            destroyed = true;
            return true;
        }
        return false;
    }

    public abstract void applyEffect(GameEngine gameEngine);
    public abstract void removeEffect(GameEngine gameEngine);
}
