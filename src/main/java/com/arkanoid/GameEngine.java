package com.arkanoid;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;

public class GameEngine {
    private Canvas canvas;
    private GraphicsContext gc;
    private Ball ball;
    private Paddle paddle;
    private List<Brick> bricks;
    private AnimationTimer gameLoop;
    private int score;
    private int lives;
    private GameState gameState;
    private double width;
    private double height;

    public GameEngine(double width, double height) {
        this.width = width;
        this.height = height;
        this.canvas = new Canvas(width, height);
        this.gc = canvas.getGraphicsContext2D();
        this.score = 0;
        this.lives = 3;
        this.gameState = GameState.PLAYING;

        initializeGame();
        setupGameLoop();
    }

    private void initializeGame() {
        ball = new Ball(width / 2, height - 100, 10);
        paddle = new Paddle(width / 2 - 60, height - 40, 120, 15);
        bricks = new ArrayList<>();

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
                bricks.add(new Brick(x, y, brickWidth, brickHeight, colors[row], points[row]));
            }
        }
    }

    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render();
            }
        };
    }

    private void update() {
        if (gameState != GameState.PLAYING) return;

        ball.update(width, height);
        paddle.update(width);
        checkCollisions();

        if (ball.isOutOfBounds(height)) {
            lives--;
            if (lives <= 0) {
                gameState = GameState.GAME_OVER;
            } else {
                resetBall();
            }
        }

        if (bricks.stream().allMatch(Brick::isDestroyed)) {
            gameState = GameState.VICTORY;
        }
    }

    private void checkCollisions() {
        if (ball.getY() + ball.getRadius() >= paddle.getY() &&
            ball.getY() - ball.getRadius() <= paddle.getY() + paddle.getHeight() &&
            ball.getX() >= paddle.getX() &&
            ball.getX() <= paddle.getX() + paddle.getWidth()) {

            ball.reverseY();
            double hitPos = (ball.getX() - paddle.getX()) / paddle.getWidth();
            double angle = (hitPos - 0.5) * 2;
            ball.setY(paddle.getY() - ball.getRadius());
        }

        for (Brick brick : bricks) {
            if (brick.checkCollision(ball)) {
                ball.reverseY();
                score += brick.getPoints();
                break;
            }
        }
    }

    private void resetBall() {
        ball.setX(width / 2);
        ball.setY(height - 100);
        ball.setDy(-3);
    }

    private void render() {
        drawBackground();

        paddle.draw(gc);
        ball.draw(gc);

        for (Brick brick : bricks) {
            brick.draw(gc);
        }

        drawUI();

        if (gameState == GameState.GAME_OVER) {
            drawGameOver();
        } else if (gameState == GameState.VICTORY) {
            drawVictory();
        }
    }

    private void drawBackground() {
        Stop[] stops = new Stop[] {
            new Stop(0, Color.rgb(20, 20, 40)),
            new Stop(1, Color.rgb(40, 20, 60))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
        gc.setFill(gradient);
        gc.fillRect(0, 0, width, height);
    }

    private void drawUI() {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        gc.fillText("Score: " + score, 20, 30);
        gc.fillText("Lives: " + lives, width - 100, 30);
    }

    private void drawGameOver() {
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, width, height);

        gc.setFill(Color.rgb(255, 100, 100));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("GAME OVER", width / 2, height / 2 - 20);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 24));
        gc.fillText("Final Score: " + score, width / 2, height / 2 + 30);
        gc.fillText("Press R to Restart", width / 2, height / 2 + 70);
    }

    private void drawVictory() {
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, width, height);

        gc.setFill(Color.rgb(100, 255, 100));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("VICTORY!", width / 2, height / 2 - 20);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 24));
        gc.fillText("Final Score: " + score, width / 2, height / 2 + 30);
        gc.fillText("Press R to Restart", width / 2, height / 2 + 70);
    }

    public void handleKeyPress(KeyEvent event) {
        switch (event.getCode()) {
            case LEFT:
                paddle.setMovingLeft(true);
                break;
            case RIGHT:
                paddle.setMovingRight(true);
                break;
            case R:
                if (gameState != GameState.PLAYING) {
                    restart();
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

    private void restart() {
        score = 0;
        lives = 3;
        gameState = GameState.PLAYING;
        bricks.clear();
        initializeGame();
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
