package com.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class ExtraBall extends PowerUp {

    public ExtraBall(double x, double y) {
        super(x, y, GameConstants.PU_RADIUS, GameConstants.NO_DURATION, GameConstants.EB_TYPE);
    }

    public void draw(GraphicsContext gc) {
        if (isDestroyed()) return;
        gc.save();
        Image img = new Image(getClass().getResourceAsStream(GameConstants.EB_PATH));
        gc.drawImage(img, x - radius, y - radius, radius * 2, radius * 2);
    }

    public void applyEffect(GameEngine gameEngine) {
        if (!isApplied()) {
            double x = gameEngine.getPaddle().getX() + gameEngine.getPaddle().getWidth() / 2;
            double y = GameConstants.BALL_Y;

            gameEngine.getBalls().add(new Ball(x, y, GameConstants.BALL_RADIUS));
        }
    }

    public void removeEffect(GameEngine gameEngine) {}
}
