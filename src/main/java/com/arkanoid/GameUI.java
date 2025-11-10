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
    private Image backgroundImage;
    private double width = GameConstants.WINDOW_WIDTH;
    private double height = GameConstants.WINDOW_HEIGHT;

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

    public GameUI() {
        loadBackground();
    }


    private void loadBackground() {
        backgroundImage = tryLoadFromClasspath("/" + GameConstants.BG_PATH);
        if (backgroundImage == null) {
            backgroundImage = tryLoadFromClasspath(GameConstants.BG_PATH);
        }
        if (backgroundImage == null) {
            backgroundImage = tryLoadFromFile("file:src/main/resources/" + GameConstants.BG_PATH);
        }
        if (backgroundImage == null) {
            System.err.println("Không tìm thấy ảnh nền: /" + GameConstants.BG_PATH);
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
        scorePulse *= GameConstants.SCORE_PULSE_DEC;
        if (comboAlpha > 0) {
            comboAlpha -= GameConstants.COMBO_ALPHA_DEC;
            if (comboAlpha < 0) comboAlpha = 0;
        }
        comboScale += (1.0 - comboScale) * 0.15;
        floatTexts.removeIf(ft -> !ft.update(GameConstants.FRAME_DELTA_TIME));
    }

    // Vẽ nền bằng ảnh + vignette để HUD nổi bật
    public void drawBackground(GraphicsContext gc) {
        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0, GameConstants.WINDOW_WIDTH,
                    GameConstants.WINDOW_HEIGHT);
        } else {
            LinearGradient fallback = new LinearGradient(
                    0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(20, 20, 40)),
                    new Stop(1, Color.rgb(40, 20, 60))
            );
            gc.setFill(fallback);
            gc.fillRect(0, 0, GameConstants.WINDOW_WIDTH, height);
        }

        RadialGradient vignette = new RadialGradient(
                0, 0, 0.5, 0.5, 0.9, true, CycleMethod.NO_CYCLE,
                new Stop(0.75, Color.TRANSPARENT),
                new Stop(1.0, Color.rgb(0, 0, 0, 0.45))
        );
        gc.setFill(vignette);
        gc.fillRect(0, 0, GameConstants.WINDOW_WIDTH, height);
    }

    // HUD: chỉ Score + Lives
    public void drawHUD(GraphicsContext gc, int score, int lives, int levelIgnored) {
        LinearGradient topBar = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(0, 0, 0, 0.55)),
                new Stop(1, Color.rgb(0, 0, 0, 0.25))
        );
        gc.setFill(topBar);
        gc.fillRect(0, 0, GameConstants.WINDOW_WIDTH, GameConstants.TOPBAR_HEIGHT);

        gc.setFill(new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(120, 200, 255, 0.8)),
                new Stop(1, Color.rgb(120, 200, 255, 0.0))
        ));
        gc.fillRect(0, GameConstants.TOPBAR_HEIGHT - GameConstants.UNDERLINE_HEIGHT,
                GameConstants.WINDOW_WIDTH, GameConstants.UNDERLINE_HEIGHT);

        if (score > displayedScore) {
            int diff = score - displayedScore;
            displayedScore += Math.max(1, (int) Math.ceil(diff * 0.15));
            scorePulse = GameConstants.SCORE_PULSE_MAX;
        } else if (score < displayedScore) {
            displayedScore = score;
        }

        double size = GameConstants.SCORE_FONT_SIZE + scorePulse * 6;
        gc.setFont(Font.font(GameConstants.FONT_NAME, FontWeight.BOLD, size));

        DropShadow glow = new DropShadow();
        glow.setColor(Color.rgb(0, 200, 255, 0.9));
        glow.setRadius(GameConstants.SCORE_FONT_SIZE / 2 + scorePulse * 8);
        gc.setEffect(glow);

        gc.setFill(new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(255, 255, 255)),
                new Stop(0.6, Color.rgb(190, 235, 255)),
                new Stop(1, Color.rgb(150, 210, 255))
        ));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("SCORE: " + String.format("%06d", displayedScore),
                GameConstants.SCORE_X, GameConstants.HUD_Y);

        gc.setEffect(null);

        gc.setFont(Font.font(GameConstants.FONT_NAME, FontWeight.BOLD, GameConstants.LIVES_FONT_SIZE));
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText("LIVES: " + lives, GameConstants.LIVES_X, GameConstants.HUD_Y);

        if (comboAlpha > GameConstants.COMBO_ALPHA_MIN) drawCombo(gc);

        gc.setTextAlign(TextAlignment.CENTER);
        for (FloatingText ft : floatTexts) ft.draw(gc);
    }

    private void drawCombo(GraphicsContext gc) {
        gc.save();
        gc.translate(GameConstants.COMBO_X, GameConstants.COMBO_Y);
        gc.scale(comboScale, comboScale);

        DropShadow glow = new DropShadow();
        glow.setColor(Color.rgb(255, 245, 120, comboAlpha));
        glow.setRadius(24);
        gc.setEffect(glow);

        gc.setFont(Font.font(GameConstants.FONT_NAME, FontWeight.BOLD, GameConstants.COMBO_FONT_SIZE));
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
        gc.setFill(Color.rgb(0, 0, 0, 0.8));
        gc.fillRect(0, 0, GameConstants.WINDOW_WIDTH, height);

        gc.setFont(Font.font(GameConstants.FONT_NAME, FontWeight.BOLD,
                GameConstants.TITLE_FONT_SIZE));
        gc.setFill(Color.rgb(180, 200, 255, 0.9));
        gc.setEffect(new DropShadow(20, Color.rgb(120, 150, 255, 0.8)));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("PAUSED", GameConstants.TEXT_X, GameConstants.TITLE_Y);

        gc.setEffect(null);
        gc.setFont(Font.loadFont(getClass().getResourceAsStream(GameConstants.INSTRUCTION_PATH),
                GameConstants.INSTRUCTION_FONT_SIZE));
        gc.setFill(Color.rgb(230, 240, 255));
        gc.fillText("Press SPACE to continue", GameConstants.TEXT_X, GameConstants.GUIDE_Y);
    }

    public void drawStartMenu(GraphicsContext gc) {
        gc.setFill(Color.rgb(0, 0, 0, 1));
        gc.fillRect(0, 0, GameConstants.WINDOW_WIDTH, height);

        gc.setEffect(new DropShadow(20, Color.web("#0044FF")));
        gc.setFill(Color.web("#00FFFF"));
        gc.setFont(Font.loadFont(getClass().getResourceAsStream(GameConstants.GAME_TITLE_PATH)
                , GameConstants.GAME_TITLE_FONT_SIZE));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("ARKANOID", GameConstants.TEXT_X, GameConstants.GAME_TITLE_Y);
        gc.setEffect(null);


        gc.setFill(Color.WHITE);
        gc.setFont(Font.loadFont(getClass().getResourceAsStream(GameConstants.INSTRUCTION_PATH),
                GameConstants.INSTRUCTION_FONT_SIZE));
        gc.fillText("Press ENTER to Start...", GameConstants.TEXT_X,
                GameConstants.GUIDE_Y);
        gc.fillText("← → to move | SPACE to launch/pause", GameConstants.TEXT_X,
                GameConstants.MOVE_GUIDE_Y);

        drawScoreBoard(gc);
    }

    public void drawScoreBoard(GraphicsContext gc) {
        gc.setFill(Color.GOLD);
        gc.setFont(Font.loadFont(getClass().getResourceAsStream(GameConstants.INSTRUCTION_PATH),
                GameConstants.SCOREBOARD_TITLE_FONT_SIZE));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("SCORE BOARD", GameConstants.TEXT_X, GameConstants.SCOREBOARD_TITLE_Y);
        double y = GameConstants.SCOREBOARD_START_Y;
        int rank = 1;
        gc.setFont(Font.loadFont(getClass().getResourceAsStream(GameConstants.INSTRUCTION_PATH),
                GameConstants.INSTRUCTION_FONT_SIZE));
        for (Score score : highScoreManager.getScores()) {
            gc.setTextAlign(TextAlignment.LEFT);
            gc.fillText(rank + " " + score.name + ": " + score.score,
                    GameConstants.SCOREBOARD_X, y);
            y += GameConstants.SCOREBOARD_OFFSET_Y;
            rank++;
        }
    }

    private static class FloatingText {
        double x, y;
        double vy = GameConstants.FLOATING_VELOCITY;
        double life = GameConstants.INITIAL_TEXT_LIFE;
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
            gc.setFont(Font.font(GameConstants.FONT_NAME, FontWeight.BOLD,
                    GameConstants.ADD_SCORE_FONT_SIZE));
            gc.setFill(color.deriveColor(0, 1, 1, alpha));
            gc.fillText(text, x, y);
        }
    }
}
