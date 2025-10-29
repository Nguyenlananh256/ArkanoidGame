package com.arkanoid;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameEngine {
    private Canvas canvas;
    private GraphicsContext gc;
    public List<Ball> balls;
    public Paddle paddle;
    public List<Brick> bricks;
    private List<PowerUp> powerUps;
    private List<PowerUp> isAppliedPowerUps;
    private AnimationTimer gameLoop;
    private int score;
    public int lives;
    private GameState gameState;
    public double width;
    public double height;

    // UI + trạng thái pause/combo
    private GameUI gameUI;
    private boolean isPaused = false;
    private int combo = 0;
    private long lastBrickHitTime = 0;

    public GameEngine(double width, double height) {
        this.width = width;
        this.height = height;
        this.canvas = new Canvas(width, height);
        this.gc = canvas.getGraphicsContext2D();
        this.score = 0;
        this.lives = 3;
        this.gameState = GameState.PLAYING;

        this.gameUI = new GameUI(width, height);

        initializeGame();
        setupGameLoop();
    }

    private void initializeGame() {
        balls = new ArrayList<>();
        balls.add(new Ball(width / 2, height - 100, 10));
        paddle = new Paddle(width / 2 - 60, height - 40, 120, 15);
        bricks = new ArrayList<>();
        powerUps = new ArrayList<>();
        isAppliedPowerUps = new ArrayList<>(Arrays.asList(
                null, null, null, null, null
        ));
        createBricks();
    }

    private void createBricks() {
        int rows = 6;
        int cols = 10;
        double brickWidth = 70;
        double brickHeight = 25;
        double padding = 5;
        double offsetX = (width - (cols * (brickWidth + padding))) / 2;
        double offsetY = 60;

        Color[] colors = {
                Color.rgb(255, 100, 100),
                Color.rgb(255, 150, 100),
                Color.rgb(255, 200, 100),
                Color.rgb(150, 255, 150),
                Color.rgb(100, 200, 255),
                Color.rgb(150, 150, 255)
        };

        int[] points = {60, 50, 40, 30, 20, 10};

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double x = offsetX + col * (brickWidth + padding);
                double y = offsetY + row * (brickHeight + padding);
                if (col - row == 3) {
                    bricks.add(new StrongBrick(x, y, brickWidth, brickHeight, colors[row], points[row]));

                } else {
                    bricks.add(new Brick(x, y, brickWidth, brickHeight, colors[row], points[row]));
                }
            }
        }
    }

    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gameState == GameState.PLAYING && !isPaused) {
                    update();
                }
                render();
            }
        };
    }

    private void update() {
        if (gameState != GameState.PLAYING) return;

        for (Ball b : balls) {
            b.update(width, height);
        }
        paddle.update(width);
        checkCollisions();

        // Bóng rơi khỏi biên -> xóa bóng đó
        List<Ball> toRemove = new ArrayList<>();
        for (Ball b : balls) {
            if (b.isOutOfBounds(height)) {
                toRemove.add(b);
            }
        }
        balls.removeAll(toRemove);
        toRemove.clear();

        // Không còn bóng nào -> Mất 1 mạng. Hết mạng -> game over
        if (balls.isEmpty()) {
            lives--;
            if (lives <= 0) {
                gameState = GameState.GAME_OVER;
            } else {
                resetBall();
                combo = 0;
            }
        }

        if (bricks.stream()
                .filter(brick -> !(brick instanceof SilverBrick))
                .allMatch(brick -> brick.isDestroyed())) {
            gameState = GameState.VICTORY;
        }

        for (PowerUp p : powerUps) {
            p.update(height, this);
        }

        for (PowerUp p : isAppliedPowerUps) {
            if (p != null && p.isApplied() && System.currentTimeMillis() - p.getStartTime() > p.getDuration()) {
                p.removeEffect(this);
                p.setExpired(true);
            }
        }
    }

    private void checkCollisions() {
        // Paddle
        for (Ball ball : balls) {
            if (ball.getY() + ball.getRadius() >= paddle.getY() &&
                    ball.getY() - ball.getRadius() <= paddle.getY() + paddle.getHeight() &&
                    ball.getX() >= paddle.getX() &&
                    ball.getX() <= paddle.getX() + paddle.getWidth()) {

                ball.reverseY();
                ball.setY(paddle.getY() - ball.getRadius());
            }
        }

        // Bricks + combo + popup điểm
        for (Ball ball : balls) {
            for (Brick brick : bricks) {
                if (brick.checkCollision(ball)) {
                    if (ball.isStrong()) {
                        if (!(brick instanceof SilverBrick)) {
                            brick.takeHit(bricks, 6, 10);
                        }
                        continue;
                    }

                    if (brick instanceof SilverBrick) {
                        ball.reverseY();
                        continue;
                    }
                    brick.takeHit(bricks, 6, 10);
                    ball.reverseY();

                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastBrickHitTime < 1000) {
                        combo++;
                        if (combo > 0) gameUI.showCombo(combo);
                    } else {
                        combo = 0;
                    }
                    lastBrickHitTime = currentTime;

                    int added = (int) Math.round(brick.getPoints() * (1 + combo * 0.5));
                    score += added;

                    // Popup điểm tại tâm viên gạch
                    double bx = brick.getX() + brick.getWidth() / 2;
                    double by = brick.getY() + brick.getHeight() / 2;
                    gameUI.addFloatingScore(bx, by, added);

                    PowerUp newPower = PowerUp.randomPowerUp(
                            brick.getX() + brick.getWidth() / 2,
                            brick.getY() + brick.getHeight() / 2);
                    if (newPower != null) {
                        powerUps.add(newPower);
                    }

                    break;
                }
            }
        }

        for (PowerUp powerUp : powerUps) {
            if ( (!powerUp.isDestroyed()) && powerUp.checkCollision(paddle)) {
                int type = powerUp.getType();
                if (isAppliedPowerUps.get(type - 1) != null
                        && isAppliedPowerUps.get(type - 1).isApplied()) {
                    isAppliedPowerUps.get(type - 1).setStartTime();
                }
                isAppliedPowerUps.set(type - 1, powerUp);
                isAppliedPowerUps.get(type - 1).applyEffect(this);
            }
        }
    }

    private void resetBall() {
        balls.clear();
        balls.add(new Ball(width / 2, height - 100, 10));
    }

    private void render() {
        gameUI.update();
        gameUI.drawBackground(gc);

        paddle.draw(gc);
        for (Ball ball:balls) {
            ball.draw(gc);
        }
        for (Brick brick : bricks) {
            brick.draw(gc);
        }
        for (PowerUp powerUp : powerUps) {
            powerUp.draw(gc);
        }


        gameUI.drawHUD(gc, score, lives, 1);

        if (isPaused && gameState == GameState.PLAYING) {
            gameUI.drawPauseMenu(gc);
        }

        if (gameState == GameState.GAME_OVER) {
            drawGameOver();
        } else if (gameState == GameState.VICTORY) {
            drawVictory();
        }
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

    }

    // Chỉ dùng mũi tên trái/phải và SPACE để pause
    public void handleKeyPress(KeyEvent event) {
        switch (event.getCode()) {
            case LEFT:
                paddle.setMovingLeft(true);
                break;
            case RIGHT:
                paddle.setMovingRight(true);
                break;
            case SPACE:
                if (gameState == GameState.PLAYING) {
                    isPaused = !isPaused;
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

    public void start() {
        gameLoop.start();
    }

    public Canvas getCanvas() {
        return canvas;
    }

    private enum GameState {
        PLAYING, GAME_OVER, VICTORY
    }
}
