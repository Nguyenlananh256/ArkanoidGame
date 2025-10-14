package game;

public class Player {
    public static final int INITIAL_SCORE = 0;
    public static final int INITIAL_LIVES = 3;
    public static final int INITIAL_LEVEL = 1;

    private int score;
    private int lives;
    private int level;

    public Player() {
        reset();
    }

    public int getScore() {
        return score;
    }

    public int getLives() {
        return lives;
    }

    public int getLevel() {
        return level;
    }

    public void scoreUp(int s) {
        score += s;
    }

    public void loseLife() {
        lives--;
    }

    public void levelUp() {
        level++;
    }

    public void reset() {
        this.score = INITIAL_SCORE;
        this.lives = INITIAL_LIVES;
        this.level = INITIAL_LEVEL;
    }
}
