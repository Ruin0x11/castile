package com.ruin.castile;

import java.util.EnumMap;

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

    public EnumMap<Corner, Integer> heightData;

    public Tile() {
        heightData = new EnumMap<Corner, Integer>(Corner.class);
        
        heightData.put(Corner.UPPER_NORTH_WEST, 5);
        heightData.put(Corner.UPPER_NORTH_EAST, 5);
        heightData.put(Corner.UPPER_SOUTH_WEST, 5);
        heightData.put(Corner.UPPER_SOUTH_EAST, 5);
        heightData.put(Corner.LOWER_NORTH_EAST, 0);
        heightData.put(Corner.LOWER_NORTH_WEST, 0);
        heightData.put(Corner.LOWER_SOUTH_WEST, 0);
        heightData.put(Corner.LOWER_SOUTH_EAST, 0);
    }

    public enum Corner {
        UPPER_NORTH_WEST,
        UPPER_NORTH_EAST,
        UPPER_SOUTH_WEST,
        UPPER_SOUTH_EAST,
        LOWER_NORTH_WEST,
        LOWER_NORTH_EAST,
        LOWER_SOUTH_WEST,
        LOWER_SOUTH_EAST
    }
}
