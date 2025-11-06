package com.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class FastBall extends PowerUp {

    private double normalSpeed;
    private double newSpeed;

    public FastBall(double x, double y) {
        super(x, y, 10, 10000, 2);
        this.normalSpeed = 1;
        this.newSpeed = 1.25;
    }

    public void draw(GraphicsContext gc) {
        if (isDestroyed()) return;
        gc.save();
        Image img = new Image(getClass().getResourceAsStream("/images/FastBall.png"));
        gc.drawImage(img, x - getRadius(), y - getRadius(), getRadius() * 2, getRadius() * 2);
    }

    @Override
    public void applyEffect(GameEngine gameEngine) {
        if (!isApplied()) {
            for (Ball ball:gameEngine.getBalls()) {
                ball.setSpeed(newSpeed);
            }
            super.setApplied(true);
            super.setStartTime();
        }
    }

    @Override
    public void removeEffect(GameEngine gameEngine) {
        if (isApplied() && getExpired()) {
            for (Ball ball:gameEngine.getBalls()) {
                ball.setSpeed(normalSpeed);
            }

            super.setApplied(false);
        }
    }
}
