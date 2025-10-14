package com.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.effect.DropShadow;

public class Brick {
    private double x;
    private double y;
    private double width;
    private double height;
    private Color color;
    private LinearGradient gradient;
    private boolean destroyed;
    private int points;
    
    public Brick(double x, double y, double width, double height, Color color, int points) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
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
        if (destroyed) return;
        
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
        if (destroyed) return false;
        
        double ballX = ball.getX();
        double ballY = ball.getY();
        double radius = ball.getRadius();
        
        if (ballX + radius > x && ballX - radius < x + width &&
            ballY + radius > y && ballY - radius < y + height) {
            destroyed = true;
            return true;
        }
        return false;
    }
    
    public boolean isDestroyed() { return destroyed; }
    public int getPoints() { return points; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
}
