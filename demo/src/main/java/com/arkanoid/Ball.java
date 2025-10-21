package com.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

public class Ball {
    private double x;
    private double y;
    private double dx;
    private double dy;
    private double radius;
    private Color color;
    private RadialGradient gradient;

    public Ball(double x, double y, double radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.dx = 6;
        this.dy = -6;
        this.color = Color.rgb(255, 100, 100);

        Stop[] stops = new Stop[] {
            new Stop(0, Color.rgb(255, 150, 150)),
            new Stop(0.5, Color.rgb(255, 100, 100)),
            new Stop(1, Color.rgb(200, 50, 50))
        };
        this.gradient = new RadialGradient(0, 0, 0.3, 0.3, 0.5, true, CycleMethod.NO_CYCLE, stops);
    }

    public void update(double width, double height) {
        x += dx;
        y += dy;

        if (x - radius <= 0 || x + radius >= width) {
            dx = -dx;
            x = Math.max(radius, Math.min(x, width - radius));
        }

        if (y - radius <= 0) {
            dy = -dy;
            y = radius;
        }
    }

    public void draw(GraphicsContext gc) {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        shadow.setRadius(10);
        shadow.setOffsetX(3);
        shadow.setOffsetY(3);

        gc.setEffect(shadow);
        gc.setFill(gradient);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        gc.setEffect(null);
    }

    public void reverseY() {
        dy = -dy;
    }

    public void reverseX() {
        dx = -dx;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getRadius() { return radius; }
    public double getDx() { return dx; }
    public double getDy() { return dy; }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setDy(double dy) { this.dy = dy; }

    public boolean isOutOfBounds(double height) {
        return y - radius > height;
    }
}
