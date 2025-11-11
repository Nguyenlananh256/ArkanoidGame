package com.arkanoid;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class GameEngine {
    // Canvas + context
    private final Canvas canvas;
    private final GraphicsContext gc;

    // Thực thể game
    private List<Ball> balls;
    private Paddle paddle;
    private List<Brick> bricks;
    private List<PowerUp> powerUps;
    private List<PowerUp> isAppliedPowerUps;

    // Vòng lặp game
    private AnimationTimer gameLoop;

    // Trạng thái/điểm số
    private int score;
    private int lives;
    private GameState gameState;
    private double width = GameConstants.WINDOW_WIDTH;
    private double height = GameConstants.WINDOW_HEIGHT;

    // UI + pause/combo
    private final GameUI gameUI;
    private int combo = GameConstants.INITIAL_COMBO;
    private long lastBrickHitTime = 0;

    // Level
    private final LevelManager levelManager;
    private int currentLevel = GameConstants.INITIAL_LEVEL;
    private int levelRows = GameConstants.DEFAULT_BRICK_ROWS;
    private int levelCols = GameConstants.DEFAULT_BRICK_COLS;

    // Bóng dính thanh trượt lúc đầu
    private boolean ballAttached = true;

    // Âm thanh hiệu ứng va chạm
    private AudioClip hitClip;

    // Nhạc nền
    private MediaPlayer bgmPlayer;

    // High scores
    private HighScoreManager highScoreManager = HighScoreManager.getInstance();
    private String playerName = "";
    private boolean nameEntered = false;

    public GameEngine() {
        this.canvas = new Canvas(width, height);
        this.gc = canvas.getGraphicsContext2D();

        this.score = GameConstants.INITIAL_SCORE;
        this.lives = GameConstants.INITIAL_LIVES;
        this.gameState = GameState.START_MENU;

        this.gameUI = new GameUI();
        this.levelManager = new LevelManager();

        // Âm va chạm
        try {
            hitClip = new AudioClip(
                    getClass().getResource(GameConstants.HIT_SOUND_PATH).toExternalForm()
            );
            hitClip.setVolume(GameConstants.HIT_VOLUME);
        } catch (Exception e) {
            System.err.println("Cannot load hit sound: " + e.getMessage());
        }

        // Nhạc nền
        try {
            Media bgm = new Media(
                    getClass().getResource(GameConstants.BGM_PATH).toExternalForm()
            );
            bgmPlayer = new MediaPlayer(bgm);
            bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            bgmPlayer.setVolume(GameConstants.BGM_VOLUME);
        } catch (Exception e) {
            System.err.println("Cannot load BGM: " + e.getMessage());
        }

        initializeGame();
        setupGameLoop();
    }

    private void initializeGame() {
        balls = new ArrayList<>();
        balls.add(new Ball(GameConstants.BALL_X, GameConstants.BALL_Y, GameConstants.BALL_RADIUS));

        paddle = new Paddle(GameConstants.PADDLE_X, GameConstants.PADDLE_Y,
                GameConstants.PADDLE_WIDTH, GameConstants.PADDLE_HEIGHT);

        bricks = new ArrayList<>();
        powerUps = new ArrayList<>();
        isAppliedPowerUps = new ArrayList<>(Arrays.asList(null, null, null, null, null));

        loadLevel(currentLevel);
        attachBallToPaddle();
    }

    private void attachBallToPaddle() {
        ballAttached = true;
        Ball ball = balls.get(0);
        double cx = paddle.getX() + paddle.getWidth() / 2.0;
        double cy = paddle.getY() - GameConstants.AFTER_HIT_OFFSET;
        ball.setX(cx);
        ball.setY(cy);
        ball.setDx(0);
        ball.setDy(0);
        ball.setSpeed(GameConstants.BALL_START_SPEED);
    }

    private void loadLevel(int level) {
        bricks = levelManager.buildLevel(level);
        levelRows = levelManager.getRows();
        levelCols = levelManager.getCols();

        paddle.setX(Math.max(0, Math.min(width - paddle.getWidth(), width / 2 - paddle.getWidth() / 2)));

        balls.clear();
        if (balls.isEmpty()) {
            balls.add(new Ball(GameConstants.BALL_X, GameConstants.BALL_Y, GameConstants.BALL_RADIUS));
        }
        attachBallToPaddle();

        powerUps.clear();
    }

    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gameState == GameState.PLAYING) {
                    update();
                }
                render();
            }
        };
    }

    public void start() {
        if (gameLoop != null) gameLoop.start();
        if (gameState == GameState.PLAYING && bgmPlayer != null) bgmPlayer.play();
    }

    public void restartGame() {
        score = GameConstants.INITIAL_SCORE;
        lives = GameConstants.INITIAL_LIVES;
        currentLevel = GameConstants.INITIAL_LEVEL;
        combo = GameConstants.INITIAL_COMBO;
        powerUps.clear();
        isAppliedPowerUps.replaceAll(p -> null);
        bricks = levelManager.buildLevel(currentLevel);
        balls.clear();
        balls.add(new Ball(GameConstants.BALL_X, GameConstants.BALL_Y, GameConstants.BALL_RADIUS));
        attachBallToPaddle();
    }

    private void update() {
        if (gameState != GameState.PLAYING) {
            return;
        }
        if (ballAttached) {
            Ball ball = balls.get(0);
            double cx = paddle.getX() + paddle.getWidth() / 2.0;
            ball.setX(cx);
            ball.setY(paddle.getY() - GameConstants.AFTER_HIT_OFFSET);
        } else {
            for (Ball b : balls) b.update(width, height);
            checkCollisions();
        }

        paddle.update(width);

        List<Ball> toRemove = new ArrayList<>();
        for (Ball b : balls) {
            if (b.isOutOfBounds(height)) toRemove.add(b);
        }
        balls.removeAll(toRemove);

        if (balls.isEmpty()) {
            lives--;
            if (lives <= 0) {
                gameState = GameState.GAME_OVER;
                playerName = "";
                if (bgmPlayer != null) bgmPlayer.stop();
            } else {
                resetBall();
                combo = GameConstants.INITIAL_COMBO;
            }
        }

        boolean allBreakablesGone = bricks.stream()
                .filter(br -> !(br instanceof SilverBrick))
                .allMatch(Brick::isDestroyed);
        if (allBreakablesGone) {
            if (currentLevel < GameConstants.MAX_LEVEL) {
                gameState = GameState.LEVEL_COMPLETED;
            } else {
                gameState = GameState.VICTORY;
                playerName = "";
                if (bgmPlayer != null) bgmPlayer.stop();
            }
        }

        for (PowerUp p : powerUps) p.update(height, this);

        for (PowerUp p : isAppliedPowerUps) {
            if (p != null && p.isApplied() && System.currentTimeMillis() - p.getStartTime() > p.getDuration()) {
                p.removeEffect(this);
                p.setExpired(true);
            }
        }
    }

    private void checkCollisions() {
        if (ballAttached) return;

        // Bóng - thanh
        for (Ball ball : balls) {
            if (ball.getDy() > 0) {
                boolean hit = (ball.getY() + ball.getRadius() >= paddle.getY()) &&
                        (ball.getY() - ball.getRadius() <= paddle.getY() + paddle.getHeight()) &&
                        (ball.getX() >= paddle.getX()) &&
                        (ball.getX() <= paddle.getX() + paddle.getWidth());
                if (!hit) continue;

                playHitSound();

                double center = paddle.getX() + paddle.getWidth() / 2.0;
                double hitPos = (ball.getX() - center) / (paddle.getWidth() / 2.0);
                if (hitPos < -1) hitPos = -1;
                if (hitPos > 1) hitPos = 1;

                double speed = Math.hypot(ball.getDx(), ball.getDy());
                if (speed < GameConstants.SPEED_THRESHOLD) {
                    speed = GameConstants.SPEED_AFTER_HIT;
                }

                double theta = Math.toRadians(hitPos * 60.0);
                double newDx = speed * Math.sin(theta);
                double newDy = -Math.abs(speed * Math.cos(theta));

                ball.setDx(newDx);
                ball.setDy(newDy);
                ball.setY(paddle.getY() - GameConstants.AFTER_HIT_OFFSET);
            }
        }

        // Bóng - gạch
        for (Ball ball : balls) {
            if (ballAttached) break;

            double nextX = ball.getX() + ball.getDx();
            double nextY = ball.getY() + ball.getDy();
            double r = ball.getRadius();

            for (Brick brick : bricks) {
                if (brick.isDestroyed()) continue;

                double bLeft = nextX - r;
                double bRight = nextX + r;
                double bTop = nextY - r;
                double bBottom = nextY + r;

                double gLeft = brick.getX();
                double gRight = brick.getX() + brick.getWidth();
                double gTop = brick.getY();
                double gBottom = brick.getY() + brick.getHeight();

                double overlapX = Math.min(bRight, gRight) - Math.max(bLeft, gLeft);
                double overlapY = Math.min(bBottom, gBottom) - Math.max(bTop, gTop);

                if (overlapX <= 0 || overlapY <= 0) continue;

                // SilverBrick: chỉ bật lại bóng, không điểm/combo/loot
                if (brick instanceof SilverBrick) {
                    playHitSound();
                    if (overlapX < overlapY) {
                        ball.setDx(-ball.getDx());
                    } else if (overlapY < overlapX) {
                        ball.setDy(-ball.getDy());
                    } else {
                        ball.setDx(-ball.getDx());
                        ball.setDy(-ball.getDy());
                    }
                    ball.setX(ball.getX() + Math.signum(ball.getDx()) * 0.1);
                    ball.setY(ball.getY() + Math.signum(ball.getDy()) * 0.1);
                    break;
                }

                // Gạch thường/strong/bomb: âm thanh
                playHitSound();

                // Bóng mạnh
                if (ball.isStrong()) {
                    boolean wasDestroyed = brick.isDestroyed();
                    brick.takeHit(bricks, levelRows, levelCols);
                    if (!wasDestroyed && brick.isDestroyed()) {
                        handleScoreComboAndLoot(brick);
                    } else if (!brick.isDestroyed()) {
                        addHitScoreOnly(brick);
                    }
                    // Cho xuyên
                    continue;
                }

                // Bóng thường: phản xạ rồi trừ máu
                boolean flipX = false, flipY = false;
                if (overlapX < overlapY) flipX = true;
                else if (overlapY < overlapX) flipY = true;
                else { flipX = true; flipY = true; }

                if (flipX) ball.setDx(-ball.getDx());
                if (flipY) ball.setDy(-ball.getDy());

                ball.setX(ball.getX() + Math.signum(ball.getDx()) * 0.1);
                ball.setY(ball.getY() + Math.signum(ball.getDy()) * 0.1);

                boolean wasDestroyed = brick.isDestroyed();
                brick.takeHit(bricks, levelRows, levelCols);
                if (!wasDestroyed && brick.isDestroyed()) {
                    handleScoreComboAndLoot(brick);
                } else if (!brick.isDestroyed()) {
                    addHitScoreOnly(brick);
                }
                break;
            }
        }

        // Nhặt power-up
        for (PowerUp pu : powerUps) {
            if (!pu.isDestroyed() && pu.checkCollision(paddle)) {
                int type = pu.getType();
                if (isAppliedPowerUps.get(type - 1) != null && isAppliedPowerUps.get(type - 1).isApplied()) {
                    isAppliedPowerUps.get(type - 1).setStartTime();
                } else {
                    isAppliedPowerUps.set(type - 1, pu);
                    isAppliedPowerUps.get(type - 1).applyEffect(this);
                }
            }
        }
    }

    private void playHitSound() {
        if (hitClip != null) hitClip.play();
    }

    // Điểm + combo + loot khi VỠ
    private void handleScoreComboAndLoot(Brick brick) {
        long now = System.currentTimeMillis();
        if (now - lastBrickHitTime <= 1000) {
            combo++;
            if (combo > 0) gameUI.showCombo(combo);
        } else {
            combo = 0;
        }
        lastBrickHitTime = now;

        int added = Math.round(brick.getPoints() * (1 + combo * 0.5f));
        score += added;

        double cx = brick.getX() + brick.getWidth() / 2;
        double cy = brick.getY() + brick.getHeight() / 2;
        gameUI.addFloatingScore(cx, cy, added);

        PowerUp pu = PowerUp.randomPowerUp(cx, cy);
        if (pu != null) powerUps.add(pu);
    }

    // Điểm mỗi HIT (chưa vỡ): không combo, không loot
    private void addHitScoreOnly(Brick brick) {
        int add = computePartialHitScore(brick);
        if (add <= 0) return;
        score += add;
        double cx = brick.getX() + brick.getWidth() / 2.0;
        double cy = brick.getY() + brick.getHeight() / 2.0;
        gameUI.addFloatingScore(cx, cy, add);
        // không thay đổi combo/lastBrickHitTime
    }

    // Ước lượng điểm mỗi hit theo loại gạch
    private int computePartialHitScore(Brick brick) {
        if (brick instanceof StrongBrick) {
            // 3 hit mới vỡ: chia đều
            return Math.max(1, Math.round(brick.getPoints() / 3f));
        }
        if (brick instanceof BombBrick) {
            // bomb chưa vỡ ở hit này: tạm cho nửa điểm
            return Math.max(1, Math.round(brick.getPoints() / 2f));
        }
        // brick thường thường vỡ ngay: không cần điểm mỗi hit
        return 0;
    }

    private void resetBall() {
        balls.clear();
        balls.add(new Ball(GameConstants.BALL_X, GameConstants.BALL_Y, GameConstants.BALL_RADIUS));
        attachBallToPaddle();
    }

    private void render() {
        gameUI.update();

        gameUI.drawBackground(gc);

        paddle.draw(gc);
        for (Ball b : balls) {
            b.draw(gc);
        }
        for (Brick br : bricks) {
            br.draw(gc);
        }
        for (PowerUp p : powerUps) {
            p.draw(gc);
        }

        gameUI.drawHUD(gc, score, lives, currentLevel);


        if(gameState == GameState.START_MENU) {
            gameUI.drawStartMenu(gc);
        }else if (gameState == GameState.PAUSED) {
            gameUI.drawPauseMenu(gc);
        } else if (gameState == GameState.LEVEL_COMPLETED) {
            drawLevelCompleted();
        } else if (gameState == GameState.GAME_OVER) {
            drawGameOver();
        } else if (gameState == GameState.VICTORY) {
            drawVictory();
        }
    }

    private void drawLevelCompleted() {
        gc.setFill(Color.rgb(0, 0, 0, 0.72));
        gc.fillRect(0, 0, width, height);

        gc.setFont(Font.font(GameConstants.FONT_NAME, FontWeight.BOLD, GameConstants.TITLE_FONT_SIZE));
        gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(255, 255, 180)),
                new Stop(1, Color.rgb(255, 200, 60))
        ));
        gc.setEffect(new DropShadow(25, Color.rgb(255, 180, 0, 0.9)));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("LEVEL " + currentLevel + " COMPLETED", GameConstants.TEXT_X,
                GameConstants.TITLE_Y);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.loadFont(getClass().getResourceAsStream(GameConstants.INSTRUCTION_PATH),
                GameConstants.INSTRUCTION_FONT_SIZE));
        gc.fillText("Score: " + score, GameConstants.TEXT_X, GameConstants.SCORE_Y);
        gc.fillText("Press ENTER to continue to the next level",
                GameConstants.TEXT_X, GameConstants.GUIDE_Y);
    }

    private void drawGameOver() {
        gc.setFill(Color.rgb(0, 0, 0, 1));
        gc.fillRect(0, 0, width, height);

        gc.setFont(Font.font(GameConstants.FONT_NAME, FontWeight.BOLD, GameConstants.TITLE_FONT_SIZE));
        gc.setFill(Color.rgb(255, 60, 60));
        gc.setEffect(new DropShadow(30, Color.rgb(0, 0, 0, 0.8)));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("GAME OVER!", GameConstants.TEXT_X, GameConstants.TITLE_Y);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.loadFont(getClass().getResourceAsStream(GameConstants.INSTRUCTION_PATH),
                GameConstants.INSTRUCTION_FONT_SIZE));
        gc.fillText("Final Score: " + score, GameConstants.TEXT_X, GameConstants.SCORE_Y);
        gc.fillText("Press ENTER to save & restart", GameConstants.TEXT_X, GameConstants.GUIDE_Y);

        gc.fillText("Enter your name:", GameConstants.TEXT_X, GameConstants.ENTER_NAME_Y);
        gc.fillText(playerName, GameConstants.TEXT_X, GameConstants.ENTER_NAME_Y + 30);
    }

    private void drawVictory() {
        gc.setFill(Color.rgb(0, 0, 0, 0.72));
        gc.fillRect(0, 0, width, height);

        gc.setFont(Font.font(GameConstants.FONT_NAME, FontWeight.BOLD, GameConstants.TITLE_FONT_SIZE));
        gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(255, 255, 180)),
                new Stop(1, Color.rgb(255, 220, 100))
        ));
        gc.setEffect(new DropShadow(30, Color.rgb(255, 255, 150, 0.9)));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("VICTORY!", GameConstants.TEXT_X, GameConstants.TITLE_Y);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.loadFont(getClass().getResourceAsStream(GameConstants.INSTRUCTION_PATH),
                GameConstants.INSTRUCTION_FONT_SIZE));
        gc.fillText("Final Score: " + score, GameConstants.TEXT_X, GameConstants.SCORE_Y);
        gc.fillText("Press ENTER to save & restart", GameConstants.TEXT_X, GameConstants.GUIDE_Y);

        gc.fillText("Enter your name:", GameConstants.TEXT_X, GameConstants.ENTER_NAME_Y);
        gc.fillText(playerName, GameConstants.TEXT_X, GameConstants.ENTER_NAME_Y + 30);
    }

    // Điều khiển
    public void handleKeyPress(KeyEvent event) {
        switch (event.getCode()) {
            case LEFT:
                paddle.setMovingLeft(true);
                break;
            case RIGHT:
                paddle.setMovingRight(true);
                break;
            case SPACE:
                if (gameState != GameState.PLAYING && gameState != GameState.PAUSED) {
                    break;
                }
                if (ballAttached) {
                    Ball ball = balls.get(0);

                    double center = paddle.getX() + paddle.getWidth() / 2.0;
                    double hitPos = (ball.getX() - center) / (paddle.getWidth() / 2.0);
                    if (hitPos < -1) hitPos = -1;
                    if (hitPos > 1) hitPos = 1;

                    double speed = GameConstants.SPEED_AFTER_HIT;
                    if (Math.abs(hitPos) < 0.05) {
                        hitPos = Math.copySign(0.173648, Math.random() < 0.5 ? -1 : 1); // ~sin(10°)
                    }
                    double theta = Math.toRadians(hitPos * 60.0);
                    double newDx = speed * Math.sin(theta);
                    double newDy = -Math.abs(speed * Math.cos(theta));

                    ball.setDx(newDx);
                    ball.setDy(newDy);
                    ball.setSpeed(GameConstants.BALL_START_SPEED);
                    ballAttached = false;
                } else {
                    if (gameState == GameState.PLAYING) {
                        gameState = GameState.PAUSED;
                    } else {
                        gameState = GameState.PLAYING;
                    }
                    if (bgmPlayer != null) {
                        if (gameState == GameState.PAUSED) {
                            bgmPlayer.pause();
                        }
                        else {
                            bgmPlayer.play();
                        }
                    }
                }
                break;
            case ENTER:
                if (gameState == GameState.START_MENU) {
                    restartGame();
                    gameState = GameState.PLAYING;
                    if (bgmPlayer != null) {
                        bgmPlayer.play();
                    }
                } else if (gameState == GameState.GAME_OVER || gameState == GameState.VICTORY) {
                    if (!playerName.isEmpty()) {
                        highScoreManager.addScore(playerName, score);
                        highScoreManager.loadScores();
                        restartGame();
                        gameState = GameState.START_MENU;
                    }
                } else if (gameState == GameState.LEVEL_COMPLETED) {
                    if (currentLevel < GameConstants.MAX_LEVEL) {
                        currentLevel++;
                        loadLevel(currentLevel);
                        gameState = GameState.PLAYING;
                        if (bgmPlayer != null) {
                            bgmPlayer.play();
                        }
                    } else {
                        gameState = GameState.VICTORY;
                        if (bgmPlayer != null) {
                            bgmPlayer.stop();
                        }
                    }
                }
                break;
            case ESCAPE:
                if (gameState == GameState.START_MENU) {
                    break;
                } else {
                    gameState = GameState.START_MENU;
                    if (bgmPlayer != null) {
                        bgmPlayer.stop();
                    }
                }
                break;
            default:
                if (event.getCode() == KeyCode.BACK_SPACE) {
                    if (!playerName.isEmpty()) {
                        playerName = playerName.substring(0, playerName.length() - 1);
                    }
                } else {
                    String key = event.getText();
                    playerName += key.toUpperCase();
                }
                break;
        }
    }

    public void handleKeyRelease(KeyEvent event) {
        switch (event.getCode()) {
            case LEFT:
                paddle.setMovingLeft(false);
                break;
            case RIGHT:
                paddle.setMovingRight(false);
                break;
            default:
                break;
        }
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void disposeAudio() {
        if (bgmPlayer != null) {
            try { bgmPlayer.stop(); } catch (Exception ignored) {}
            try { bgmPlayer.dispose(); } catch (Exception ignored) {}
        }
    }

    public List<Ball> getBalls() {
        return balls;
    }

    public Paddle getPaddle() {
        return paddle;
    }

    public double getHeight() {
        return height;
    }

    public void addLive() {
        lives++;
    }
}

enum GameState {
    START_MENU,
    PLAYING,
    PAUSED,
    GAME_OVER,
    VICTORY,
    LEVEL_COMPLETED
}
