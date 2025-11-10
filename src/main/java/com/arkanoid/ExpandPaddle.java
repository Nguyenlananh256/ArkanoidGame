package com.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class ExpandPaddle extends PowerUp {

    private double normalPaddle;
    private double newPaddle;

    public ExpandPaddle(double x, double y) {
        super(x, y, GameConstants.PU_RADIUS, GameConstants.EP_DURATION, GameConstants.EP_TYPE);
        this.normalPaddle = GameConstants.PADDLE_WIDTH;
        this.newPaddle = GameConstants.EXPANDED_PADDLE_WIDTH;
    }

    public void draw(GraphicsContext gc) {
        if (isDestroyed()) {
            return;
        }
        gc.save();
        Image img = new Image(getClass().getResourceAsStream(GameConstants.EP_PATH));
        gc.drawImage(img, x - radius, y - radius, radius * 2, radius * 2);
    }

    @Override
    public void applyEffect(GameEngine gameEngine) {
        if (!isApplied()) {
            gameEngine.getPaddle().setWidth(newPaddle);

            super.setApplied(true);
            super.setStartTime();
        }
    }

    @Override
    public void removeEffect(GameEngine gameEngine) {
        if (isApplied() && getExpired()) {
            gameEngine.getPaddle().setWidth(normalPaddle);

            super.setApplied(false);
        }
    }
}
