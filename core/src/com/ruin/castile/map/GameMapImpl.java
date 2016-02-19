package com.ruin.castile.map;

import com.badlogic.gdx.math.Vector3;
import com.ruin.castile.util.Vector2i;

/**
 * represents a single game map
 *
 * Created by Prin on 2016/02/15.
 */
public class GameMapImpl implements GameMap {

    Tile[][] tiles;

    GameMapMesh currentMesh;

    public GameMapImpl(Tile[][] tiles) {
        this.tiles = tiles;
    }

    public GameMapImpl(int w, int l) {
        tiles = new Tile[w][l];
        for(int i = 0; i < w; i++) {
            for(int j = 0; j < l; j++) {
                tiles[i][j] = new Tile();
                Tile.ScreenData dat = new Tile.ScreenData(new Vector2i(24 * (getWidth()-i), 24 * (getLength()-j)), new Vector2i(24, 24));
                tiles[i][j].screenData.put(Tile.Screen.UPPER, dat);
                tiles[i][j].addHeight(i*j);
            }
        }
    }

    @Override
    public Tile getTile(int x, int y) {
        return tiles[x][y];
    }

    @Override
    public int getWidth() {
        return tiles.length;
    }

    @Override
    public int getLength() {
        return tiles[0].length;
    }

    @Override
    public int getSize() {
        return getWidth() * getLength();
    }

    @Override
    public void setMesh(GameMapMesh newMesh) {
        currentMesh = newMesh;
    }

    @Override
    public GameMapMesh getMesh() {
        return currentMesh;
    }

    @Override
    public boolean hasMesh() {
        return currentMesh != null;
    }

    @Override
    public float getHeightAtPoint(float x, float y) {
        int indexX = (int)x;
        int indexY = (int)y;

        if(indexX > getWidth()-1 || indexY > getLength()-1 || x < 0 || y < 0)
            return 0;

        if(tiles[indexX][indexY] == null)
            return 0;

        return tiles[indexX][indexY].getHeightAtPoint(x - indexX, y-indexY);
    }

    @Override
    public void setTile(Tile tile, int x, int y) {
        tiles[x][y] = tile;
    }


}
