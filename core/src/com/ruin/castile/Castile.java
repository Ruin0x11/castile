package com.ruin.castile;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import java.nio.FloatBuffer;

public class Castile extends ApplicationAdapter {
    ShaderProgram shader, shaderB;
    Mesh mesh, meshB;
    Texture texture, textureB;
    Matrix4 matrix = new Matrix4();
    Tile[][] tiles = new Tile[1][1];

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
        tiles[0][0] = new Tile();
    }

    Vector3 axis = new Vector3(1, 1, 0);
    float angle = 0;

    @Override
    public void render () {
        angle += Gdx.graphics.getDeltaTime() * 45;
        matrix.setToRotation(axis, angle);

        Gdx.gl20.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        Gdx.gl20.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl20.glEnable(GL20.GL_TEXTURE_2D);
        Gdx.gl20.glEnable(GL20.GL_BLEND);
        Gdx.gl20.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        drawBackground();
        drawTerrain();
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
        shaderB.setUniformMatrix("u_mvpMatrix", matrix);
        meshB.render(shaderB, GL20.GL_TRIANGLES);
        shaderB.end();
    }


    public Mesh genTile() {
        Mesh mesh = new Mesh(true, 4*6, 36, new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
                new VertexAttribute(VertexAttributes.Usage.ColorUnpacked, 4, "a_color"),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texcoords"));


        /** Cube vertices */
        short[] indices = {0, 2, 1, 0, 3, 2, 4, 5, 6, 4, 6, 7, 8, 9, 10, 8, 10, 11, 12, 15, 14, 12, 14, 13, 16, 17, 18, 16, 18, 19,
                20, 23, 22, 20, 22, 21};

        float[] cubeVerts = {
                -0.5f, -0.5f, -0.5f,
                -0.5f, -0.5f, 0.5f,
                0.5f, -0.5f, 0.5f,
                0.5f, -0.5f, -0.5f,

                -0.5f, 0.5f, -0.5f,
                -0.5f, 0.5f, 0.5f,
                0.5f, 0.5f, 0.5f,
                0.5f, 0.5f, -0.5f,

                -0.5f, -0.5f, -0.5f,
                -0.5f, 0.5f, -0.5f,
                0.5f, 0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,

                -0.5f, -0.5f, 0.5f,
                -0.5f, 0.5f, 0.5f,
                0.5f, 0.5f, 0.5f,
                0.5f, -0.5f, 0.5f,

                -0.5f, -0.5f, -0.5f,
                -0.5f, -0.5f, 0.5f,
                -0.5f, 0.5f, 0.5f,
                -0.5f, 0.5f, -0.5f,

                0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, 0.5f,
                0.5f, 0.5f, 0.5f,
                0.5f, 0.5f, -0.5f,};

        FloatBuffer buf = FloatBuffer.allocate(1 * 1 * 9 * 4 * 6);
        int i = 0;
        for(int x = 0; x < 1; x++) {
            for(int y = 0; y < 1; y++) {
                Tile t = tiles[x][y];
                float size = 0.5f;
                float[] verts = {
                        // bottom (-y)
                        -size, -size, -size, 1, 1, 1, 1, 0, 1,
                        -size, -size, size, 1, 1, 1, 1, 1, 1,
                        size, -size, size, 1, 1, 1, 1, 1, 0,
                        size, -size, -size, 1, 1, 1, 1, 0, 0,

                        // top (+y)
                        -size, size, -size, 1, 1, 1, 1, 0, 1,
                        -size, size, size, 1, 1, 1, 1, 1, 1,
                        size, size, size, 1, 1, 1, 1, 1, 0,
                        size, size, -size, 1, 1, 1, 1, 0, 0,

                        // back (-z)
                        -size, -size, -size, 1, 1, 1, 1, 0, 1,
                        -size, size, -size, 1, 1, 1, 1, 1, 1,
                        size, size, -size, 1, 1, 1, 1, 1, 0,
                        size, -size, -size, 1, 1, 1, 1, 0, 0,

                        // front (+z)
                        -size, -size, size, 1, 1, 1, 1, 0, 1,
                        -size, size, size, 1, 1, 1, 1, 1, 1,
                        size, size, size, 1, 1, 1, 1, 1, 0,
                        size, -size, size, 1, 1, 1, 1, 0, 0,

                        // left (-x)
                        -size, -size, -size, 1, 1, 1, 1, 0, 1,
                        -size, -size, size, 1, 1, 1, 1, 1, 1,
                        -size, size, size, 1, 1, 1, 1, 1, 0,
                        -size, size, -size, 1, 1, 1, 1, 0, 0,

                        // right (+x)
                        size, -size, -size, 1, 1, 1, 1, 0, 1,
                        size, -size, size, 1, 1, 1, 1, 1, 1,
                        size, size, size, 1, 1, 1, 1, 1, 0,
                        size, size, -size, 1, 1, 1, 1, 0, 0,
                };
                buf.put(verts, i * 9 * 4 * 6, verts.length);
                i++;
            }
        }

        mesh.setVertices(buf.array());
        mesh.setIndices(indices);

        return mesh;
    }
}
