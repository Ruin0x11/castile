package com.ruin.castile.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.ruin.castile.map.GameMap;
import com.ruin.castile.map.GameMapImpl;
import com.ruin.castile.map.GameMapMesh;
import com.ruin.castile.map.Tile;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by ruin on 16/02/19.
 */
public class MPDLoader {
    public static GameMap loadMPD(String filename) {
        FileHandle file = Gdx.files.internal(filename);

        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(
                    file.read()));

            GameMap map = new GameMapImpl(48, 48);

            char[][] texCoordSides = new char[7][6]
            for(int i = 0; i < 7; i++) {
                for(int j = 0; j < 6; j++) {
                    texCoordSides[i][j] = in.readChar();
                }
            }

            char[][] heights = new char[3][4];
            for(int i = 0; i < 3; i++) {
                for(int j = 0; j < 4; j++) {
                    heights[i][j] = in.readChar();
                }
            }

            Tile tile = new Tile();
            tile.setHeight(Tile.Corner.UPPER_NORTH_WEST, heights[0][0]);
            tile.setHeight(Tile.Corner.UPPER_SOUTH_WEST, heights[0][1]);
            tile.setHeight(Tile.Corner.UPPER_SOUTH_EAST, heights[0][2]);
            tile.setHeight(Tile.Corner.UPPER_NORTH_EAST, heights[0][3]);

            char work = in.readChar();
            char xCoord = in.readChar();
            char yCoord = in.readChar();
            map.setTile(tile, xCoord, yCoord);
            char moveRestriction = in.readChar();
            char unk = in.readChar();
            short huh = in.readShort();
            in.skipBytes(6);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
