package com.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.*;
import javafx.scene.effect.*;
import java.util.ArrayList;
import java.util.List;

public class Paddle extends GameObject {
    private double width;
    private double height;
    private double velocity = GameConstants.INITIAL_PADDLE_VELOCITY;  // Vận tốc hiện tại
    private double acceleration = GameConstants.ACCELERATION;  // Gia tốc
    private double maxSpeed = GameConstants.MAX_SPEED;  // Tốc độ tối đa
    private double friction = GameConstants.FRICTION;  // Ma sát để dừng mượt
    private boolean movingLeft;
    private boolean movingRight;

    // Hiệu ứng glow
    private double glowIntensity = GameConstants.MIN_GLOW;
    private boolean isGlowing = false;

    // Trail effect
    private List<PaddleTrail> trails = new ArrayList<>();

    public Paddle(double x, double y, double width, double height) {
        super(x, y);
        this.width = width;
        this.height = height;
        this.movingLeft = false;
        this.movingRight = false;
    }

    public void update(double canvasWidth) {
        // Smooth acceleration
        if (movingLeft) {
            velocity -= acceleration;
        } else if (movingRight) {
            velocity += acceleration;
        } else {
            velocity *= friction; // Giảm tốc khi không nhấn phím
        }

        // Giới hạn vận tốc
        velocity = Math.max(-maxSpeed, Math.min(maxSpeed, velocity));

        // Update vị trí
        double newX = x + velocity;

        // Giới hạn trong màn hình với bounce effect
        if (newX < 0) {
            newX = 0;
            velocity *= GameConstants.BOUCE_BACK; // Bounce back effect
        } else if (newX + width > canvasWidth) {
            newX = canvasWidth - width;
            velocity *= GameConstants.BOUCE_BACK;
        }

        // Thêm trail effect khi di chuyển nhanh
        if (Math.abs(velocity) > GameConstants.TRAIL_SPEED_THRESHOLD) {
            trails.add(new PaddleTrail(x, y, width, height, Math.abs(velocity) / maxSpeed));
        }

        x = newX;

        // Update trails
        trails.removeIf(trail -> !trail.update());

        // Update glow effect
        if (isGlowing) {
            glowIntensity = Math.min(1, glowIntensity + GameConstants.GLOW_INC);
        } else {
            glowIntensity = Math.max(0, glowIntensity - GameConstants.GLOW_DEC);
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        // Draw trails first
        for (PaddleTrail trail : trails) {
            trail.draw(gc);
        }
        gc.save();

        // Shadow effect động
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 100, 255, 0.8));
        shadow.setRadius(GameConstants.SHADOW_BASE + glowIntensity * 20);
        shadow.setSpread(GameConstants.SHADOW_SPREAD);
        gc.setEffect(shadow);

        // Gradient động dựa trên velocity
        double hue = GameConstants.HUE_BASE + Math.abs(velocity) * 5;
        Color baseColor = Color.hsb(hue, 0.8, 1.0);
        Color lightColor = baseColor.brighter();
        Color darkColor = baseColor.darker();

        LinearGradient gradient = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, lightColor),
                new Stop(0.3, baseColor),
                new Stop(0.7, baseColor),
                new Stop(1, darkColor)
        );

        // Draw main paddle
        gc.setFill(gradient);
        gc.fillRoundRect(x, y, width, height, GameConstants.PADDLE_CORNER_RADIUS,
                GameConstants.PADDLE_CORNER_RADIUS);

        // Inner glow
        if (glowIntensity > 0) {
            RadialGradient innerGlow = new RadialGradient(
                    0, 0, 0.5, 0.5, 0.8, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(255, 255, 255, glowIntensity * 0.3)),
                    new Stop(1, Color.TRANSPARENT)
            );
            gc.setFill(innerGlow);

            gc.fillRoundRect(x + 5, y + 2, width - 10, height - 4,
                    GameConstants.INNER_CORNER_RADIUS, GameConstants.INNER_CORNER_RADIUS);
        }

        // Border với hiệu ứng neon
        gc.setStroke(Color.rgb(150, 200, 255, 0.9));
        gc.setLineWidth(GameConstants.UNDERLINE_HEIGHT);
        gc.strokeRoundRect(x, y, width, height, GameConstants.PADDLE_CORNER_RADIUS,
                GameConstants.PADDLE_CORNER_RADIUS);
        gc.restore();
    }

    public void reset() {
        setX(GameConstants.PADDLE_X);
        setY(GameConstants.PADDLE_Y);
        setWidth(GameConstants.PADDLE_WIDTH);
    }

    public void activateGlow() {
        isGlowing = true;
    }

    // Getters & Setters
    public void setMovingLeft(boolean moving) {
        this.movingLeft = moving;
        if (moving) activateGlow();
    }

    public void setMovingRight(boolean moving) {
        this.movingRight = moving;
        if (moving) {
            activateGlow();
        }
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

    public double getVelocity() {
        return velocity;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setWidth(double width) {
        this.width = width;
    }
}

// Trail effect
class PaddleTrail extends GameObject {
    private double width;
    private double height;
    private double opacity;

    public PaddleTrail(double x, double y, double width, double height, double intensity) {
        super(x, y);
        this.width = width;
        this.height = height;
        this.opacity = intensity * GameConstants.TRAIL_OPACITY_FACTOR;
    }

    public boolean update() {
        opacity -= GameConstants.TRAIL_OPACITY_DEC;
        return opacity > 0;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(Color.rgb(100, 150, 255, opacity));
        gc.fillRoundRect(x, y, width, height, GameConstants.PADDLE_CORNER_RADIUS,
                GameConstants.PADDLE_CORNER_RADIUS);
    }
}
