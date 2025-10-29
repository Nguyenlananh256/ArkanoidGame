package com.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.effect.DropShadow;

import java.util.List;

public class Brick {
    private double x;
    private double y;
    private double width;
    private double height;
    private Color color;
    private LinearGradient gradient;
    public int hitPoints;
    private boolean destroyed;
    private int points;

    public Brick(double x, double y, double width, double height, Color color, int points) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
        this.hitPoints = 1;
        this.destroyed = false;
        this.points = points;

        Color lighter = color.brighter();
        Color darker = color.darker();

        Stop[] stops = new Stop[] {
            new Stop(0, lighter),
            new Stop(0.5, color),
            new Stop(1, darker)
        };
        this.gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
    }

    public void draw(GraphicsContext gc) {
        if (isDestroyed()) return;

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.4));
        shadow.setRadius(5);
        shadow.setOffsetX(2);
        shadow.setOffsetY(2);

        gc.setEffect(shadow);
        gc.setFill(gradient);
        gc.fillRoundRect(x, y, width, height, 8, 8);

        gc.setStroke(color.brighter().brighter());
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, width, height, 8, 8);
        gc.setEffect(null);
    }

    public boolean checkCollision(Ball ball) {
        if (isDestroyed()) return false;

        double ballX = ball.getX();
        double ballY = ball.getY();
        double radius = ball.getRadius();

        if (ballX + radius > x && ballX - radius < x + width &&
            ballY + radius > y && ballY - radius < y + height) {
            return true;
        }
        return false;
    }

    public boolean isDestroyed() { return hitPoints <= 0; }

    public void takeHit(List<Brick> bricks, int col, int row) {
        hitPoints = 0;
    }

    public int getPoints() { return points; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
}

class StrongBrick extends Brick {
    public StrongBrick(double x, double y, double width, double height, Color color, int points) {
        super(x, y, width, height, color, points);
        super.hitPoints = 3;
    }

    public void takeHit(List<Brick> bricks, int col, int row) {
        hitPoints--;
    }
}

class SilverBrick extends Brick{

    public SilverBrick(double x, double y, double width, double height, Color color, int points) {
        super(x, y, width, height, color, points);
    }

    public boolean isDestroyed() { return false; }
    public void takeHit(List<Brick> bricks, int col, int row) {}

}

class BombBrick extends Brick{
    public BombBrick(double x, double y, double width, double height, Color color, int points) {
        super(x, y, width, height, color, points);
    }

    public void takeHit(List<Brick> bricks, int row, int col) {
        hitPoints--;
        int index = bricks.indexOf(this);
        int size = bricks.size();

        if (index % col != 0) {
            Brick left = bricks.get(index - 1);
            if (!left.isDestroyed()) {
                left.takeHit(bricks, row, col);
            }
        }

        if (index % col != col - 1) {
            Brick right = bricks.get(index + 1);
            if (!right.isDestroyed()) {
                right.takeHit(bricks, row, col);
            }
        }

        if (index >= col) {
            Brick up = bricks.get(index - col);
            if (!up.isDestroyed()) {
                up.takeHit(bricks, row, col);
            }

            if (index % col != 0) {
                Brick leftup = bricks.get(index - col - 1);
                if (!leftup.isDestroyed()) {
                    leftup.takeHit(bricks, row, col);
                }
            }

            if (index % col != col - 1) {
                Brick rightup = bricks.get(index - col + 1);
                if (!rightup.isDestroyed()) {
                    rightup.takeHit(bricks, row, col);
                }
            }
        }

        if (index < size - col) {
            Brick down = bricks.get(index + col);
            if (!down.isDestroyed()) {
                down.takeHit(bricks, row, col);
            }

            if (index % col != 0) {
                Brick leftdown = bricks.get(index + col - 1);
                if (!leftdown.isDestroyed()) {
                    leftdown.takeHit(bricks, row, col);
                }
            }

            if (index % col != col - 1) {
                Brick rightdown = bricks.get(index + col + 1);
                if (!rightdown.isDestroyed()) {
                    rightdown.takeHit(bricks, row, col);
                }
            }
        }
    }
}
