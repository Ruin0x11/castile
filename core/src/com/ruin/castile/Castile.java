package com.ruin.castile;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TShortList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TShortArrayList;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

public class Castile extends ApplicationAdapter {
    public static final int MAP_WIDTH = 1;
    public static final int MAP_LENGTH = 2;

    public static final int MAP_SIZE = MAP_WIDTH * MAP_LENGTH;

    ShaderProgram shader, shaderB;
    Mesh mesh, meshB;
    Texture texture, textureB;
    Matrix4 matrix = new Matrix4();
    Tile[][] tiles = new Tile[MAP_WIDTH][MAP_LENGTH];
    PerspectiveCamera cam;
    float rotationSpeed = 1f;

    @Override
    public void create () {
        String vertexShader =
                "attribute vec4 a_position;    \n"
                + "attribute vec4 a_color;\n"
                + "attribute vec2 a_texCoord0;\n"
                + "varying vec4 v_color;"
                + "varying vec2 v_texCoords;"
                + "void main()                  \n"
                + "{                            \n"
                + "   v_color = a_color; \n"
                + "   v_texCoords = a_texCoord0; \n"
                + "   gl_Position =  a_position;  \n"
                + "}                            \n";
        String fragmentShader =
                "#ifdef GL_ES\n"
                + "precision mediump float;\n"
                + "#endif\n" + "varying vec4 v_color;\n"
                + "varying vec2 v_texCoords;\n"
                + "uniform sampler2D u_texture;\n"
                + "void main()                                  \n"
                + "{                                            \n"
                + "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n"
                + "}";

        shader = new ShaderProgram(vertexShader, fragmentShader);
        if (shader.isCompiled() == false) {
            Gdx.app.log("ShaderTest", shader.getLog());
            Gdx.app.exit();
        }

        float[] c =        {0,0,0.75f,1,
                            0,0,0.75f,1,
                            0,0,0,1,
                            0,0,0,1};

        mesh = new Mesh(true, 4, 6, VertexAttribute.Position(), VertexAttribute.ColorUnpacked(), VertexAttribute.TexCoords(0));
        mesh.setVertices(new float[]   {-1.0f, -1.0f, 0, c[0], c[1], c[2], c[3], 0, 1,
                                        1.0f, -1.0f, 0,  c[4], c[5], c[6], c[7], 1, 1,
                                        1.0f, 1.0f, 0,   c[8], c[9], c[10], c[11], 1, 0,
                                        -1.0f, 1.0f, 0,  c[12], c[13], c[14], c[15], 0, 0});
        mesh.setIndices(new short[] {0, 1, 2, 2, 3, 0});
        texture = new Texture("default.png");

        String vertexShaderB =
                "uniform mat4 u_mvpMatrix;                   \n"
                        + "attribute vec4 a_position;                  \n"
                        + "attribute vec2 a_texcoords;\n"
                        + "attribute vec4 a_color;\n"
                        + "varying vec2 v_texCoords;\n"
                        + "varying vec4 v_color;\n"
                        + "void main()                                 \n"
                        + "{                                           \n"
                        + "   v_texCoords = a_texcoords;\n"
                        + "   v_color = a_color;\n"
                        + "   gl_Position = u_mvpMatrix * a_position;  \n"
                        + "}                            \n";
        String fragmentShaderB =
                "#ifdef GL_ES\n"
                        + "precision mediump float;\n"
                        + "#endif\n"
                        + "varying vec2 v_texCoords;\n"
                        + "varying vec4 v_color;\n"
                        + "uniform sampler2D u_texture;\n"
                        + "void main()                                  \n"
                        + "{                                            \n"
                        + "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);;\n"
                        + "}";
        // @on

        shaderB = new ShaderProgram(vertexShaderB, fragmentShaderB);
        meshB = genTile();
        meshB.getVertexAttribute(VertexAttributes.Usage.Position).alias = "a_position";
        textureB = new Texture("badlogic.jpg");

        for(int i = 0; i < MAP_WIDTH; i++) {
            for(int j = 0; j < MAP_LENGTH; j++) {
                tiles[i][j] = new Tile();
            }
        }

        cam = new PerspectiveCamera(67, 2f * (4f/3f), 2f);
        cam.position.set(0,0.5f,-1.5f);

        Vector3 axis = new Vector3(0, 1, 0);
        float angle = 180;
        cam.rotate(axis, angle);

    }

    @Override
    public void render () {
        handleInput();
        //matrix.setToRotation(axis, angle);
        cam.update();

        Gdx.gl20.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        Gdx.gl20.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl20.glEnable(GL20.GL_TEXTURE_2D);
        Gdx.gl20.glEnable(GL20.GL_BLEND);
        Gdx.gl20.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        drawBackground();
        drawTerrain();

        Gdx.graphics.setTitle("Frames / Second : " + Gdx.graphics.getFramesPerSecond());
    }

    private void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            cam.translate(0.05f, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            cam.translate(-0.05f, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            cam.translate(0, 0, -0.05f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            cam.translate(0, 0, 0.05f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Z)) {
            cam.translate(0, -0.05f, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            cam.translate(0, 0.05f, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            cam.rotate(-rotationSpeed, 0, 1, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            cam.rotate(rotationSpeed, 0, 1, 0);
        }
    }

    @Override
    public void dispose () {
        mesh.dispose();
        texture.dispose();
        shader.dispose();
    }

    void drawBackground() {
        texture.bind();
        shader.begin();
        shader.setUniformi("u_texture", 0);
        mesh.render(shader, GL20.GL_TRIANGLES);
        shader.end();
    }

    void drawTerrain()
    {
        textureB.bind();
        shaderB.begin();
        shaderB.setUniformi("u_texture", 0);
        shaderB.setUniformMatrix("u_mvpMatrix", cam.combined);
        meshB.render(shaderB, GL20.GL_TRIANGLES);
        shaderB.end();
    }


    public Mesh genTile() {
        Mesh mesh = new Mesh(true, 24 * MAP_SIZE, 36 * MAP_SIZE, new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
                new VertexAttribute(VertexAttributes.Usage.ColorUnpacked, 4, "a_color"),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texcoords"));


        /** Cube vertices */
        short[] indices = {0, 2, 1, 0, 3, 2,
                           4, 5, 6, 4, 6, 7,
                           8, 9, 10, 8, 10, 11,
                           12, 15, 14, 12, 14, 13,
                           16, 17, 18, 16, 18, 19,
                           20, 23, 22, 20, 22, 21};


        //    F ----- G       ^Y
        //   /|      /|       |
        //  E ------H |       |
        //  | |     | |       |------>X
        //  | |B ---|-|C     /
        //  |/      |/      /
        //  A ------D      Z

        TFloatList vertList = new TFloatArrayList(9 * 24 * MAP_SIZE);
        TShortList indList = new TShortArrayList(36 * MAP_SIZE);
        int i = 0;
        for(int x = 0; x < MAP_WIDTH; x++) {
            for(int y = 0; y < MAP_LENGTH; y++) {
                Tile t = tiles[x][y];
                float size = 0.3f;
                Vector3 pta = new Vector3( x - size,  0 - size,  y - size );
                Vector3 ptb = new Vector3( x - size,  0 - size,  y + size );
                Vector3 ptc = new Vector3( x + size,  0 - size,  y + size );
                Vector3 ptd = new Vector3( x + size,  0 - size,  y - size );
                Vector3 pte = new Vector3( x - size,  0 + size,  y - size );
                Vector3 ptf = new Vector3( x - size,  0 + size,  y + size );
                Vector3 ptg = new Vector3( x + size,  0 + size,  y + size );
                Vector3 pth = new Vector3( x + size,  0 + size,  y - size );

                float[] verts = {
                        // bottom (-y)
                        pta.x, pta.y, pta.z, 1, 1, 1, 1, 0, 1,
                        ptb.x, ptb.y, ptb.z, 1, 1, 1, 1, 1, 1,
                        ptc.x, ptc.y, ptc.z, 1, 1, 1, 1, 1, 0,
                        ptd.x, ptd.y, ptd.z, 1, 1, 1, 1, 0, 0,

                        // top (+y)
                        pte.x, pte.y, pte.z, 1, 1, 1, 1, 0, 1,
                        ptf.x, ptf.y, ptf.z, 1, 1, 1, 1, 1, 1,
                        ptg.x, ptg.y, ptg.z, 1, 1, 1, 1, 1, 0,
                        pth.x, pth.y, pth.z, 1, 1, 1, 1, 0, 0,

                        // back (-z)
                        pta.x, pta.y, pta.z, 1, 1, 1, 1, 0, 1,
                        pte.x, pte.y, pte.z, 1, 1, 1, 1, 1, 1,
                        pth.x, pth.y, pth.z, 1, 1, 1, 1, 1, 0,
                        ptd.x, ptd.y, ptd.z, 1, 1, 1, 1, 0, 0,

                        // front (+z)
                        ptb.x, ptb.y, ptb.z, 1, 1, 1, 1, 0, 1,
                        ptf.x, ptf.y, ptf.z, 1, 1, 1, 1, 1, 1,
                        ptg.x, ptg.y, ptg.z, 1, 1, 1, 1, 1, 0,
                        ptc.x, ptc.y, ptc.z, 1, 1, 1, 1, 0, 0,

                        // left (-x)
                        pta.x, pta.y, pta.z, 1, 1, 1, 1, 0, 1,
                        ptb.x, ptb.y, ptb.z, 1, 1, 1, 1, 1, 1,
                        ptf.x, ptf.y, ptf.z, 1, 1, 1, 1, 1, 0,
                        pte.x, pte.y, pte.z, 1, 1, 1, 1, 0, 0,

                        // right (+x)
                        ptd.x, ptd.y, ptd.z, 1, 1, 1, 1, 0, 1,
                        ptc.x, ptc.y, ptc.z, 1, 1, 1, 1, 1, 1,
                        ptg.x, ptg.y, ptg.z, 1, 1, 1, 1, 1, 0,
                        pth.x, pth.y, pth.z, 1, 1, 1, 1, 0, 0,
                };
                vertList.add(verts);
                short[] curIndices = new short[indices.length];
                for(int j = 0; j < indices.length; j++) {
                    curIndices[j] = (short) (indices[j] + (36*i));
                }
                indList.add(curIndices);
                i++;
            }
        }

        System.out.println(vertList.size() + " " + indList.size());
        mesh.setVertices(vertList.toArray());
        mesh.setIndices(indList.toArray());

        return mesh;
    }
}
