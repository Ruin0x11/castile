package com.ruin.castile;

/**
 * Created by Prin on 2016/02/13.
 */
public class Tile {
    public static final int HEIGHT_UPPER = 0;
    public static final int HEIGHT_LOWER = 1;

    public static final int CORNER_NW = 0;
    public static final int CORNER_NE = 1;
    public static final int CORNER_SW = 2;
    public static final int CORNER_SE = 3;

    float size = 0.5f;

    public int[][] data;

    public Tile() {
        data = new int[2][4];

        data[HEIGHT_UPPER][CORNER_NW] = 10;
        data[HEIGHT_UPPER][CORNER_NE] = 10;
        data[HEIGHT_UPPER][CORNER_SW] = 10;
        data[HEIGHT_UPPER][CORNER_SE] = 10;
        data[HEIGHT_LOWER][CORNER_NW] = 0;
        data[HEIGHT_LOWER][CORNER_NE] = 0;
        data[HEIGHT_LOWER][CORNER_SW] = 0;
        data[HEIGHT_LOWER][CORNER_SE] = 0;
    }
}
