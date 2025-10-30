package com.arkanoid;

import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

public class FastBall extends PowerUp {

    private double normalSpeed;
    private double newSpeed;

    public FastBall(double x, double y) {
        super(x, y, 10, 10000, 2);
        super.color = Color.rgb(255, 100, 100);
        Color lighter = color.brighter();
        Color darker = color.darker();
        this.normalSpeed = 1;
        this.newSpeed = 1.5;

        Stop[] stops = new Stop[] {
                new Stop(0, lighter),
                new Stop(0.5, color),
                new Stop(1, darker)
        };
        super.gradient = new RadialGradient(0, 0, 0.3, 0.3, 0.5, true, CycleMethod.NO_CYCLE, stops);

    }

    @Override
    public void applyEffect(GameEngine gameEngine) {
        if (!isApplied()) {
            for (Ball ball:gameEngine.balls) {
                ball.setSpeed(newSpeed);
            }
            super.setApplied(true);
            super.setStartTime();
        }
    }

    @Override
    public void removeEffect(GameEngine gameEngine) {
        if (isApplied() && getExpired()) {
            for (Ball ball:gameEngine.balls) {
                ball.setSpeed(normalSpeed);
            }

            super.setApplied(false);
        }
    }
}
