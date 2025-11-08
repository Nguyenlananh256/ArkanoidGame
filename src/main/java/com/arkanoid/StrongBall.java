package com.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class StrongBall extends PowerUp {

    public StrongBall(double x, double y) {
        super(x, y, 10, 2500, 5);
    }

    public void draw(GraphicsContext gc) {
        if (isDestroyed()) return;
        gc.save();
        Image img = new Image(getClass().getResourceAsStream("/images/StrongBall.png"));
        gc.drawImage(img, x - getRadius(), y - getRadius(), getRadius() * 2, getRadius() * 2);
    }

    public void applyEffect(GameEngine gameEngine) {
        if (!isApplied()) {
            for (Ball ball : gameEngine.getBalls()) {
                ball.setStrong(true);
            }

            super.setApplied(true);
            super.setStartTime();
        }
    }

    public void removeEffect(GameEngine gameEngine) {
        if (isApplied() && getExpired()) {
            for (Ball ball : gameEngine.getBalls()) {
                ball.setStrong(false);
            }

            super.setApplied(false);
        }
    }
}