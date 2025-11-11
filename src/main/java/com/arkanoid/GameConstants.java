package com.arkanoid;


import javafx.scene.image.Image;

public final class GameConstants {
    // Screen
    public static final double WINDOW_WIDTH = 780;
    public static final double WINDOW_HEIGHT = 600;
    public static final String GAME_TITLE = "Arkanoid - OOP Game";

    // Level
    public static final int INITIAL_LEVEL = 1;
    public static final int MIN_LIVES = 0;
    public static final int MAX_LEVEL = 3;
    public static final int DEFAULT_BRICK_ROWS = 6;
    public static final int DEFAULT_BRICK_COLS = 10;
    public static final int [] POINTS = {60, 50, 40, 30, 20, 10};

    // Player
    public static final int INITIAL_SCORE = 0;
    public static final int INITIAL_LIVES = 3;
    public static final int INITIAL_COMBO = 0;
    public static final String HIGH_SCORES_FILE = "highscores.txt";
    public static final int MAX_SCORES = 5;

    // Sound
    public static final String BGM_PATH = "/audio/backgroundaudio.mp3";
    public static final String HIT_SOUND_PATH = "/audio/vacham.wav";
    public static final double BGM_VOLUME = 0.5;
    public static final double HIT_VOLUME = 0.8;

    // Paddle
    public static final double PADDLE_X = WINDOW_WIDTH / 2 - 60;
    public static final double PADDLE_Y = WINDOW_HEIGHT - 40;
    public static final double PADDLE_WIDTH = 120;
    public static final double EXPANDED_PADDLE_WIDTH = 180;
    public static final double PADDLE_HEIGHT = 15;
    public static final double INITIAL_PADDLE_VELOCITY = 0;
    public static final double ACCELERATION = 1.5;
    public static final double MAX_SPEED = 8.0;
    public static final double FRICTION = 0.9;
    public static final double BOUCE_BACK = -0.5;
    public static final double PADDLE_CORNER_RADIUS = 15;

    public static final double TRAIL_SPEED_THRESHOLD = 3.0;
    public static final double TRAIL_OPACITY_FACTOR = 0.3;
    public static final double TRAIL_OPACITY_DEC = 0.05;

    public static final double GLOW_INC = 0.1;
    public static final double GLOW_DEC = 0.05;
    public static final double MAX_GLOW = 1;
    public static final double MIN_GLOW = 0;

    public static final double SHADOW_BASE = 10;
    public static final double SHADOW_SPREAD = 0.2;

    public static final double HUE_BASE = 200;

    public static final double INNER_CORNER_RADIUS = 10;

    // Ball
    public static final String BALL_IMAGE_PATH = "/images/ball.png";

    public static final double BALL_X = WINDOW_WIDTH / 2;
    public static final double BALL_Y = WINDOW_HEIGHT - 100;
    public static final double BALL_RADIUS = 10;
    public static final double INITIAL_DX = 3.0;
    public static final double INITIAL_DY = -3.0;
    public static final double BALL_START_SPEED = 1.0;
    public static final double STATIONARY_THRESHOLD = 1e-6;
    public static final double SPEED_THRESHOLD = 0.0001;
    public static final double SPEED_AFTER_HIT = 4.0;
    public static final double BALL_FAST_SPEED = 1.25;

    public static final double PADDLE_CENTER_DX = PADDLE_WIDTH / 2;
    public static final double AFTER_HIT_OFFSET = BALL_RADIUS + 0.01;

    // Brick
    public static final Image BRICK_IMG = new Image(GameConstants.class.getResourceAsStream("/images/brick.png"));
    public static final Image STRONGBRICK1_IMG = new Image(GameConstants.class.getResourceAsStream("/images/strong_brick1.png"));
    public static final Image STRONGBRICK2_IMG = new Image(GameConstants.class.getResourceAsStream("/images/strong_brick2.png"));
    public static final Image STRONGBRICK3_IMG = new Image(GameConstants.class.getResourceAsStream("/images/strong_brick3.png"));
    public static final Image SILVERBRICK_IMG = new Image(GameConstants.class.getResourceAsStream("/images/silver_brick.png"));
    public static final Image BOMBBRICK_IMG = new Image(GameConstants.class.getResourceAsStream("/images/bomb_brick.png"));
    public static final int BRICK_HIT_POINTS = 1;
    public static final int STRONGBRICK_HIT_POINTS = 3;
    public static final double BRICK_WIDTH = 70;
    public static final double BRICK_HEIGHT = 25;
    public static final double PADDING = 5;

    // Power-up
    public static final Image EP_IMG = new Image(GameConstants.class.getResourceAsStream("/images/ExpandPaddle.png"));
    public static final Image FB_IMG = new Image(GameConstants.class.getResourceAsStream("/images/FastBall.png"));
    public static final Image EB_IMG = new Image(GameConstants.class.getResourceAsStream("/images/ExtraBall.png"));
    public static final Image EL_IMG = new Image(GameConstants.class.getResourceAsStream("/images/ExtraLife.png"));
    public static final Image SB_IMG = new Image(GameConstants.class.getResourceAsStream("/images/StrongBall.png"));

    public static final double PU_RADIUS = 10;
    public static final double PU_DIAMETER = PU_RADIUS * 2;

    public static final long EP_DURATION = 10000;
    public static final long FB_DURATION = 10000;
    public static final long SB_DURATION = 2500;
    public static final long NO_DURATION = 0;

    public static final int EP_TYPE = 1;
    public static final int FB_TYPE = 2;
    public static final int EB_TYPE = 3;
    public static final int EL_TYPE = 4;
    public static final int SB_TYPE = 5;

    public static final double FALL_SPEED = 2.0;

    // UI - HUD
    public static final String BG_PATH = "images/game_bg.jpg";
    public static final String GAME_TITLE_PATH = "/font/Arka_solid.ttf";
    public static final String INSTRUCTION_PATH = "/font/PressStart2P-Regular.ttf";

    public static final String FONT_NAME = "Times New Roman";

    public static final double GAME_TITLE_FONT_SIZE = 100;
    public static final double TITLE_FONT_SIZE = 55;
    public static final double SCOREBOARD_TITLE_FONT_SIZE = 20;
    public static final double INSTRUCTION_FONT_SIZE = 15;
    public static final double SCORE_FONT_SIZE = 24;
    public static final double LIVES_FONT_SIZE = 20;
    public static final double COMBO_FONT_SIZE = 40;
    public static final double ADD_SCORE_FONT_SIZE = 18;

    public static final double TEXT_X = WINDOW_WIDTH / 2;
    public static final double GAME_TITLE_Y = 150;
    public static final double SCOREBOARD_TITLE_Y = 210;
    public static final double TITLE_Y = WINDOW_HEIGHT / 2 - 50;
    public static final double GUIDE_Y = WINDOW_HEIGHT - 10;
    public static final double MOVE_GUIDE_Y = WINDOW_HEIGHT - 80;
    public static final double SCORE_Y = WINDOW_HEIGHT / 2 + 30;
    public static final double ENTER_NAME_Y = WINDOW_HEIGHT / 2 + 60;
    public static final double SCOREBOARD_X = WINDOW_WIDTH / 2 - 100;
    public static final double SCOREBOARD_START_Y = 250;
    public static final double SCOREBOARD_OFFSET_Y = 40;
    public static final double SCORE_X = 20;
    public static final double LIVES_X = WINDOW_WIDTH - 20;
    public static final double HUD_Y = 36;

    public static final double SCORE_PULSE_MAX = 1.0;
    public static final double SCORE_PULSE_DEC = 0.9;
    public static final double COMBO_ALPHA_DEC = 0.02;
    public static final double COMBO_ALPHA_MIN = 0.01;
    public static final double FRAME_DELTA_TIME = 0.016;

    public static final double TOPBAR_HEIGHT = 56;
    public static final double UNDERLINE_HEIGHT = 2;
    public static final double COMBO_X = WINDOW_WIDTH / 2;
    public static final double COMBO_Y = WINDOW_HEIGHT * 0.22;

    public static final double FLOATING_VELOCITY = -30;
    public static final double INITIAL_TEXT_LIFE = 1.0;

}
