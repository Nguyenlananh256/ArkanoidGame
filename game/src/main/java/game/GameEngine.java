package game;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;

public class GameEngine {
    private Player player;
    private AnimationTimer gameLoop;
    private Canvas canvas;
    private GameState state;
    private boolean isPlaying = false;

//    private Ball ball;
//    private Paddle paddle;
//    private Brick brick;

    public GameEngine() {
//        Khởi tạo ball, paddle, brick
        state = GameState.START;
        player = new Player();
        setGameLoop();
    }

    public void setGameLoop() {
        gameLoop = new AnimationTimer() {
            public void handle(long l) {
                if (state == GameState.PLAYING) {
                    update();
                }
                //renderScene
            }
        };
    }

    public void startGame() {
        state = GameState.PLAYING;
        if (!isPlaying) {
            gameLoop.start();
            isPlaying = true;
        }
    }

    public void pauseGame() {
        if (state == GameState.PLAYING) {
            state = GameState.PAUSE;
        }
    }

    public void reset() {
        player.reset();

    }

    public void update() {
        //Movement and collision update
        //Game state update
    }
}
