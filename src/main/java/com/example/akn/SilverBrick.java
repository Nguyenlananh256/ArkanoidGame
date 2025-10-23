package com.example.akn;

import javafx.scene.paint.Color;

import java.util.List;

public class SilverBrick extends Brick{

    public SilverBrick(double x, double y, double width, double height, Color color, int points) {
        super(x, y, width, height, color, points);
    }

    public boolean isDestroyed() { return false; }
    public void setDestroyed(List<Brick> bricks, int col, int row) {}

}
