package com.arkanoid;

import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

public class StrongBall extends PowerUp {

    public StrongBall(double x, double y) {
        super(x, y, 10, 2500, 5);
        super.color = Color.rgb(0, 0, 0);
        Color lighter = color.brighter();
        Color darker = color.darker();

        Stop[] stops = new Stop[] {
                new Stop(0, lighter),
                new Stop(0.5, color),
                new Stop(1, darker)
        };
        super.gradient = new RadialGradient(0, 0, 0.3, 0.3, 0.5, true, CycleMethod.NO_CYCLE, stops);
    }

    public void applyEffect(GameEngine gameEngine) {
        if (!isApplied()) {
            for (Ball ball : gameEngine.balls) {
                ball.setStrong(true);
            }

            super.setApplied(true);
            super.setStartTime();
        }
    }

    public void removeEffect(GameEngine gameEngine) {
        if (isApplied() && getExpired()) {
            for (Ball ball : gameEngine.balls) {
                ball.setStrong(false);
            }

            super.setApplied(false);
        }
    }
}
