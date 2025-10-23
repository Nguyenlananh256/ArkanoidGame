package com.example.akn;

import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

public class ExpandPaddle extends PowerUp {

    public ExpandPaddle(double x, double y) {
        super(x, y, 10, 10000);
        super.color = Color.rgb(255, 150, 100);
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
            double newWidth = gameEngine.paddle.getWidth() * 1.5;
            gameEngine.paddle.setWidth(newWidth);

            super.setApplied(true);
            this.startTime = System.currentTimeMillis();
        }
    }

    @Override
    public void removeEffect(GameEngine gameEngine) {
        if (getIsApplied() && getExpired()) {
            double preWidth = gameEngine.paddle.getWidth() / 1.5;
            gameEngine.paddle.setWidth(preWidth);

            super.setApplied(false);
        }
    }
}
