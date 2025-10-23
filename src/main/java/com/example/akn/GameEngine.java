package com.example.akn;

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
    public List<Ball> balls;
    public Paddle paddle;
    public List<Brick> bricks;
    private List<PowerUp> powerUps;
    private boolean ballAttached;
    private AnimationTimer gameLoop;
    private int score;
    public int lives;
    private GameState gameState;
    double width;
    double height;

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
        balls = new ArrayList<>();
        balls.add(new Ball(width / 2, height - 100, 10));
        paddle = new Paddle(width / 2 - 60, height - 40, 120, 15);
        bricks = new ArrayList<>();
        powerUps = new ArrayList<>();

        ballAttached = true;
        positionBallOnPaddle();

        createBricks();
    }

    private void positionBallOnPaddle() {
        balls.get(0).setX(paddle.getX() + paddle.getWidth() / 2);
        balls.get(0).setY(paddle.getY() - balls.get(0).getRadius() - 2);
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

        Color steelBrickColor = Color.rgb(128, 128, 128);

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

    private void resetBall() {
        balls.clear();
        balls.add(new Ball(width / 2, height - 100, 10));
        ballAttached = true;
        positionBallOnPaddle();
    }

    private void update() {
        if (gameState != GameState.PLAYING) return;

        if (ballAttached) {
            // Nếu bóng đang dính paddle thì đi theo paddle
            balls.get(0).stickToPaddle(paddle);
        } else {
            // Nếu đã bắn bóng thì cập nhật như bình thường
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
                }
            }

            if (bricks.stream()
                    .filter(brick -> !(brick instanceof SilverBrick))
                    .allMatch(brick -> brick.isDestroyed())) {
                gameState = GameState.VICTORY;
            }
        }

        for (PowerUp p : powerUps) {
            p.update(height, this);
        }
    }

    private void checkCollisions() {
        double r = 10;

        // Va chạm với paddle trước (không được bỏ qua)
        for (Ball ball : balls) {
            if (ball.getDy() > 0) {
                // kiểm tra có va chạm vào thanh paddle không
                boolean collidePaddle =
                        ball.getY() + r >= paddle.getY()
                                && ball.getY() - r <= paddle.getY() + paddle.getHeight()
                                && ball.getX() >= paddle.getX()
                                && ball.getX() <= paddle.getX() + paddle.getWidth();

                if (collidePaddle) {
                    // phản xạ dựa trên vị trí chạm trên paddle
                    double paddleCenter = paddle.getX() + paddle.getWidth() / 2;
                    double hitPos = (ball.getX() - paddleCenter) / (paddle.getWidth() / 2);
                    hitPos = Math.max(-1, Math.min(1, hitPos));

                    double speed = Math.hypot(ball.getDx(), ball.getDy());
                    double angle = Math.toRadians(hitPos * 60.0);

                    double newDx = speed * Math.sin(angle);
                    double newDy = -Math.abs(speed * Math.cos(angle)); // bật lên

                    ball.setDx(newDx);
                    ball.setDy(newDy);

                    // đẩy ra khỏi paddle để tránh kẹt
                    ball.setY(paddle.getY() - r - 0.5);
                }
            }
        }

        for (Ball ball : balls) {
            // 1) Thu thập gạch giao cắt tại vị trí tiếp theo (lookahead)
            double nextX = ball.getX() + ball.getDx();
            double nextY = ball.getY() + ball.getDy();

            List<Brick> hits = new ArrayList<>();
            for (Brick brick : bricks) {
                if (brick.isDestroyed()) continue;

                double ballLeft   = nextX - r;
                double ballRight  = nextX + r;
                double ballTop    = nextY - r;
                double ballBottom = nextY + r;

                double bx = brick.getX(), by = brick.getY();
                double bw = brick.getWidth(), bh = brick.getHeight();

                boolean overlap =
                        ballRight  >= bx &&
                                ballLeft   <= bx + bw &&
                                ballBottom >= by &&
                                ballTop    <= by + bh;

                if (overlap) hits.add(brick);
            }

            // 2) Xử lý nhiều gạch: hợp nhất phản xạ trục và phá gạch
            if (!hits.isEmpty()) {
                boolean flipX = false, flipY = false;

                double ballLeft   = ball.getX() - r;
                double ballRight  = ball.getX() + r;
                double ballTop    = ball.getY() - r;
                double ballBottom = ball.getY() + r;

                for (Brick brick : hits) {
                    double bx = brick.getX(), by = brick.getY();
                    double bw = brick.getWidth(), bh = brick.getHeight();

                    double overlapX = Math.min(ballRight, bx + bw) - Math.max(ballLeft, bx);
                    double overlapY = Math.min(ballBottom, by + bh) - Math.max(ballTop, by);

                    if (overlapX < overlapY) {
                        flipX = true;
                    } else if (overlapY < overlapX) {
                        flipY = true;
                    } else {
                        flipX = true;
                        flipY = true;
                    }

                    if (!brick.isDestroyed()) {
                        brick.setDestroyed(bricks, 6, 10);
                        score += brick.getPoints();
                        PowerUp newPower = PowerUp.randomPowerUp(
                                brick.getX() + brick.getWidth() / 2,
                                brick.getY() + brick.getHeight() / 2);
                        if (newPower != null) {
                            powerUps.add(newPower);
                        }
                    }
                }

                if (flipX) ball.setDx(-ball.getDx());
                if (flipY) ball.setDy(-ball.getDy());

                // Epsilon tách khỏi gạch theo hướng mới
                double eps = 0.5;
                ball.setX(ball.getX() + Math.signum(ball.getDx()) * eps);
                ball.setY(ball.getY() + Math.signum(ball.getDy()) * eps);
            }

        }

        for (PowerUp powerUp : powerUps) {
            if ( (!powerUp.isDestroyed()) && powerUp.checkCollision(paddle)) {
                powerUp.applyEffect(this);
            }
        }

    }

    private void render() {
        drawBackground();

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

            case SPACE:
                if (ballAttached) {
                    ballAttached = false;

                    // Tốc độ khởi đầu cố định
                    double speed = 4.0;

                    // Tính góc theo vị trí tương đối so với tâm paddle
                    double paddleCenter = paddle.getX() + paddle.getWidth() / 2.0;
                    double hitPos = (balls.get(0).getX() - paddleCenter) / (paddle.getWidth() / 2.0);
                    hitPos = Math.max(-1.0, Math.min(1.0, hitPos)); // [-1..1]

                    // Ánh xạ vị trí -> góc tối đa 60°
                    double maxDeg = 60.0;
                    double angleDeg = hitPos * maxDeg;

                    // Nếu quá gần chính giữa, nắn lệch 10° ngẫu nhiên để tránh bay thẳng đứng
                    if (Math.abs(hitPos) < 0.05) {
                        angleDeg = (Math.random() < 0.5 ? -10.0 : 10.0);
                    }

                    double angle = Math.toRadians(angleDeg);

                    // Đặt vận tốc giữ nguyên speed
                    double newDx = speed * Math.sin(angle);
                    double newDy = -Math.abs(speed * Math.cos(angle)); // bay lên trên

                    balls.get(0).setDx(newDx);
                    balls.get(0).setDy(newDy);
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
        setupGameLoop();
    }

    public void start() {
        gameLoop.start();
    }

    public Canvas getCanvas() {
        return canvas;
    }

    private enum GameState {
        PLAYING,GAME_OVER, VICTORY
    }
}
