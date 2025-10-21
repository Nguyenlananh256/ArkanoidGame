package com.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameUI {
    private final double width;
    private final double height;

    // Time/animation
    private long startNano = System.nanoTime();
    private long lastNano = startNano;
    private double t = 0;          // seconds
    private double dt = 0.016;     // delta time fallback

    // Parallax stars
    private static final int STARS_NEAR = 60;
    private static final int STARS_FAR = 40;
    private final double[] sxNear = new double[STARS_NEAR];
    private final double[] syNear = new double[STARS_NEAR];
    private final double[] ssNear = new double[STARS_NEAR];
    private final double[] sxFar  = new double[STARS_FAR];
    private final double[] syFar  = new double[STARS_FAR];
    private final double[] ssFar  = new double[STARS_FAR];

    // HUD state (animation)
    private int displayedScore = 0;
    private double scorePulse = 0; // 0..1

    // Combo banner
    private String comboText = "";
    private double comboAlpha = 0;  // 0..1
    private double comboScale = 1.0;

    // Optional floating texts (points popups)
    private final List<FloatingText> floatTexts = new ArrayList<>();

    private final Random rng = new Random();

    public GameUI(double width, double height) {
        this.width = width;
        this.height = height;

        // Init stars
        for (int i = 0; i < STARS_NEAR; i++) {
            sxNear[i] = rng.nextDouble() * width;
            syNear[i] = rng.nextDouble() * height;
            ssNear[i] = 1.2 + rng.nextDouble() * 1.3; // size
        }
        for (int i = 0; i < STARS_FAR; i++) {
            sxFar[i] = rng.nextDouble() * width;
            syFar[i] = rng.nextDouble() * height;
            ssFar[i] = 0.6 + rng.nextDouble() * 0.9;
        }
    }


    public void update() {
        long now = System.nanoTime();
        dt = (now - lastNano) / 1e9;
        if (dt < 0 || dt > 0.1) dt = 0.016;
        lastNano = now;
        t = (now - startNano) / 1e9;

        // Starfield parallax
        double vNear = 40; // px/s
        double vFar  = 15;
        for (int i = 0; i < STARS_NEAR; i++) {
            sxNear[i] -= vNear * dt;
            if (sxNear[i] < -2) {
                sxNear[i] = width + rng.nextDouble() * 30;
                syNear[i] = rng.nextDouble() * height;
            }
        }
        for (int i = 0; i < STARS_FAR; i++) {
            sxFar[i] -= vFar * dt;
            if (sxFar[i] < -2) {
                sxFar[i] = width + rng.nextDouble() * 30;
                syFar[i] = rng.nextDouble() * height;
            }
        }

        scorePulse *= Math.pow(0.85, dt * 60); // decay theo frame-rate


        if (comboAlpha > 0) {
            comboAlpha -= 0.02;
            if (comboAlpha < 0) comboAlpha = 0;
        }
        comboScale += (1.0 - comboScale) * 0.15;

        // Update floating texts
        floatTexts.removeIf(ft -> !ft.update(dt));
    }


    public void drawBackground(GraphicsContext gc) {
        // 1) Gradient nền dynamic
        double cx = 0.5 + Math.sin(t * 0.3) * 0.08;
        double cy = 0.5 + Math.cos(t * 0.25) * 0.08;
        RadialGradient bg = new RadialGradient(
                0, 0, cx, cy, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(12, 12, 26)),
                new Stop(0.5, Color.rgb(22, 16, 46)),
                new Stop(1.0, Color.rgb(40, 20, 70))
        );
        gc.setFill(bg);
        gc.fillRect(0, 0, width, height);

        gc.setFill(Color.rgb(255, 255, 255, 0.25));
        for (int i = 0; i < STARS_FAR; i++) {
            double size = ssFar[i];
            gc.fillOval(sxFar[i], syFar[i], size, size);
        }

        for (int i = 0; i < STARS_NEAR; i++) {
            double tw = (Math.sin(t * 6 + i) * 0.5 + 0.5) * 0.6 + 0.4;
            double alpha = 0.45 + tw * 0.4;
            gc.setFill(Color.rgb(200, 230, 255, alpha));
            double size = ssNear[i] + tw * 0.7;
            gc.fillOval(sxNear[i], syNear[i], size, size);
        }


        drawGrid(gc);

        drawScanlines(gc);

        RadialGradient vignette = new RadialGradient(
                0, 0, 0.5, 0.5, 0.9, true, CycleMethod.NO_CYCLE,
                new Stop(0.75, Color.TRANSPARENT),
                new Stop(1.0, Color.rgb(0, 0, 0, 0.45))
        );
        gc.setFill(vignette);
        gc.fillRect(0, 0, width, height);
    }

    private void drawGrid(GraphicsContext gc) {
        gc.setLineWidth(1);
        Color line = Color.rgb(130, 160, 255, 0.06);

        gc.setStroke(line);
        for (int x = 0; x < width; x += 50) {
            gc.strokeLine(x, 0, x, height);
        }
        for (int y = 0; y < height; y += 50) {
            gc.strokeLine(0, y, width, y);
        }
    }

    private void drawScanlines(GraphicsContext gc) {
        gc.setStroke(Color.rgb(0, 0, 0, 0.06));
        gc.setLineWidth(1);
        for (int y = 0; y < height; y += 3) {
            gc.strokeLine(0, y, width, y);
        }
    }

    public void drawHUD(GraphicsContext gc, int score, int lives, int levelIgnored) {
        // Top bar
        LinearGradient topBar = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(0, 0, 0, 0.55)),
                new Stop(1, Color.rgb(0, 0, 0, 0.25))
        );
        gc.setFill(topBar);
        gc.fillRect(0, 0, width, 56);

        // Bottom border glow
        LinearGradient glowLine = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(120, 200, 255, 0.8)),
                new Stop(1, Color.rgb(120, 200, 255, 0.0))
        );
        gc.setFill(glowLine);
        gc.fillRect(0, 54, width, 2);

        // Score tăng dần
        if (score > displayedScore) {
            int diff = score - displayedScore;
            displayedScore += Math.max(1, (int)Math.ceil(diff * 0.15));
            scorePulse = 1.0; // pulse khi tăng
        } else if (score < displayedScore) {
            displayedScore = score; // sync
        }

        // Score text
        double baseSize = 24;
        double size = baseSize + scorePulse * 6;
        gc.setFont(Font.font("Arial", FontWeight.BOLD, size));

        DropShadow glow = new DropShadow();
        glow.setColor(Color.rgb(0, 200, 255, 0.9));
        glow.setRadius(12 + scorePulse * 8);
        glow.setBlurType(BlurType.GAUSSIAN);
        gc.setEffect(glow);

        gc.setFill(new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(255, 255, 255)),
                new Stop(0.6, Color.rgb(190, 235, 255)),
                new Stop(1, Color.rgb(150, 210, 255))
        ));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("SCORE: " + String.format("%06d", displayedScore), 20, 36);

        gc.setEffect(null);

        drawLives(gc, lives);


        if (comboAlpha > 0.01) {
            drawCombo(gc);
        }

        for (FloatingText ft : floatTexts) {
            ft.draw(gc);
        }
    }

    private void drawLives(GraphicsContext gc, int lives) {
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        gc.setFill(Color.WHITE);
        double labelX = width - 180;
        gc.fillText("LIVES:", labelX, 36);

        double startX = labelX + 64;
        double y = 18;
        for (int i = 0; i < lives; i++) {
            drawHeart(gc, startX + i * 26, y, 10);
        }
    }

    private void drawHeart(GraphicsContext gc, double x, double y, double r) {
        // Gradient đỏ -> hồng
        LinearGradient g = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(255, 90, 110)),
                new Stop(1, Color.rgb(220, 30, 60))
        );
        gc.setFill(g);
        gc.fillOval(x - r, y, r, r);
        gc.fillOval(x, y, r, r);

        gc.beginPath();
        gc.moveTo(x - r, y + r * 0.7);
        gc.lineTo(x + r, y + r * 0.7);
        gc.lineTo(x, y + r * 1.8);
        gc.closePath();
        gc.setFill(g);
        gc.fill();

        // outline
        gc.setStroke(Color.rgb(255, 200, 210, 0.9));
        gc.setLineWidth(1.5);
        gc.strokeOval(x - r, y, r, r);
        gc.strokeOval(x, y, r, r);
        gc.beginPath();
        gc.moveTo(x - r, y + r * 0.7);
        gc.lineTo(x + r, y + r * 0.7);
        gc.lineTo(x, y + r * 1.8);
        gc.closePath();
        gc.stroke();
    }

    private void drawCombo(GraphicsContext gc) {
        String text = comboText;
        double alpha = comboAlpha;
        double scale = comboScale;

        gc.save();
        gc.translate(width / 2, height * 0.22);
        gc.scale(scale, scale);

        DropShadow glow = new DropShadow();
        glow.setColor(Color.rgb(255, 245, 120, alpha));
        glow.setRadius(24);
        gc.setEffect(glow);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        gc.setFill(Color.rgb(255, 220, 50, alpha));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(text, 0, 0);

        gc.setEffect(null);
        gc.restore();
    }

    public void showCombo(int combo) {
        this.comboText = combo + "x COMBO!";
        this.comboAlpha = 1.0;
        this.comboScale = 1.25;
    }


    public void addFloatingScore(double x, double y, int points) {
        floatTexts.add(new FloatingText(x, y, "+" + points, Color.rgb(255, 240, 180)));
    }

    public void drawPauseMenu(GraphicsContext gc) {
        // Overlay tối
        gc.setFill(Color.rgb(0, 0, 0, 0.72));
        gc.fillRect(0, 0, width, height);

        // Text neon
        DropShadow glow = new DropShadow();
        glow.setColor(Color.rgb(120, 220, 255, 0.9));
        glow.setRadius(25);
        gc.setEffect(glow);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 56));
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("PAUSED", width / 2, height / 2 - 30);

        gc.setEffect(null);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 22));
        gc.setFill(Color.rgb(230, 240, 255));
        gc.fillText("Press SPACE to continue", width / 2, height / 2 + 20);
    }

    // Simple floating text effect
    private static class FloatingText {
        double x, y;
        double vy = -30;   // px/s
        double life = 1.0; // seconds
        String text;
        Color color;

        FloatingText(double x, double y, String text, Color color) {
            this.x = x;
            this.y = y;
            this.text = text;
            this.color = color;
        }

        boolean update(double dt) {
            y += vy * dt;
            life -= dt;
            return life > 0;
        }

        void draw(GraphicsContext gc) {
            double alpha = Math.max(0, life);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            gc.setFill(color.deriveColor(0, 1, 1, alpha));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(text, x, y);
        }
    }
}