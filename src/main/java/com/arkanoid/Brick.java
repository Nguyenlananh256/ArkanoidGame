package com.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.List;

enum BrickKind {
    NORMAL,
    STRONG,
    SILVER,
    BOMB
}

public class Brick extends GameObject {
    private double width;
    private double height;
    public int hitPoints;
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
        this.hitPoints = GameConstants.BRICK_HIT_POINTS;
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (isDestroyed()) return;
        Image img = new Image(getClass().getResourceAsStream(GameConstants.BRICK_PATH));
        gc.drawImage(img, x, y, width, height);
    }

    public boolean isDestroyed() {
        return hitPoints <= 0;
    }

    // Chuẩn hóa: (rows, cols)
    public void takeHit(List<Brick> bricks, int rows, int cols) {
        hitPoints = 0;
    }

    public int getPoints() {
        return points;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}

// Strong: 3 hit
class StrongBrick extends Brick {
    public StrongBrick(double x, double y, double width, double height, int points) {
        super(x, y, width, height, BrickKind.STRONG, points);
        super.hitPoints = GameConstants.STRONGBRICK_HIT_POINTS;
    }

    public void draw(GraphicsContext gc) {
        if (isDestroyed()) return;
        Image img = null;
        if (hitPoints == GameConstants.STRONGBRICK_HIT_POINTS) {
            img = new Image(getClass().getResourceAsStream(GameConstants.STRONGBRICK1_PATH));
        } else if (hitPoints == GameConstants.STRONGBRICK_HIT_POINTS - 1) {
            img = new Image(getClass().getResourceAsStream(GameConstants.STRONGBRICK2_PATH));
        } else if (hitPoints == GameConstants.STRONGBRICK_HIT_POINTS - 2) {
            img = new Image(getClass().getResourceAsStream(GameConstants.STRONGBRICK3_PATH));
        }
        gc.drawImage(img, x, y, getWidth(), getHeight());
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

    public void draw(GraphicsContext gc) {
        if (isDestroyed()) return;
        Image img = new Image(getClass().getResourceAsStream(GameConstants.SILVERBRICK_PATH));
        gc.drawImage(img, x, y, getWidth(), getHeight());
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

    public void draw(GraphicsContext gc) {
        if (isDestroyed()) return;

        Image img = new Image(getClass().getResourceAsStream(GameConstants.BOMBBRICK_PATH));
        gc.drawImage(img, x, y, getWidth(), getHeight());
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
