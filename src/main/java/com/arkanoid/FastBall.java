package com.arkanoid;

import javafx.scene.canvas.GraphicsContext;

import static com.arkanoid.GameConstants.FB_IMG;

public class FastBall extends PowerUp {

    private double normalSpeed;
    private double newSpeed;

    public FastBall(double x, double y) {
        super(x, y, GameConstants.PU_RADIUS, GameConstants.FB_DURATION, GameConstants.FB_TYPE);
        this.normalSpeed = GameConstants.BALL_START_SPEED;
        this.newSpeed = GameConstants.BALL_FAST_SPEED;
    }

    public void draw(GraphicsContext gc) {
        if (isDestroyed()) return;
        gc.save();
        //Image img = new Image(getClass().getResourceAsStream(GameConstants.FB_PATH));
        gc.drawImage(FB_IMG, x - radius, y - radius, radius * 2, radius * 2);
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
