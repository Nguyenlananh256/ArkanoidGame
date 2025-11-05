package com.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.effect.DropShadow;

import java.util.List;

enum BrickKind { NORMAL, STRONG, SILVER, BOMB }

public class Brick extends GameObject {
    private double width;
    private double height;
    private Color color;
    private LinearGradient gradient;
    public int hitPoints;
    private boolean destroyed;
    private int points;
    private final BrickKind kind;

    public Brick(double x, double y, double width, double height, int points) {
        this(x, y, width, height, BrickKind.NORMAL, points);
    }

    protected Brick(double x, double y, double width, double height, BrickKind kind, int points) {
        super(x, y);
        this.width = width;
        this.height = height;
        this.kind = kind;
        this.points = points;
        this.hitPoints = 1;
        this.destroyed = false;

        this.color = palette(kind);
        Color lighter = color.brighter();
        Color darker = color.darker();
        Stop[] stops = new Stop[] {
                new Stop(0, lighter),
                new Stop(0.5, color),
                new Stop(1, darker)
        };
        this.gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
    }

    private static Color palette(BrickKind kind) {
        switch (kind) {
            case NORMAL: return Color.rgb(86, 156, 214);
            case STRONG: return Color.rgb(220, 80, 90);
            case SILVER: return Color.rgb(180, 180, 185);
            case BOMB:   return Color.rgb(255, 145, 0);
            default:     return Color.GRAY;
        }
    }

    @Override
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
        return (ballX + radius > x && ballX - radius < x + width &&
                ballY + radius > y && ballY - radius < y + height);
    }

    public boolean isDestroyed() { return hitPoints <= 0; }

    // Chuẩn hóa: (rows, cols)
    public void takeHit(List<Brick> bricks, int rows, int cols) {
        hitPoints = 0;
    }

    public int getPoints() { return points; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
}

// Strong: 3 hit
class StrongBrick extends Brick {
    public StrongBrick(double x, double y, double width, double height, int points) {
        super(x, y, width, height, BrickKind.STRONG, points);
        super.hitPoints = 3;
    }
    @Override
    public void takeHit(List<Brick> bricks, int rows, int cols) {
        hitPoints--;
    }
}

// Silver: không vỡ
class SilverBrick extends Brick {
    public SilverBrick(double x, double y, double width, double height, int points) {
        super(x, y, width, height, BrickKind.SILVER, points);
    }
    @Override
    public boolean isDestroyed() { return false; }
    @Override
    public void takeHit(List<Brick> bricks, int rows, int cols) { /* no-op */ }
}

// Bomb: nổ lan 8 hướng dựa trên số cột 'cols'
class BombBrick extends Brick {
    public BombBrick(double x, double y, double width, double height, int points) {
        super(x, y, width, height, BrickKind.BOMB, points);
    }
    @Override
    public void takeHit(List<Brick> bricks, int rows, int cols) {
        hitPoints--;
        int index = bricks.indexOf(this);
        int size = bricks.size();

        // trái
        if (index % cols != 0) {
            Brick left = bricks.get(index - 1);
            if (!left.isDestroyed()) left.takeHit(bricks, rows, cols);
        }
        // phải
        if (index % cols != cols - 1) {
            Brick right = bricks.get(index + 1);
            if (!right.isDestroyed()) right.takeHit(bricks, rows, cols);
        }
        // trên + chéo trên
        if (index >= cols) {
            Brick up = bricks.get(index - cols);
            if (!up.isDestroyed()) up.takeHit(bricks, rows, cols);

            if (index % cols != 0) {
                Brick leftup = bricks.get(index - cols - 1);
                if (!leftup.isDestroyed()) leftup.takeHit(bricks, rows, cols);
            }
            if (index % cols != cols - 1) {
                Brick rightup = bricks.get(index - cols + 1);
                if (!rightup.isDestroyed()) rightup.takeHit(bricks, rows, cols);
            }
        }
        // dưới + chéo dưới
        if (index < size - cols) {
            Brick down = bricks.get(index + cols);
            if (!down.isDestroyed()) down.takeHit(bricks, rows, cols);

            if (index % cols != 0) {
                Brick leftdown = bricks.get(index + cols - 1);
                if (!leftdown.isDestroyed()) leftdown.takeHit(bricks, rows, cols);
            }
            if (index % cols != cols - 1) {
                Brick rightdown = bricks.get(index + cols + 1);
                if (!rightdown.isDestroyed()) rightdown.takeHit(bricks, rows, cols);
            }
        }
    }
}
