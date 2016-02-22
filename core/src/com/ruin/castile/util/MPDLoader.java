package com.ruin.castile.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.common.io.LittleEndianDataInputStream;
import com.ruin.castile.map.GameMap;
import com.ruin.castile.map.GameMapImpl;
import com.ruin.castile.map.GameMapMesh;
import com.ruin.castile.map.Tile;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * load .mpd geometry from PS2 Nippon Ichi games
 *
 * Created by ruin on 16/02/19.
 */
public class MPDLoader {
    public static GameMap load(String filename) {
        FileHandle file = Gdx.files.internal(filename);

        GameMap map = new GameMapImpl(48, 48);

        try {
            LittleEndianDataInputStream in = new LittleEndianDataInputStream(new BufferedInputStream(
                    file.read()));

            int numTiles = in.readUnsignedShort();
            System.out.println(numTiles);
            in.skipBytes(16);
            int[][] colors = new int[4][4];
            for(int i = 0; i < 4; i++) {
                for(int j = 0; j < 4; j++) {
                    colors[i][j] = in.readUnsignedByte();
                }
            }

            in.skipBytes(30);
            while(numTiles > 0) {
                int[][] texCoordSides = new int[7][6];
                for (int i = 0; i < 7; i++) {
                        texCoordSides[i][0] = in.readUnsignedByte();
                        texCoordSides[i][1] = in.readUnsignedByte();
                        texCoordSides[i][2] = in.readUnsignedByte();
                        texCoordSides[i][3] = in.readUnsignedByte();
                        texCoordSides[i][4] = in.readUnsignedByte();
                        texCoordSides[i][5] = in.readUnsignedByte();
                }

                int[][] heights = new int[3][4];
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 4; j++) {
                        heights[i][j] = 0xff - in.readUnsignedByte();
                    }
                }

                int work = in.readUnsignedByte();
                int zCoord = in.readUnsignedByte();
                int xCoord = in.readUnsignedByte();
                int moveRestriction = in.readUnsignedByte();
                int unk = in.readUnsignedByte();
                int testOne = in.readUnsignedByte();
                int testTwo = in.readUnsignedByte();
                if(testOne == 10 && testTwo == 10) {
                    Tile tile = new Tile();
                    tile.setHeight(Tile.Corner.UPPER_SOUTH_WEST, heights[0][0]);
                    tile.setHeight(Tile.Corner.UPPER_SOUTH_EAST, heights[0][1]);
                    tile.setHeight(Tile.Corner.UPPER_NORTH_WEST, heights[0][2]);
                    tile.setHeight(Tile.Corner.UPPER_NORTH_EAST, heights[0][3]);
                    tile.setAddHeight(moveRestriction);

                    for(Tile.Screen screen : Tile.Screen.values()) {
                        int i = screen.ordinal();
                        if(texCoordSides[i][2] == 0 || texCoordSides[i][2] == 0)
                            continue;
                        tile.setScreenData(screen, new Tile.ScreenData(texCoordSides[i][4] % 2,
                                texCoordSides[i][0], texCoordSides[i][1], texCoordSides[i][2], texCoordSides[i][3]));
                    }

                    map.setTile(tile, xCoord, zCoord);
                    numTiles -= 1;
                }
                in.skipBytes(3);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }
}
