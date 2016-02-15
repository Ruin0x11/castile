package com.ruin.castile;

import java.util.EnumMap;

/**
 * describes a single tile of geometry.
 *
 * Created by Prin on 2016/02/13.
 */
public class Tile {

    // southwestern tile represents (0,0) in (x,z)
    // east increases x, north increases z
    // PS2 map size limit is 48 x 48

    // each side ("screen") defines texture region on a 256x256 texture sheet
    // and rotation/translation/transparency, brightness/color
    // tiles are 24 x 24 but the region area is free

    // some reason to believe tile width is ~10 Dm

    // the four upper corners of the tile determine height in Dm
    // the eight lower corners correspond to height of a single lower point on a face (cosmetic)

    // also two additional "upper" shadow textures

    public enum Corner {
        UPPER_NORTH_WEST,
        UPPER_NORTH_EAST,
        UPPER_SOUTH_WEST,
        UPPER_SOUTH_EAST,
        LOWER_NORTH_NORTH_WEST,
        LOWER_NORTH_NORTH_EAST,
        LOWER_WEST_NORTH_WEST,
        LOWER_WEST_SOUTH_WEST,
        LOWER_SOUTH_SOUTH_WEST,
        LOWER_SOUTH_SOUTH_EAST,
        LOWER_EAST_NORTH_EAST,
        LOWER_EAST_SOUTH_EAST,
    }

    public enum Screen {
        NORTH,
        SOUTH,
        EAST,
        WEST,
        UPPER,
        UPPER_SHADOW_ONE,
        UPPER_SHADOW_TWO,
    }

    float size = 0.5f;

    public EnumMap<Corner, Integer> heightData;

    public EnumMap<Screen, ScreenData> screenData;

    public Tile() {
        heightData = new EnumMap<Corner, Integer>(Corner.class);
        
        heightData.put(Corner.UPPER_NORTH_WEST, 10);
        heightData.put(Corner.UPPER_NORTH_EAST, 10);
        heightData.put(Corner.UPPER_SOUTH_WEST, 10);
        heightData.put(Corner.UPPER_SOUTH_EAST, 10);
        heightData.put(Corner.LOWER_NORTH_NORTH_WEST, 0);
        heightData.put(Corner.LOWER_NORTH_NORTH_EAST, 0);
        heightData.put(Corner.LOWER_WEST_NORTH_WEST, 0);
        heightData.put(Corner.LOWER_WEST_SOUTH_WEST, 0);
        heightData.put(Corner.LOWER_SOUTH_SOUTH_WEST, 0);
        heightData.put(Corner.LOWER_SOUTH_SOUTH_EAST, 0);
        heightData.put(Corner.LOWER_EAST_NORTH_EAST, 0);
        heightData.put(Corner.LOWER_EAST_SOUTH_EAST, 0);

        screenData = new EnumMap<Screen, ScreenData>(Screen.class);

        for (Screen screen : Screen.values()) {
            screenData.put(screen, new ScreenData());
        }
    }

    public int getHeight() {
        return (heightData.get(Corner.UPPER_NORTH_WEST) +
                heightData.get(Corner.UPPER_NORTH_EAST) +
                heightData.get(Corner.UPPER_SOUTH_WEST) +
                heightData.get(Corner.UPPER_SOUTH_EAST)) / 4;
    }

    public void addHeight(int dm) {
        heightData.put(Corner.UPPER_NORTH_WEST, heightData.get(Corner.UPPER_NORTH_WEST) + dm);
        heightData.put(Corner.UPPER_NORTH_EAST, heightData.get(Corner.UPPER_NORTH_EAST) + dm);
        heightData.put(Corner.UPPER_SOUTH_WEST, heightData.get(Corner.UPPER_SOUTH_WEST) + dm);
        heightData.put(Corner.UPPER_SOUTH_EAST, heightData.get(Corner.UPPER_SOUTH_EAST) + dm);
    }

    public static class ScreenData {
        public int texUnit;
        public Vector2i texCoords;
        public Vector2i texRegion;

        public ScreenData() {
            texUnit = 0;
            texCoords = new Vector2i(154, 308);
            texRegion = new Vector2i(24, 96);
        }

        public ScreenData(Vector2i coords, Vector2i region) {
            texUnit = 0;
            texCoords = coords;
            texRegion = region;
        }

    }
}
