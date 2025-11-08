package com.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class ExtraBall extends PowerUp {

    public ExtraBall(double x, double y) {
        super(x, y, 10, 0, 3);
    }

    public void draw(GraphicsContext gc) {
        if (isDestroyed()) return;
        gc.save();
        Image img = new Image(getClass().getResourceAsStream("/images/ExtraBall.png"));
        gc.drawImage(img, x - getRadius(), y - getRadius(), getRadius() * 2, getRadius() * 2);
    }

    public void applyEffect(GameEngine gameEngine) {
        if (!isApplied()) {
            double x = gameEngine.getPaddle().getX() + gameEngine.getPaddle().getWidth() / 2;
            double y = gameEngine.getHeight() - 100;

            gameEngine.getBalls().add(new Ball(x, y, 10));
        }
    }

    public void removeEffect(GameEngine gameEngine) {}
}