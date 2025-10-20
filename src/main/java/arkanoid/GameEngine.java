package arkanoid;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import java.util.ArrayList;
import java.util.List;


// điều khiển
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

    private boolean ballAttached;



    private Image background;

    // khởi tạo game ban đầu
    public GameEngine(double width, double height) {
        this.width = width;
        this.height = height;
        this.canvas = new Canvas(width, height);
        this.gc = canvas.getGraphicsContext2D();
        this.background = new Image(getClass().getResourceAsStream("/images/background.png"));

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

        bricks = LevelBuilder.build(LevelBuilder.LEVEL1, width, 60.0);

        ballAttached = true;

        positionBallOnPaddle();
    }


    private void positionBallOnPaddle() {
        ball.setX(paddle.getX() + paddle.getWidth() / 2);
        ball.setY(paddle.getY() - ball.getRadius() - 2);
    }

    private void createBricks() {
        int rows = 6;
        int cols = 10;
        double brickWidth = 70;
        double brickHeight = 25;
        double padding = 5;
        double offsetX = (width - (cols * (brickWidth + padding))) / 2;
        double offsetY = 60;


        int[] points = {60, 50, 40, 30, 20, 10};

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double x = offsetX + col * (brickWidth + padding);
                double y = offsetY + row * (brickHeight + padding);
                boolean isIndestructible = (row == 2 && col % 3 == 0); // ví dụ: hàng 3, cột 0,3,6,9 là gạch không phá
                bricks.add(new Brick(x, y, brickWidth, brickHeight, points[row], isIndestructible));

            }
        }
    }


    private void resetBall() {
        ballAttached = true;
        positionBallOnPaddle();
        ball.setDx(0);
        ball.setDy(0);
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


    // cập nhật logic
    private void update() {
        if (gameState != GameState.PLAYING) return;

        if (ballAttached) {
            // Nếu bóng đang dính paddle thì đi theo paddle
            ball.stickToPaddle(paddle);
        } else {
            // Nếu đã bắn bóng thì cập nhật như bình thường
            ball.update(width, height);
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

        paddle.update(width);
    }


    private void checkCollisions() {
        double r = ball.getRadius();

        // Va chạm với paddle trước (không được bỏ qua)
        if (ball.getDy() > 0) {
            // kiểm tra có va chạm vào thanh paddle không
            boolean collidePaddle =
                    ball.getY() + r >= paddle.getY() &&
                            ball.getY() - r <= paddle.getY() + paddle.getHeight() &&
                            ball.getX() >= paddle.getX() &&
                            ball.getX() <= paddle.getX() + paddle.getWidth();

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

                if (!brick.isIndestructible() && !brick.isDestroyed()) {
                    brick.destroy();
                    score += brick.getPoints();
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





    // vẽ
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


    // vẽ background
    private void drawBackground() {
        gc.drawImage(background, 0, 0, width, height);
    }



    // hiển thị điểm và mạng
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


    // xử lí bàn phím
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
                    double hitPos = (ball.getX() - paddleCenter) / (paddle.getWidth() / 2.0);
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

                    ball.setDx(newDx);
                    ball.setDy(newDy);
                }
                break;

            default:
                break;
        }
    }


    // nhả không di chuyển nữa
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


    // chơi lại
    private void restart() {
        score = 0;
        lives = 3;
        gameState = GameState.PLAYING;
        bricks.clear();
        initializeGame();
    }


    // bắt đầu vòng lặp
    public void start() {
        gameLoop.start();
    }

    public Canvas getCanvas() {
        return canvas;
    }


    // xác định trạng thái của game
    private enum GameState {
        PLAYING, GAME_OVER, VICTORY
    }
}
