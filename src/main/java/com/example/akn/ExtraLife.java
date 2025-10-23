package com.example.akn;

import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

public class ExtraLife extends PowerUp {

    public ExtraLife(double x, double y) {
        super(x, y, 10, 0);
        super.color = Color.rgb(150, 255, 150);
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
        if (!getIsApplied()) {
            gameEngine.lives --;
        }
    }

    public void removeEffect(GameEngine gameEngine) {}
}

