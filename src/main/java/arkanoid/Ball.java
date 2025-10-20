package arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Ball {
    private double x, y;
    private double dx, dy;
    private double radius;
    private Image image;

    public Ball(double x, double y, double radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.dx = 5;
        this.dy = -4;
        this.image = new Image(getClass().getResourceAsStream("/images/ball.png"));
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
        gc.drawImage(image, x - radius, y - radius, radius * 2, radius * 2);
    }

    public void reverseY() { dy = -dy; }
    public void reverseX() { dx = -dx; }

    public void stickToPaddle(Paddle paddle) {
        this.x = paddle.getX() + paddle.getWidth() / 2;
        this.y = paddle.getY() - this.radius - 2;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getRadius() { return radius; }
    public double getDx() { return dx; }
    public double getDy() { return dy; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setDy(double dy) { this.dy = dy; }
    public void setDx(double dx) { this.dx = dx; }

    public boolean isOutOfBounds(double height) {
        return y - radius > height;
    }
}
