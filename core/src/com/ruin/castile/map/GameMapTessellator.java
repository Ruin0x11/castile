package com.ruin.castile.map;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Vector3;
import com.ruin.castile.util.Rect2f;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TShortList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TShortArrayList;

/**
 * Created by Prin on 2016/02/15.
 */
public class GameMapTessellator {

    public GameMapMesh generateMapMesh(GameMap map) {
        Mesh mesh = new Mesh(true, 24 * map.getSize(), 36 * map.getSize(), new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
                new VertexAttribute(VertexAttributes.Usage.ColorUnpacked, 4, "a_color"),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texcoords"));

        Texture textureMap = new Texture("ground.png");

        /** Cube vertices */
        short[] indices = {0, 2, 1, 0, 3, 2,
                4, 5, 6, 4, 6, 7,
                8, 9, 10, 8, 10, 11,
                12, 15, 14, 12, 14, 13,
                16, 17, 18, 16, 18, 19,
                20, 23, 22, 20, 22, 21};


        //    F ----- G
        //   /|      /|
        //  E ------H |   ^ Y  Z
        //  | |     | |   |   /
        //  | |B ---|-|C  |  /
        //  |/      |/    | /
        //  A ------D     |------>X

        TFloatList vertList = new TFloatArrayList(9 * 24 * map.getSize());
        TShortList indList = new TShortArrayList(36 * map.getSize());
        int i = 0;
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getLength(); y++) {
                Tile t = map.getTile(x, y);
                float size = 0.5f;

                Vector3 pta = new Vector3(x - size, 0, y - size);
                Vector3 ptb = new Vector3(x - size, 0, y + size);
                Vector3 ptc = new Vector3(x + size, 0, y + size);
                Vector3 ptd = new Vector3(x + size, 0, y - size);
                Vector3 pte = new Vector3(x - size, 0.1f * t.heightData.get(Tile.Corner.UPPER_SOUTH_WEST), y - size);
                Vector3 ptf = new Vector3(x - size, 0.1f * t.heightData.get(Tile.Corner.UPPER_SOUTH_EAST), y + size);
                Vector3 ptg = new Vector3(x + size, 0.1f * t.heightData.get(Tile.Corner.UPPER_NORTH_EAST), y + size);
                Vector3 pth = new Vector3(x + size, 0.1f * t.heightData.get(Tile.Corner.UPPER_NORTH_WEST), y - size);

                Rect2f[] texReg = new Rect2f[5];
                texReg[0] = getTextureBounds(t.screenData.get(Tile.Screen.UPPER), textureMap);
                texReg[1] = getTextureBounds(t.screenData.get(Tile.Screen.SOUTH), textureMap);
                texReg[2] = getTextureBounds(t.screenData.get(Tile.Screen.NORTH), textureMap);
                texReg[3] = getTextureBounds(t.screenData.get(Tile.Screen.WEST), textureMap);
                texReg[4] = getTextureBounds(t.screenData.get(Tile.Screen.EAST), textureMap);

                float[] verts = {
                        // bottom (-y)
                        pta.x, pta.y, pta.z, 1, 1, 1, 1, 0, 0,
                        ptb.x, ptb.y, ptb.z, 1, 1, 1, 1, 0, 0,
                        ptc.x, ptc.y, ptc.z, 1, 1, 1, 1, 0, 0,
                        ptd.x, ptd.y, ptd.z, 1, 1, 1, 1, 0, 0,

                        // top (+y)
                        pte.x, pte.y, pte.z, 1, 1, 1, 1, texReg[0].maxX(), texReg[0].maxY(),
                        ptf.x, ptf.y, ptf.z, 1, 1, 1, 1, texReg[0].maxX(), texReg[0].minY(),
                        ptg.x, ptg.y, ptg.z, 1, 1, 1, 1, texReg[0].minX(), texReg[0].minY(),
                        pth.x, pth.y, pth.z, 1, 1, 1, 1, texReg[0].minX(), texReg[0].maxY(),

                        // back (-z) (south)
                        pta.x, pta.y, pta.z, 1, 1, 1, 1, texReg[1].maxX(), texReg[1].maxY(),
                        pte.x, pte.y, pte.z, 1, 1, 1, 1, texReg[1].maxX(), texReg[1].minY(),
                        pth.x, pth.y, pth.z, 1, 1, 1, 1, texReg[1].minX(), texReg[1].minY(),
                        ptd.x, ptd.y, ptd.z, 1, 1, 1, 1, texReg[1].minX(), texReg[1].maxY(),

                        // front (+z) (north)
                        ptb.x, ptb.y, ptb.z, 1, 1, 1, 1, texReg[2].minX(), texReg[2].maxY(),
                        ptf.x, ptf.y, ptf.z, 1, 1, 1, 1, texReg[2].minX(), texReg[2].minY(),
                        ptg.x, ptg.y, ptg.z, 1, 1, 1, 1, texReg[2].maxX(), texReg[2].minY(),
                        ptc.x, ptc.y, ptc.z, 1, 1, 1, 1, texReg[2].maxX(), texReg[2].maxY(),

                        // left (-x) (west)
                        pta.x, pta.y, pta.z, 1, 1, 1, 1, texReg[3].minX(), texReg[3].maxY(),
                        ptb.x, ptb.y, ptb.z, 1, 1, 1, 1, texReg[3].maxX(), texReg[3].maxY(),
                        ptf.x, ptf.y, ptf.z, 1, 1, 1, 1, texReg[3].maxX(), texReg[3].minY(),
                        pte.x, pte.y, pte.z, 1, 1, 1, 1, texReg[3].minX(), texReg[3].minY(),

                        // right (+x) (east)
                        ptd.x, ptd.y, ptd.z, 1, 1, 1, 1, texReg[4].maxX(), texReg[4].maxY(),
                        ptc.x, ptc.y, ptc.z, 1, 1, 1, 1, texReg[4].minX(), texReg[4].maxY(),
                        ptg.x, ptg.y, ptg.z, 1, 1, 1, 1, texReg[4].minX(), texReg[4].minY(),
                        pth.x, pth.y, pth.z, 1, 1, 1, 1, texReg[4].maxX(), texReg[4].minY(),
                };
                vertList.add(verts);
                short[] curIndices = new short[indices.length];
                for (int j = 0; j < indices.length; j++) {
                    curIndices[j] = (short) (indices[j] + (24 * i));
                }
                indList.add(curIndices);
                i++;
            }
        }
        mesh.setVertices(vertList.toArray());
        mesh.setIndices(indList.toArray());

        mesh.getVertexAttribute(VertexAttributes.Usage.Position).alias = "a_position";

        return new GameMapMesh(mesh, textureMap);
    }

    Rect2f getTextureBounds(Tile.ScreenData data, Texture textureMap) {
        float minX = (float) data.texCoords.x / (float) textureMap.getWidth();
        float minY = (float) data.texCoords.y / (float) textureMap.getHeight();
        float width = 1.0f / (textureMap.getWidth() / data.texRegion.x);
        float height = 1.0f / (textureMap.getHeight() / data.texRegion.y);
        return Rect2f.createFromMinAndSize(minX, minY, width, height);
    }
}
