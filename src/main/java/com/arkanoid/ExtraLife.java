package com.arkanoid;

import javafx.scene.canvas.GraphicsContext;

import static com.arkanoid.GameConstants.EL_IMG;

public class ExtraLife extends PowerUp {

    public ExtraLife(double x, double y) {
        super(x, y, GameConstants.PU_RADIUS, GameConstants.NO_DURATION, GameConstants.EL_TYPE);
    }

    public void draw(GraphicsContext gc) {
        if (isDestroyed()) return;
        gc.save();
        //Image img = new Image(getClass().getResourceAsStream(GameConstants.EL_PATH));
        gc.drawImage(EL_IMG, x - radius, y - radius, radius * 2, radius * 2);
    }

    public void applyEffect(GameEngine gameEngine) {
        if (!isApplied()) {
            gameEngine.addLive();
        }
    }

    public void removeEffect(GameEngine gameEngine) {}
}
