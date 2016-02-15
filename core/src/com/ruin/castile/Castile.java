package com.ruin.castile;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TShortList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TShortArrayList;

public class Castile extends ApplicationAdapter {
    public static final int MAP_WIDTH = 6;
    public static final int MAP_LENGTH = 6;

    public static final int MAP_SIZE = MAP_WIDTH * MAP_LENGTH;

    ShaderProgram shader, shaderB;
    Mesh mesh, meshB;
    Texture texture, textureB;
    Matrix4 matrix = new Matrix4();
    Tile[][] tiles = new Tile[MAP_WIDTH][MAP_LENGTH];
    Camera cam;
    float rotationSpeed = 1f;
    Texture[] textureMaps = new Texture[2];

    DecalBatch decals;

    Vector3 yourPosition;

    @Override
    public void create () {
        String vertexShader =
                "attribute vec4 a_position;    \n"
                + "attribute vec4 a_color;\n"
                + "attribute vec2 a_texCoord0;\n"
                + "varying vec4 v_color;"
                + "varying vec2 v_texCoords;\n"
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

        for(int i = 0; i < MAP_WIDTH; i++) {
            for(int j = 0; j < MAP_LENGTH; j++) {
                tiles[i][j] = new Tile();
                Tile.ScreenData dat = new Tile.ScreenData(new Vector2i(24 * (MAP_WIDTH-i), 24 * (MAP_LENGTH-j)), new Vector2i(24, 24));
                tiles[i][j].screenData.put(Tile.Screen.UPPER, dat);
                tiles[i][j].addHeight(i*j*5);
            }
        }

        textureMaps[0] = new Texture("ground.png");
        textureMaps[1] = new Texture("wall.png");

        shaderB = new ShaderProgram(vertexShaderB, fragmentShaderB);
        meshB = genTile();
        meshB.getVertexAttribute(VertexAttributes.Usage.Position).alias = "a_position";
        textureB = new Texture("groundB.png");

        cam = new PerspectiveCamera(19f, 2f * (4f/3f), 2f);

        Vector3 axis = new Vector3(0, 1, 0);
        yourPosition = new Vector3(-13, 15, -13);
        float angle = 180;
        //cam.rotate(axis, angle);

        cam.lookAt(2,-2,2);


    }

    @Override
    public void render () {
        handleInput();
        //matrix.setToRotation(axis, angle);
        cam.position.set(yourPosition);
        cam.update();

        Gdx.gl20.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        Gdx.gl20.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl20.glClearDepthf(cam.far);

        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl20.glEnable(GL20.GL_TEXTURE_2D);
        Gdx.gl20.glEnable(GL20.GL_BLEND);
        Gdx.gl20.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        drawBackground();

        drawTerrain();

        Gdx.graphics.setTitle("Frames / Second : " + Gdx.graphics.getFramesPerSecond() + " | " + yourPosition + " " + cam.direction);
    }

    private void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            yourPosition.add(0, 0, -0.05f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            yourPosition.add(0, 0, 0.05f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            yourPosition.add(-0.05f, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            yourPosition.add(0.05f, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Z)) {
            yourPosition.add(0, -0.05f, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            yourPosition.add(0, 0.05f, 0);
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
        meshB.dispose();
        texture.dispose();
        shader.dispose();
    }

    void drawBackground() {
        //stop 3D objects from clipping the background
        Gdx.gl20.glDisable(GL20.GL_DEPTH_TEST);

        texture.bind();
        shader.begin();
        shader.setUniformi("u_texture", 0);
        mesh.render(shader, GL20.GL_TRIANGLES);
        shader.end();
    }

    void drawTerrain() {
        Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE0);
        textureMaps[0].bind();
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


        //    F ----- G
        //   /|      /|
        //  E ------H |   ^ Y  Z
        //  | |     | |   |   /
        //  | |B ---|-|C  |  /
        //  |/      |/    | /
        //  A ------D     |------>X

        TFloatList vertList = new TFloatArrayList(9 * 24 * MAP_SIZE);
        TShortList indList = new TShortArrayList(36 * MAP_SIZE);
        int i = 0;
        for(int x = 0; x < MAP_WIDTH; x++) {
            for(int y = 0; y < MAP_LENGTH; y++) {
                Tile t = tiles[x][y];
                float size = 0.5f;

                Vector3 pta = new Vector3( x - size,  0,  y - size );
                Vector3 ptb = new Vector3( x - size,  0,  y + size );
                Vector3 ptc = new Vector3( x + size,  0,  y + size );
                Vector3 ptd = new Vector3( x + size,  0,  y - size );
                Vector3 pte = new Vector3( x - size,  0.1f*t.heightData.get(Tile.Corner.UPPER_SOUTH_WEST),  y - size );
                Vector3 ptf = new Vector3( x - size,  0.1f*t.heightData.get(Tile.Corner.UPPER_SOUTH_EAST),  y + size );
                Vector3 ptg = new Vector3( x + size,  0.1f*t.heightData.get(Tile.Corner.UPPER_NORTH_EAST),  y + size );
                Vector3 pth = new Vector3( x + size,  0.1f*t.heightData.get(Tile.Corner.UPPER_NORTH_WEST),  y - size );

                Rect2f[] texReg = new Rect2f[5];
                texReg[0] = getTextureBounds(t.screenData.get(Tile.Screen.UPPER));
                texReg[1] = getTextureBounds(t.screenData.get(Tile.Screen.SOUTH));
                texReg[2] = getTextureBounds(t.screenData.get(Tile.Screen.NORTH));
                texReg[3] = getTextureBounds(t.screenData.get(Tile.Screen.WEST));
                texReg[4] = getTextureBounds(t.screenData.get(Tile.Screen.EAST));

                float[] verts = {
                        // bottom (-y)
                        pta.x, pta.y, pta.z, 1, 1, 1, 1, 0, 1,
                        ptb.x, ptb.y, ptb.z, 1, 1, 1, 1, 1, 1,
                        ptc.x, ptc.y, ptc.z, 1, 1, 1, 1, 1, 0,
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
                for(int j = 0; j < indices.length; j++) {
                    curIndices[j] = (short) (indices[j] + (24*i));
                }
                indList.add(curIndices);
                i++;
            }
        }
        mesh.setVertices(vertList.toArray());
        mesh.setIndices(indList.toArray());

        return mesh;
    }

    Rect2f getTextureBounds(Tile.ScreenData data) {
        float minX = (float) data.texCoords.x / (float) textureMaps[0].getWidth();
        float minY = (float) data.texCoords.y / (float) textureMaps[0].getHeight();
        float width = 1.0f / (textureMaps[0].getWidth()/data.texRegion.x);
        float height = 1.0f / (textureMaps[0].getHeight()/data.texRegion.y);
        return Rect2f.createFromMinAndSize(minX, minY, width, height);
    }
}
