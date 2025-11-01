package com.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.*;

public class ParticleSystem {
    private List<Particle> particles = new ArrayList<>();
    private Random random = new Random();

    public void createBrickExplosion(double x, double y, Color color) {
        for (int i = 0; i < 20; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = random.nextDouble() * 5 + 2;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;

            particles.add(new Particle(x, y, vx, vy, color));
        }
    }

    public void createBallTrail(double x, double y) {
        Color trailColor = Color.rgb(255, 200, 100, 0.5);
        particles.add(new Particle(x, y, 0, 0.5, trailColor, 0.3));
    }

    public void update() {
        particles.removeIf(p -> !p.isAlive());
        particles.forEach(Particle::update);
    }

    public void draw(GraphicsContext gc) {
        particles.forEach(p -> p.draw(gc));
    }
}

class Particle {
    private double x, y, vx, vy;
    private Color color;
    private double life;
    private double gravity = 0.2;

    public Particle(double x, double y, double vx, double vy, Color color) {
        this(x, y, vx, vy, color, 1.0);
    }

    public Particle(double x, double y, double vx, double vy, Color color, double life) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.color = color;
        this.life = life;
    }

    public void update() {
        x += vx;
        y += vy;
        vy += gravity;
        life -= 0.02;
    }

    public void draw(GraphicsContext gc) {
        gc.setFill(color.deriveColor(0, 1, 1, life));
        double size = 3 * life;
        gc.fillOval(x - size/2, y - size/2, size, size);
    }

    public boolean isAlive() {
        return life > 0;
    }
}