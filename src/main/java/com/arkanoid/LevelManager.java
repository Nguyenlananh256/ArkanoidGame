package com.arkanoid;

import com.arkanoid.BombBrick;
import com.arkanoid.SilverBrick;
import com.arkanoid.StrongBrick;

import java.util.ArrayList;
import java.util.List;

public class LevelManager {
    // Cấu hình lưới hiện hành của level đã build
    private int rows;
    private int cols;

    private double offsetY = 60;

    public List<Brick> buildLevel(int level) {
        rows = GameConstants.DEFAULT_BRICK_ROWS;
        cols = GameConstants.DEFAULT_BRICK_COLS;

        double offsetX = (GameConstants.WINDOW_WIDTH - (cols * (GameConstants.BRICK_WIDTH + GameConstants.PADDING))) / 2;

        List<Brick> bricks = new ArrayList<>();

        switch (level) {
            case 1:
                buildLevel1(bricks, offsetX, GameConstants.POINTS);
                break;
            case 2:
                buildLevel2(bricks, offsetX, GameConstants.POINTS);
                break;
            default:
                buildLevel3(bricks, offsetX, GameConstants.POINTS);
                break;
        }
        return bricks;
    }

    private void buildLevel1(List<Brick> bricks, double offsetX, int[] points) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double x = offsetX + col * (GameConstants.BRICK_WIDTH + GameConstants.PADDING);
                double y = offsetY + row * (GameConstants.BRICK_HEIGHT + GameConstants.PADDING);

                if (row == 0) {
                    bricks.add(new SilverBrick(x, y, GameConstants.BRICK_WIDTH, GameConstants.BRICK_HEIGHT, 0)); // hàng chắn trên
                }


            }
        }
    }

    private void buildLevel2(List<Brick> bricks, double offsetX, int[] points) {
        // Hai hàng Silver trên cùng, Strong dày hơn, Bomb rải đều hơn
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double x = offsetX + col * (GameConstants.BRICK_WIDTH + GameConstants.PADDING);
                double y = offsetY + row * (GameConstants.BRICK_HEIGHT + GameConstants.PADDING);

                if (row <= 1) {
                    bricks.add(new SilverBrick(x, y, GameConstants.BRICK_WIDTH, GameConstants.BRICK_HEIGHT, 0));
                } else if (row == 2 || row == 3) {
                    bricks.add(new StrongBrick(x, y, GameConstants.BRICK_WIDTH, GameConstants.BRICK_HEIGHT, points[row]));
                } else if (row == 4 && col % 3 == 1) {
                    bricks.add(new BombBrick(x, y, GameConstants.BRICK_WIDTH, GameConstants.BRICK_HEIGHT, points[row]));
                } else if (row == 5 && (col % 2 == 0)) {
                    bricks.add(new StrongBrick(x, y, GameConstants.BRICK_WIDTH, GameConstants.BRICK_HEIGHT, points[row]));
                } else {
                    bricks.add(new Brick(x, y, GameConstants.BRICK_WIDTH, GameConstants.BRICK_HEIGHT, points[row]));
                }
            }
        }
    }

    private void buildLevel3(List<Brick> bricks, double offsetX, int[] points) {
        // Mức khó: vành Silver, Strong dày, Bomb dạng caro
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double x = offsetX + col * (GameConstants.BRICK_WIDTH + GameConstants.PADDING);
                double y = offsetY + row * (GameConstants.BRICK_HEIGHT + GameConstants.PADDING);

                boolean border = (row == 0) || (row == rows - 1) || (col == 0) || (col == cols - 1);
                if (border) {
                    bricks.add(new StrongBrick(x, y, GameConstants.BRICK_WIDTH, GameConstants.BRICK_HEIGHT, points[Math.min(row, points.length - 1)]));
                } else if ((row + col) % 2 == 0) {
                    bricks.add(new BombBrick(x, y, GameConstants.BRICK_WIDTH, GameConstants.BRICK_HEIGHT, points[Math.min(row, points.length - 1)]));
                } else {
                    bricks.add(new SilverBrick(x, y, GameConstants.BRICK_WIDTH, GameConstants.BRICK_HEIGHT, 0));
                }
            }
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }
}
