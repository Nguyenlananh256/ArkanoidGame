package com.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class ExtraLife extends PowerUp {

    public ExtraLife(double x, double y) {
        super(x, y, 10, 0, 4);
    }

    public void draw(GraphicsContext gc) {
        if (isDestroyed()) return;
        gc.save();
        Image img = new Image(getClass().getResourceAsStream("/images/ExtraLife.png"));
        gc.drawImage(img, x - getRadius(), y - getRadius(), getRadius() * 2, getRadius() * 2);
    }

    public void applyEffect(GameEngine gameEngine) {
        if (!isApplied()) {
            gameEngine.addLive();
        }
    }

    public void removeEffect(GameEngine gameEngine) {}
}