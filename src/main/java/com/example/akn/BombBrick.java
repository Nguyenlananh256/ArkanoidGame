package com.example.akn;

import javafx.scene.paint.Color;

import java.util.List;

public class BombBrick extends Brick{
    public BombBrick(double x, double y, double width, double height, Color color, int points) {
        super(x, y, width, height, color, points);
    }

    public void setDestroyed(List<Brick> bricks, int row, int col) {
        hitPoints--;
        int index = bricks.indexOf(this);
        int size = bricks.size();

        if (index % col != 0) {
            Brick left = bricks.get(index - 1);
            if (!left.isDestroyed()) {
                left.setDestroyed(bricks, row, col);
            }
        }

        if (index % col != col - 1) {
            Brick right = bricks.get(index + 1);
            if (!right.isDestroyed()) {
                right.setDestroyed(bricks, row, col);
            }
        }

        if (index >= col) {
            Brick up = bricks.get(index - col);
            if (!up.isDestroyed()) {
                up.setDestroyed(bricks, row, col);
            }

            if (index % col != 0) {
                Brick leftup = bricks.get(index - col - 1);
                if (!leftup.isDestroyed()) {
                    leftup.setDestroyed(bricks, row, col);
                }
            }

            if (index % col != col - 1) {
                Brick rightup = bricks.get(index - col + 1);
                if (!rightup.isDestroyed()) {
                    rightup.setDestroyed(bricks, row, col);
                }
            }
        }

        if (index < size - col) {
            Brick down = bricks.get(index + col);
            if (!down.isDestroyed()) {
                down.setDestroyed(bricks, row, col);
            }

            if (index % col != 0) {
                Brick leftdown = bricks.get(index + col - 1);
                if (!leftdown.isDestroyed()) {
                    leftdown.setDestroyed(bricks, row, col);
                }
            }

            if (index % col != col - 1) {
                Brick rightdown = bricks.get(index + col + 1);
                if (!rightdown.isDestroyed()) {
                    rightdown.setDestroyed(bricks, row, col);
                }
            }
        }
    }
}
