package arkanoid;

import java.util.ArrayList;
import java.util.List;

public final class LevelBuilder {
    public static final int NORMAL = 0;
    public static final int INDESTRUCTIBLE = 1;
    public static final int EMPTY = -1;

    public static final int[][] LEVEL1 = new int[][]{
            {1,1,1,1,1,1,1,1,1},
            {1,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,1},
            {1,1,1,1,-1,1,1,1,1}
    };

    public static List<Brick> build(int[][] layout, double gameWidth, double offsetY) {
        double padding = 5.0;
        double brickHeight = 25.0;
        int normalPoints = 20;
        return build(layout, gameWidth, offsetY, padding, brickHeight, normalPoints);
    }

    public static List<Brick> build(int[][] layout, double gameWidth, double offsetY,
                                    double padding, double brickHeight, int normalPoints) {
        List<Brick> bricks = new ArrayList<>();
        int rows = layout.length;
        if (rows == 0) return bricks;
        int cols = layout[0].length;

        double totalPadding = (cols - 1) * padding;
        double brickWidth = Math.floor((gameWidth - totalPadding) / cols);
        double totalWidth = cols * brickWidth + totalPadding;
        double offsetX = (gameWidth - totalWidth) / 2.0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int cell = layout[r][c];
                if (cell == EMPTY) continue;

                boolean indestructible = (cell == INDESTRUCTIBLE);
                int points = indestructible ? 0 : normalPoints;
                double x = offsetX + c * (brickWidth + padding);
                double y = offsetY + r * (brickHeight + padding);
                bricks.add(new Brick(x, y, brickWidth, brickHeight, points, indestructible));
            }
        }
        return bricks;
    }

    private LevelBuilder() {}
}
