
package arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Paddle {
    private double x, y, width, height, speed;
    private boolean movingLeft, movingRight;
    private Image image;

    public Paddle(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = 6;
        this.movingLeft = false;
        this.movingRight = false;
        this.image = new Image(getClass().getResourceAsStream("/images/paddle.png"));
    }

    public void update(double canvasWidth) {
        if (movingLeft && x > 0) x -= speed;
        if (movingRight && x + width < canvasWidth) x += speed;
    }

    public void draw(GraphicsContext gc) {
        gc.drawImage(image, x, y, width, height);
    }

    public void setMovingLeft(boolean moving) { this.movingLeft = moving; }
    public void setMovingRight(boolean moving) { this.movingRight = moving; }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
}
