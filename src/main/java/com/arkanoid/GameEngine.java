package com.arkanoid;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private final double width;
    private final double height;

    // UI + pause/combo
    private final GameUI gameUI;
    private int combo = 0;
    private long lastBrickHitTime = 0;

    // Level
    private final LevelManager levelManager;
    private int currentLevel = 1;
    private static final int MAX_LEVEL = 3;
    private int levelRows = 6;
    private int levelCols = 10;

    // Bóng dính thanh trượt lúc đầu
    private boolean ballAttached = true;

    // Âm thanh hiệu ứng va chạm
    private AudioClip hitClip;

    // Nhạc nền
    private MediaPlayer bgmPlayer;

    public GameEngine(double width, double height) {
        this.width = width;
        this.height = height;
        this.canvas = new Canvas(width, height);
        this.gc = canvas.getGraphicsContext2D();

        this.score = 0;
        this.lives = 3;
        this.gameState = GameState.START_MENU;

        this.gameUI = new GameUI(width, height);
        this.levelManager = new LevelManager(width, height);

        // Âm va chạm (ví dụ /audio/vacham.wav)
        try {
            hitClip = new AudioClip(
                    getClass().getResource("/audio/vacham.wav").toExternalForm()
            );
            hitClip.setVolume(0.8);
        } catch (Exception e) {
            System.err.println("Cannot load hit sound: " + e.getMessage());
        }

        // Nhạc nền
        try {
            Media bgm = new Media(
                    getClass().getResource("/audio/backgroundaudio.mp3").toExternalForm()
            );
            bgmPlayer = new MediaPlayer(bgm);
            bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            bgmPlayer.setVolume(0.5);
        } catch (Exception e) {
            System.err.println("Cannot load BGM: " + e.getMessage());
        }

        initializeGame();
        setupGameLoop();
    }

    private void initializeGame() {
        balls = new ArrayList<>();
        balls.add(new Ball(width / 2, height - 100, 10));

        paddle = new Paddle(width / 2 - 60, height - 40, 120, 15);

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
        double cy = paddle.getY() - ball.getRadius() - 0.01;
        ball.setX(cx);
        ball.setY(cy);
        ball.setDx(0);
        ball.setDy(0);
        ball.setSpeed(1.0);
    }

    private void loadLevel(int level) {
        bricks = levelManager.buildLevel(level);
        levelRows = levelManager.getRows();
        levelCols = levelManager.getCols();

        paddle.setX(Math.max(0, Math.min(width - paddle.getWidth(), width / 2 - paddle.getWidth() / 2)));

        if (balls.isEmpty()) {
            balls.add(new Ball(width / 2, height - 100, 10));
        }
        attachBallToPaddle();
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
        score = 0;
        lives = 3;
        currentLevel = 1;
        combo = 0;
        powerUps.clear();
        isAppliedPowerUps.replaceAll(p -> null);
        bricks = levelManager.buildLevel(currentLevel);
        balls.clear();
        balls.add(new Ball(width / 2, height - 100, 10));
        //reset paddle
        attachBallToPaddle();
        gameState = GameState.PLAYING;
        if (bgmPlayer != null) {
            bgmPlayer.play();
        }
    }

    private void update() {
        if (gameState != GameState.PLAYING) return;

        if (ballAttached) {
            Ball ball = balls.get(0);
            double cx = paddle.getX() + paddle.getWidth() / 2.0;
            ball.setX(cx);
            ball.setY(paddle.getY() - ball.getRadius() - 0.01);
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
                if (bgmPlayer != null) bgmPlayer.stop();
            } else {
                resetBall();
                combo = 0;
            }
        }

        boolean allBreakablesGone = bricks.stream()
                .filter(br -> !(br instanceof SilverBrick))
                .allMatch(Brick::isDestroyed);
        if (allBreakablesGone) {
            if (currentLevel < MAX_LEVEL) {
                gameState = GameState.LEVEL_COMPLETED;
            } else {
                gameState = GameState.VICTORY;
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
                if (speed < 0.0001) speed = 4.0;

                double theta = Math.toRadians(hitPos * 60.0);
                double newDx = speed * Math.sin(theta);
                double newDy = -Math.abs(speed * Math.cos(theta));

                ball.setDx(newDx);
                ball.setDy(newDy);
                ball.setY(paddle.getY() - ball.getRadius() - 0.01);
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

                // Bóng mạnh: xuyên hoặc tùy chỉnh
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
        balls.add(new Ball(width / 2, height - 100, 10));
        attachBallToPaddle();
    }

    private void render() {
        gameUI.update();

        gameUI.drawBackground(gc);

        paddle.draw(gc);
        for (Ball b : balls) b.draw(gc);
        for (Brick br : bricks) br.draw(gc);
        for (PowerUp p : powerUps) p.draw(gc);

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

        gc.setFill(Color.rgb(255, 215, 100));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 44));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("LEVEL " + currentLevel + " COMPLETED", width / 2, height / 2 - 40);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 24));
        gc.fillText("Score: " + score, width / 2, height / 2 + 5);

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        gc.fillText("Press ENTER to continue to the next level", width / 2, height / 2 + 45);
    }

    private void drawGameOver() {
        gc.setFill(Color.rgb(0, 0, 0, 0.72));
        gc.fillRect(0, 0, width, height);

        gc.setFill(Color.rgb(255, 100, 100));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("GAME OVER", width / 2, height / 2 - 20);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 24));
        gc.fillText("Final Score: " + score, width / 2, height / 2 + 30);
        gc.fillText("Press ENTER to restart", width / 2, height / 2 + 70);
    }

    private void drawVictory() {
        gc.setFill(Color.rgb(0, 0, 0, 0.72));
        gc.fillRect(0, 0, width, height);

        gc.setFill(Color.rgb(100, 255, 100));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("VICTORY!", width / 2, height / 2 - 20);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 24));
        gc.fillText("Final Score: " + score, width / 2, height / 2 + 30);
        gc.fillText("Press ENTER to restart", width / 2, height / 2 + 70);
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

                    double speed = 4.0;
                    if (Math.abs(hitPos) < 0.05) {
                        hitPos = Math.copySign(0.173648, Math.random() < 0.5 ? -1 : 1); // ~sin(10°)
                    }
                    double theta = Math.toRadians(hitPos * 60.0);
                    double newDx = speed * Math.sin(theta);
                    double newDy = -Math.abs(speed * Math.cos(theta));

                    ball.setDx(newDx);
                    ball.setDy(newDy);
                    ball.setSpeed(1.0);
                    ballAttached = false;
                } else {
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
                } else if (gameState == GameState.GAME_OVER || gameState == GameState.VICTORY) {
                    restartGame();
                } else if (gameState == GameState.LEVEL_COMPLETED) {
                    if (currentLevel < MAX_LEVEL) {
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
            default:
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

    public List<Ball> getBalls() { return balls; }
    public Paddle getPaddle() { return paddle; }
    public double getHeight() { return height; }
    public void addLive() {lives++;}
}

enum GameState {
    START_MENU,
    PLAYING,
    PAUSED,
    GAME_OVER,
    VICTORY,
    LEVEL_COMPLETED
}
