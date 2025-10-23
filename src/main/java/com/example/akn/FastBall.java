package com.example.akn;

import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

public class FastBall extends PowerUp {

    public FastBall(double x, double y) {
        super(x, y, 10, 10000);
        super.color = Color.rgb(255, 100, 100);
        Color lighter = color.brighter();
        Color darker = color.darker();

        Stop[] stops = new Stop[] {
                new Stop(0, lighter),
                new Stop(0.5, color),
                new Stop(1, darker)
        };
        super.gradient = new RadialGradient(0, 0, 0.3, 0.3, 0.5, true, CycleMethod.NO_CYCLE, stops);

    }

    @Override
    public void applyEffect(GameEngine gameEngine) {
        if (!getIsApplied()) {
            for (Ball ball:gameEngine.balls) {
                double newDx = ball.getDx() * 1.5;
                ball.setDx(newDx);
                double newDy = ball.getDy() * 1.5;
                ball.setDy(newDy);
            }
            super.setApplied(true);
            this.startTime = System.currentTimeMillis();
        }
    }

    @Override
    public void removeEffect(GameEngine gameEngine) {
        if (getIsApplied() && getExpired()) {
            for (Ball ball:gameEngine.balls) {
                double preDx = ball.getDx() / 1.5;
                ball.setDx(preDx);
                double preDy = ball.getDy() / 1.5;
                ball.setDy(preDy);
            }
            super.setApplied(false);
        }
    }
}
