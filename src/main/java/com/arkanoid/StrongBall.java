package com.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class StrongBall extends PowerUp {

    public StrongBall(double x, double y) {
        super(x, y, GameConstants.PU_RADIUS, GameConstants.SB_DURATION, GameConstants.SB_TYPE);
    }

    public void draw(GraphicsContext gc) {
        if (isDestroyed()) {
            return;
        }
        gc.save();
        Image img = new Image(getClass().getResourceAsStream(GameConstants.SB_PATH));
        gc.drawImage(img, x - radius, y - radius, radius * 2, radius * 2);
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
