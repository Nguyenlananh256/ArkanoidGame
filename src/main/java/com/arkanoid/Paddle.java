package com.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.*;
import javafx.scene.effect.*;
import java.util.ArrayList;
import java.util.List;

public class Paddle {
    private double x;
    private double y;
    private double width;
    private double height;
    private double velocity = 0;  // Vận tốc hiện tại
    private double acceleration = 1.5;  // Gia tốc
    private double maxSpeed = 8;  // Tốc độ tối đa
    private double friction = 0.9;  // Ma sát để dừng mượt
    private boolean movingLeft;
    private boolean movingRight;

    // Hiệu ứng glow
    private double glowIntensity = 0;
    private boolean isGlowing = false;

    // Trail effect
    private List<PaddleTrail> trails = new ArrayList<>();

    public Paddle(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
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
            velocity *= -0.5; // Bounce back effect
            createImpactParticles(true);
        } else if (newX + width > canvasWidth) {
            newX = canvasWidth - width;
            velocity *= -0.5;
            createImpactParticles(false);
        }

        // Thêm trail effect khi di chuyển nhanh
        if (Math.abs(velocity) > 3) {
            trails.add(new PaddleTrail(x, y, width, height, Math.abs(velocity) / maxSpeed));
        }

        x = newX;

        // Update trails
        trails.removeIf(trail -> !trail.update());

        // Update glow effect
        if (isGlowing) {
            glowIntensity = Math.min(1, glowIntensity + 0.1);
        } else {
            glowIntensity = Math.max(0, glowIntensity - 0.05);
        }
    }

    public void draw(GraphicsContext gc) {
        // Draw trails first
        for (PaddleTrail trail : trails) {
            trail.draw(gc);
        }

        // Main paddle với hiệu ứng nâng cao
        gc.save();

        // Shadow effect động
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 100, 255, 0.8));
        shadow.setRadius(10 + glowIntensity * 20);
        shadow.setSpread(0.2);
        gc.setEffect(shadow);

        // Gradient động dựa trên velocity
        double hue = 200 + Math.abs(velocity) * 5;
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
        gc.fillRoundRect(x, y, width, height, 15, 15);

        // Inner glow
        if (glowIntensity > 0) {
            RadialGradient innerGlow = new RadialGradient(
                    0, 0, 0.5, 0.5, 0.8, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(255, 255, 255, glowIntensity * 0.3)),
                    new Stop(1, Color.TRANSPARENT)
            );
            gc.setFill(innerGlow);
            gc.fillRoundRect(x + 5, y + 2, width - 10, height - 4, 10, 10);
        }

        // Border với hiệu ứng neon
        gc.setStroke(Color.rgb(150, 200, 255, 0.9));
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, width, height, 15, 15);

        gc.restore();
    }

    private void createImpactParticles(boolean leftSide) {
        // Tạo particle khi va chạm cạnh (implement trong GameEngine)
        isGlowing = true;
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
        if (moving) activateGlow();
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public double getVelocity() { return velocity; }


    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }


    public void setWidth(double width) { this.width = width; }
}

// Inner class cho trail effect
class PaddleTrail {
    private double x, y, width, height;
    private double opacity;

    public PaddleTrail(double x, double y, double width, double height, double intensity) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.opacity = intensity * 0.3;
    }

    public boolean update() {
        opacity -= 0.05;
        return opacity > 0;
    }

    public void draw(GraphicsContext gc) {
        gc.setFill(Color.rgb(100, 150, 255, opacity));
        gc.fillRoundRect(x, y, width, height, 15, 15);
    }
}
