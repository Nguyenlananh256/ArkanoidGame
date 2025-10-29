package com.arkanoid;

import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

public class ExtraBall extends PowerUp {

    public ExtraBall(double x, double y) {
        super(x, y, 10, 0, 3);
        super.color = Color.rgb(255, 200, 100);
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
            double x = gameEngine.paddle.getX() + gameEngine.paddle.getWidth() / 2;
            double y = gameEngine.height - 100;

            gameEngine.balls.add(new Ball(x, y, 10));
        }
    }

    public void removeEffect(GameEngine gameEngine) {}
}
