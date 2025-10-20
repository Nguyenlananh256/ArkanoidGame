package arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Brick {
    private double x, y;
    private double width, height;
    private Image image;
    private boolean destroyed;
    private int points;
    private boolean indestructible;

    public Brick(double x, double y, double width, double height, int points, boolean indestructible) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.points = points;
        this.indestructible = indestructible;
        this.destroyed = false;

        if (indestructible)
            this.image = new Image(getClass().getResourceAsStream("/images/tuong_khongvo.png"));
        else
            this.image = new Image(getClass().getResourceAsStream("/images/tuong.png"));
    }

    public void draw(GraphicsContext gc) {
        if (destroyed) return;
        gc.drawImage(image, x, y, width, height);
    }

    public boolean intersectsCircle(double cx, double cy, double r) {
        if (destroyed) return false;
        double closestX = Math.max(x, Math.min(cx, x + width));
        double closestY = Math.max(y, Math.min(cy, y + height));
        double dx = cx - closestX;
        double dy = cy - closestY;
        return dx * dx + dy * dy <= r * r;
    }

    public void destroy() {
        if (!indestructible) {
            destroyed = true;
        }
    }

    public boolean isDestroyed() { return destroyed; }
    public boolean isIndestructible() { return indestructible; }
    public int getPoints() { return points; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
}
