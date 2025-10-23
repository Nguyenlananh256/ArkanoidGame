package com.example.akn;

import javafx.scene.paint.Color;

public class StrongBrick extends Brick {

    public StrongBrick(double x, double y, double width, double height, Color color, int points) {
        super(x, y, width, height, color, points);
        super.hitPoints = 3;
    }

}
