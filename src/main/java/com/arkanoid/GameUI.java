package com.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class GameUI {
    private final double width;
    private final double height;


    private static final String BG_PATH = "images/game_bg.jpg";
    private Image backgroundImage;

    // Hiệu ứng nhỏ cho score
    private int displayedScore = 0;
    private double scorePulse = 0;

    // Combo banner
    private String comboText = "";
    private double comboAlpha = 0;
    private double comboScale = 1.0;

    // Popup điểm
    private final List<FloatingText> floatTexts = new ArrayList<>();

    //High scores
    private HighScoreManager highScoreManager = HighScoreManager.getInstance();

    public GameUI(double width, double height) {
        this.width = width;
        this.height = height;
        loadBackground();
    }


    private void loadBackground() {
        backgroundImage = tryLoadFromClasspath("/" + BG_PATH);
        if (backgroundImage == null) backgroundImage = tryLoadFromClasspath(BG_PATH);
        if (backgroundImage == null) backgroundImage = tryLoadFromFile("file:src/main/resources/" + BG_PATH);

        if (backgroundImage == null) {
            System.err.println("Không tìm thấy ảnh nền: /" + BG_PATH);
        } else {
            System.out.println("Đã nạp ảnh nền.");
        }
    }

    private Image tryLoadFromClasspath(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is != null) {
                Image img = new Image(is);
                return img.isError() ? null : img;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private Image tryLoadFromFile(String uri) {
        try {
            Image img = new Image(uri);
            return img.isError() ? null : img;
        } catch (Exception ignored) {}
        return null;
    }

    // Cập nhật hiệu ứng nhẹ mỗi frame
    public void update() {
        scorePulse *= 0.90;
        if (comboAlpha > 0) {
            comboAlpha -= 0.02;
            if (comboAlpha < 0) comboAlpha = 0;
        }
        comboScale += (1.0 - comboScale) * 0.15;
        floatTexts.removeIf(ft -> !ft.update(0.016));
    }

    // Vẽ nền bằng ảnh + vignette để HUD nổi bật
    public void drawBackground(GraphicsContext gc) {
        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0, width, height);
        } else {
            LinearGradient fallback = new LinearGradient(
                    0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(20, 20, 40)),
                    new Stop(1, Color.rgb(40, 20, 60))
            );
            gc.setFill(fallback);
            gc.fillRect(0, 0, width, height);
        }

        RadialGradient vignette = new RadialGradient(
                0, 0, 0.5, 0.5, 0.9, true, CycleMethod.NO_CYCLE,
                new Stop(0.75, Color.TRANSPARENT),
                new Stop(1.0, Color.rgb(0, 0, 0, 0.45))
        );
        gc.setFill(vignette);
        gc.fillRect(0, 0, width, height);
    }

    // HUD: chỉ Score + Lives
    public void drawHUD(GraphicsContext gc, int score, int lives, int levelIgnored) {
        LinearGradient topBar = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(0, 0, 0, 0.55)),
                new Stop(1, Color.rgb(0, 0, 0, 0.25))
        );
        gc.setFill(topBar);
        gc.fillRect(0, 0, width, 56);

        gc.setFill(new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(120, 200, 255, 0.8)),
                new Stop(1, Color.rgb(120, 200, 255, 0.0))
        ));
        gc.fillRect(0, 54, width, 2);

        if (score > displayedScore) {
            int diff = score - displayedScore;
            displayedScore += Math.max(1, (int) Math.ceil(diff * 0.15));
            scorePulse = 1.0;
        } else if (score < displayedScore) {
            displayedScore = score;
        }

        double baseSize = 24;
        double size = baseSize + scorePulse * 6;
        gc.setFont(Font.font("Time New Roman", FontWeight.BOLD, size));

        DropShadow glow = new DropShadow();
        glow.setColor(Color.rgb(0, 200, 255, 0.9));
        glow.setRadius(12 + scorePulse * 8);
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

        gc.setFont(Font.font("Time New Roman", FontWeight.BOLD, 20));
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText("LIVES: " + lives, width - 20, 36);

        if (comboAlpha > 0.01) drawCombo(gc);

        gc.setTextAlign(TextAlignment.CENTER);
        for (FloatingText ft : floatTexts) ft.draw(gc);
    }

    private void drawCombo(GraphicsContext gc) {
        gc.save();
        gc.translate(width / 2, height * 0.22);
        gc.scale(comboScale, comboScale);

        DropShadow glow = new DropShadow();
        glow.setColor(Color.rgb(255, 245, 120, comboAlpha));
        glow.setRadius(24);
        gc.setEffect(glow);

        gc.setFont(Font.font("Time New Roman", FontWeight.BOLD, 40));
        gc.setFill(Color.rgb(255, 220, 50, comboAlpha));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(comboText, 0, 0);

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
        gc.setFill(Color.rgb(0, 0, 0, 0.72));
        gc.fillRect(0, 0, width, height);

        DropShadow glow = new DropShadow();
        glow.setColor(Color.rgb(120, 220, 255, 0.9));
        glow.setRadius(25);
        gc.setEffect(glow);

        gc.setFont(Font.font("Time New Roman", FontWeight.BOLD, 56));
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("PAUSED", width / 2, height / 2 - 30);

        gc.setEffect(null);
        gc.setFont(Font.font("Time New Roman", FontWeight.NORMAL, 22));
        gc.setFill(Color.rgb(230, 240, 255));
        gc.fillText("Press SPACE to continue", width / 2, height / 2 + 20);
    }

    public void drawStartMenu(GraphicsContext gc) {
        gc.setFill(Color.rgb(0, 0, 0, 1));
        gc.fillRect(0, 0, width, height);

        //Font.loadFont(getClass().getResourceAsStream("/font/Arka_solid.ttf"), 100)

        gc.setEffect(new DropShadow(20, Color.web("#0044FF"))); // ánh cam
        gc.setFill(Color.web("#00FFFF")); // vàng kim
        gc.setFont(Font.loadFont(getClass().getResourceAsStream("/font/Arka_solid.ttf"), 100));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("ARKANOID", width / 2, 150);
        gc.setEffect(null);


        gc.setFill(Color.WHITE);
        gc.setFont(Font.loadFont(getClass().getResourceAsStream("/font/PressStart2P-Regular.ttf"), 15));
        gc.fillText("Press ENTER to Start...", width / 2, height - 10);
        gc.fillText("← → to move | SPACE to launch/pause", width / 2, height - 80);

        drawScoreBoard(gc);
    }

    public void drawScoreBoard(GraphicsContext gc) {
        gc.setFill(Color.GOLD);
        gc.setFont(Font.loadFont(getClass().getResourceAsStream("/font/PressStart2P-Regular.ttf"), 20));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("SCORE BOARD", width / 2, 210);
        int y = 250;
        int rank = 1;
        gc.setFont(Font.loadFont(getClass().getResourceAsStream("/font/PressStart2P-Regular.ttf"), 15));
        for (Score score : highScoreManager.getScores()) {
            gc.setTextAlign(TextAlignment.LEFT);
            gc.fillText(rank + " " + score.name + ": " + score.score, width / 2 - 100, y);
            y += 40;
            rank++;
        }
    }

    private static class FloatingText {
        double x, y;
        double vy = -30;
        double life = 1.0;
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
            gc.setFont(Font.font("Times New Roman", FontWeight.BOLD, 18));
            gc.setFill(color.deriveColor(0, 1, 1, alpha));
            gc.fillText(text, x, y);
        }
    }
}