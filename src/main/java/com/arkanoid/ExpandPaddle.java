package com.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class ExpandPaddle extends PowerUp {

    private double normalPaddle;
    private double newPaddle;

    public ExpandPaddle(double x, double y) {
        super(x, y, 10, 10000, 1);
        this.normalPaddle = 120;
        this.newPaddle = 180;
    }

    public void draw(GraphicsContext gc) {
        if (isDestroyed()) return;
        gc.save();
        Image img = new Image(getClass().getResourceAsStream("/images/ExpandPaddle.png"));
        gc.drawImage(img, x - getRadius(), y - getRadius(), getRadius() * 2, getRadius() * 2);
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

