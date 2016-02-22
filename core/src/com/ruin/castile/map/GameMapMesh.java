package com.ruin.castile.map;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.ruin.castile.util.Rect2f;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TShortList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TShortArrayList;

/**
 * light wrapper around Mesh
 *
 * Created by Prin on 2016/02/15.
 */
public class GameMapMesh {

    TextureArray textureMap;

    //GameMapMeshElements elements;

    Mesh mesh;

    public GameMapMesh(Mesh mesh, TextureArray textureMap) {
        this.mesh = mesh;
        this.textureMap = textureMap;
    }

    public void render(ShaderProgram shader, int primitiveType) {
        textureMap.bind();
        mesh.render(shader, primitiveType);
    }

    public void dispose() {
        mesh.dispose();
        textureMap.dispose();
    }

    public Mesh getMesh() { return this.mesh; }

    public TextureArray getTextureMap() { return this.textureMap; }

    public static class GameMapMeshElements {

        int vertexCount;
        TFloatList coords;
        TFloatList colors;
        TFloatList texCoords;

        public GameMapMeshElements() {
            vertexCount = 0;
            coords = new TFloatArrayList();
            colors = new TFloatArrayList();
            texCoords = new TFloatArrayList();
        }
    }
}
