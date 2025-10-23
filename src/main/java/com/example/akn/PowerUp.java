package com.example.akn;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.*;

import java.util.Random;

public abstract class PowerUp {

    private double x;
    private double y;
    private double radius;
    private final long duration;
    private boolean expired;
    private boolean destroyed;
    private boolean isApplied;
    long startTime;

    public Color color;
    public RadialGradient gradient;

    public PowerUp(double x, double y, double radius, long duration) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.duration = duration;
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

        else return null;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getRadius() {
        return radius;
    }

    public long getDuration() {
        return duration;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public boolean getExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public boolean getIsApplied() {
        return isApplied;
    }

    public void setApplied(boolean applied) {
        isApplied = applied;
    }

    public void update(double height, GameEngine gameEngine) {
        y += 2;
        if (y + radius > height) {
            destroyed = true;
        }

        if (isApplied && System.currentTimeMillis() - startTime > getDuration()) {
            removeEffect(gameEngine);
            setExpired(true);
        }
    }

    public void draw(GraphicsContext gc) {
        if (isDestroyed()) return;

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        shadow.setRadius(10);
        shadow.setOffsetX(3);
        shadow.setOffsetY(3);

        gc.setEffect(shadow);
        gc.setFill(gradient);
        gc.fillOval(getX() - getRadius(), getY() - getRadius(), getRadius() * 2, getRadius() * 2);
        gc.setEffect(null);
    }

    public boolean checkCollision(Paddle paddle) {
        double paddleX = paddle.getX();
        double paddleY = paddle.getY();
        double paddleW = paddle.getWidth();
        double paddleH = paddle.getHeight();
        if (x + radius > paddleX && x - radius < paddleX + paddleW &&
                y + radius > paddleY && y - radius < y + paddleH) {
            destroyed = true;
            return true;
        }
        return false;
    }

    public abstract void applyEffect(GameEngine gameEngine);
    public abstract void removeEffect(GameEngine gameEngine);

}
