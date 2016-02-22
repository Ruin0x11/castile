package com.ruin.castile.map;

import com.badlogic.gdx.math.Vector3;
import com.ruin.castile.util.Vector2i;

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
        UPPER_SOUTH_WEST,
        UPPER_SOUTH_EAST,
        UPPER_NORTH_WEST,
        UPPER_NORTH_EAST,
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
        WEST,
        EAST,
        NORTH,
        SOUTH,
        UPPER,
        UPPER_SHADOW_ONE,
        UPPER_SHADOW_TWO,
    }

    public static final int DEFAULT_DM = 10;

    float size = 0.5f;

    int addHeight = 0;

    public EnumMap<Corner, Integer> heightData;

    public EnumMap<Screen, ScreenData> screenData;

    public Tile() {
        this(DEFAULT_DM);
    }

    public Tile(int height) {
        this(height, height, height, height);
    }

    public Tile(int nw, int ne, int sw, int se) {
        heightData = new EnumMap<Corner, Integer>(Corner.class);

        heightData.put(Corner.UPPER_NORTH_WEST, nw);
        heightData.put(Corner.UPPER_NORTH_EAST, ne);
        heightData.put(Corner.UPPER_SOUTH_WEST, sw);
        heightData.put(Corner.UPPER_SOUTH_EAST, se);
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

    public int getHeight(Corner corner) {
        return heightData.get(corner);
    }

    public int getHeight() {
        return ((heightData.get(Corner.UPPER_NORTH_WEST) +
                heightData.get(Corner.UPPER_NORTH_EAST) +
                heightData.get(Corner.UPPER_SOUTH_WEST) +
                heightData.get(Corner.UPPER_SOUTH_EAST)) / 4);
    }

    public int getCombinedHeight() { return getHeight() + this.addHeight; }

    public void setAddHeight(int addHeight) { this.addHeight = addHeight; }
    public int getAddHeight(int addHeight) { return this.addHeight; }

    public float getHeightAtPoint(float x, float z) {
        Vector3 a, b, c;
        if(x > z) {
            // northwestern triangle
            a = new Vector3(1, getHeight(Corner.UPPER_NORTH_WEST), 0);
            b = new Vector3(0, getHeight(Corner.UPPER_SOUTH_EAST), 0);
            c = new Vector3(1, getHeight(Corner.UPPER_NORTH_EAST), 1);
        }
        else {
            // southeastern triangle
            a = new Vector3(0, getHeight(Corner.UPPER_NORTH_WEST), 1);
            b = new Vector3(0, getHeight(Corner.UPPER_SOUTH_WEST), 0);
            c = new Vector3(1, getHeight(Corner.UPPER_NORTH_EAST), 1);
        }

        float i = -(c.z*b.y-a.z*b.y-c.z*a.y+
                a.y*b.z+c.y*a.z-b.z*c.y);
        float j = (a.z*c.x+b.z*a.x+c.z*b.x-
                b.z*c.x-a.z*b.x-c.z*a.x);
        float k = (b.y*c.x+a.y*b.x+c.y*a.x-
                a.y*c.x-b.y*a.x-b.x*c.y);
        float l = -i*a.x-j*a.y-k*a.z;
        return (-(i*x+k*z+l)/j);
    }

    public float getCombinedHeightAtPoint(float x, float z) {
        return getHeightAtPoint(x, z) + this.addHeight;
    }

    public void addHeight(int dm) {
        addHeight(Corner.UPPER_NORTH_WEST, dm);
        addHeight(Corner.UPPER_NORTH_EAST, dm);
        addHeight(Corner.UPPER_SOUTH_WEST, dm);
        addHeight(Corner.UPPER_SOUTH_EAST, dm);
    }

    public void addHeight(Corner corner, int dm) {
        heightData.put(corner, heightData.get(corner) + dm);
    }

    public void setHeight(Corner corner, int dm) {
        heightData.put(corner, dm);
    }

    public void setScreenData(Screen screen, ScreenData data) {
        this.screenData.put(screen, data);
    }

    public static class ScreenData {
        public int texUnit;
        public Vector2i texCoords;
        public Vector2i texRegion;

        public ScreenData() {
            texUnit = 0;
            texCoords = new Vector2i(0, 0);
            texRegion = new Vector2i(24, 24);
        }

        public ScreenData(int unit, Vector2i coords, Vector2i region) {
            texUnit = unit;
            texCoords = coords;
            texRegion = region;
        }

        public ScreenData(int unit, int x, int y, int w, int h) {
            texUnit = unit;
            texCoords = new Vector2i(x, y);
            texRegion = new Vector2i(w, h);
        }

    }
}
