package com.arkanoid;

import com.arkanoid.BombBrick;
import com.arkanoid.SilverBrick;
import com.arkanoid.StrongBrick;

import java.util.ArrayList;
import java.util.List;

public class LevelManager {
    private final double width;
    private final double height;

    // Cấu hình lưới hiện hành của level đã build
    private int rows;
    private int cols;

    // Kích thước gạch mặc định
    private double brickWidth = 70;
    private double brickHeight = 25;
    private double padding = 5;
    private double offsetY = 60;

    public LevelManager(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public List<Brick> buildLevel(int level) {
        // Mặc định 6x10, có thể đổi riêng từng level
        rows = 6;
        cols = 10;

        double offsetX = (width - (cols * (brickWidth + padding))) / 2;

        List<Brick> bricks = new ArrayList<>();
        int[] points = {60, 50, 40, 30, 20, 10};

        switch (level) {
            case 1:
                buildLevel1(bricks, offsetX, points);
                break;
            case 2:
                buildLevel2(bricks, offsetX, points);
                break;
            case 3:
            default:
                buildLevel3(bricks, offsetX, points);
                break;
        }
        return bricks;
    }

    private void buildLevel1(List<Brick> bricks, double offsetX, int[] points) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double x = offsetX + col * (brickWidth + padding);
                double y = offsetY + row * (brickHeight + padding);

                if (row == 0) {
                    bricks.add(new SilverBrick(x, y, brickWidth, brickHeight, 0)); // hàng chắn trên
                } else if (row == 1) {
                    bricks.add(new StrongBrick(x, y, brickWidth, brickHeight, points[row])); // hàng mạnh
                } else if (row == 2 && col % 4 == 1) {
                    bricks.add(new BombBrick(x, y, brickWidth, brickHeight, points[row])); // bom thưa
                } else {
                    bricks.add(new Brick(x, y, brickWidth, brickHeight, points[row])); // thường
                }
            }
        }
    }

    private void buildLevel2(List<Brick> bricks, double offsetX, int[] points) {
        // Hai hàng Silver trên cùng, Strong dày hơn, Bomb rải đều hơn
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double x = offsetX + col * (brickWidth + padding);
                double y = offsetY + row * (brickHeight + padding);

                if (row <= 1) {
                    bricks.add(new SilverBrick(x, y, brickWidth, brickHeight, 0));
                } else if (row == 2 || row == 3) {
                    bricks.add(new StrongBrick(x, y, brickWidth, brickHeight, points[row]));
                } else if (row == 4 && col % 3 == 1) {
                    bricks.add(new BombBrick(x, y, brickWidth, brickHeight, points[row]));
                } else if (row == 5 && (col % 2 == 0)) {
                    bricks.add(new StrongBrick(x, y, brickWidth, brickHeight, points[row]));
                } else {
                    bricks.add(new Brick(x, y, brickWidth, brickHeight, points[row]));
                }
            }
        }
    }

    private void buildLevel3(List<Brick> bricks, double offsetX, int[] points) {
        // Mức khó: vành Silver, Strong dày, Bomb dạng caro
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double x = offsetX + col * (brickWidth + padding);
                double y = offsetY + row * (brickHeight + padding);

                boolean border = (row == 0) || (row == rows - 1) || (col == 0) || (col == cols - 1);
                if (border) {
                    bricks.add(new SilverBrick(x, y, brickWidth, brickHeight, 0));
                } else if ((row + col) % 2 == 0) {
                    bricks.add(new BombBrick(x, y, brickWidth, brickHeight, points[Math.min(row, points.length - 1)]));
                } else {
                    bricks.add(new StrongBrick(x, y, brickWidth, brickHeight, points[Math.min(row, points.length - 1)]));
                }
            }
        }
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
}
