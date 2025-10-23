package com.example.akn;


import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

public class Paddle {
    private double x;
    private double y;
    private double width;
    private double height;
    private double speed;
    private boolean movingLeft;
    private boolean movingRight;
    private LinearGradient gradient;

    public Paddle(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = 6;
        this.movingLeft = false;
        this.movingRight = false;

        Stop[] stops = new Stop[] {
                new Stop(0, Color.rgb(100, 150, 255)),
                new Stop(0.5, Color.rgb(50, 100, 255)),
                new Stop(1, Color.rgb(100, 150, 255))
        };
        this.gradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);
    }

    public void update(double canvasWidth) {
        if (movingLeft && x > 0) {
            x -= speed;
        }
        if (movingRight && x + width < canvasWidth) {
            x += speed;
        }
    }

    public void draw(GraphicsContext gc) {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.6));
        shadow.setRadius(8);
        shadow.setOffsetX(2);
        shadow.setOffsetY(2);

        gc.setEffect(shadow);
        gc.setFill(gradient);
        gc.fillRoundRect(x, y, width, height, 10, 10);

        gc.setStroke(Color.rgb(200, 220, 255));
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, width, height, 10, 10);
        gc.setEffect(null);
    }

    public void setMovingLeft(boolean moving) { this.movingLeft = moving; }
    public void setMovingRight(boolean moving) { this.movingRight = moving; }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public void setHeight(double height) {
        this.height = height;
    }
}
