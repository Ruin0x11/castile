package com.ruin.castile.map;

/**
 * Created by Prin on 2016/02/15.
 */
public interface GameMap {

    Tile getTile(int x, int y);

    int getWidth();

    int getLength();

    int getSize();

    void setMesh(GameMapMesh newMesh);

    GameMapMesh getMesh();

    boolean hasMesh();

    float getHeightAtPoint(float x, float y);

}
