package com.ruin.castile;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.ruin.castile.map.*;

public class Castile extends ApplicationAdapter {
    public static final int MAP_WIDTH = 6;
    public static final int MAP_LENGTH = 6;

    public static final int MAP_SIZE = MAP_WIDTH * MAP_LENGTH;

    ShaderProgram backgroundShader, mapShader;

    Mesh backgroundMesh;
    Texture backgroundTexture;
    Camera cam;
    float rotationSpeed = 1f;
    Texture[] textureMaps = new Texture[2];

    GameMap map;

    DecalBatch decals;

    Vector3 yourPosition;
    Vector3 cameraOffset = new Vector3(-50, 50, -50);

    float lerp = 0.1f;

    @Override
    public void create () {

        FileHandle file = Gdx.files.internal("shaders/bg_vert.glsl");
        String backgroundVertexShader = file.readString();
        file = Gdx.files.internal("shaders/bg_frag.glsl");
        String backgroundFragmentShader = file.readString();

        backgroundShader = new ShaderProgram(backgroundVertexShader, backgroundFragmentShader);
        if (backgroundShader.isCompiled() == false) {
            Gdx.app.log("ShaderTest", backgroundShader.getLog());
            Gdx.app.exit();
        }

        float[] c =        {0,0,0.75f,1,
                            0,0,0.75f,1,
                            0,0,0,1,
                            0,0,0,1};

        backgroundMesh = new Mesh(true, 4, 6, VertexAttribute.Position(), VertexAttribute.ColorUnpacked(), VertexAttribute.TexCoords(0));
        backgroundMesh.setVertices(new float[]   {-1.0f, -1.0f, 0, c[0], c[1], c[2], c[3], 0, 1,
                                        1.0f, -1.0f, 0,  c[4], c[5], c[6], c[7], 1, 1,
                                        1.0f, 1.0f, 0,   c[8], c[9], c[10], c[11], 1, 0,
                                        -1.0f, 1.0f, 0,  c[12], c[13], c[14], c[15], 0, 0});
        backgroundMesh.setIndices(new short[] {0, 1, 2, 2, 3, 0});
        backgroundTexture = new Texture("default.png");

        file = Gdx.files.internal("shaders/map_vert.glsl");
        String mapVertexShader = file.readString();
        file = Gdx.files.internal("shaders/map_frag.glsl");
        String mapFragmentShader = file.readString();

        mapShader = new ShaderProgram(mapVertexShader, mapFragmentShader);
        if (mapShader.isCompiled() == false) {
            Gdx.app.log("ShaderTest", mapShader.getLog());
            Gdx.app.exit();
        }

        map = new GameMapImpl(9, 9);

        GameMapTessellator tessellator = new GameMapTessellator();
        GameMapMesh mapMesh = tessellator.generateMapMesh(map);
        map.setMesh(mapMesh);

        cam = new PerspectiveCamera(5f, 2f * (4f/3f), 2f);

        Vector3 axis = new Vector3(0, 1, 0);
        yourPosition = new Vector3(0, 0, 0);
        float angle = 180;
        //cam.rotate(axis, angle);

        cam.lookAt(2,-2,2);

        decals = new DecalBatch(new CameraGroupStrategy(cam));
    }

    @Override
    public void render () {
        handleInput();
        //matrix.setToRotation(axis, angle);
        float curHeight = map.getHeightAtPoint(yourPosition.x, yourPosition.z);
        //System.out.println(curHeight);
        Vector3 camPosition = new Vector3(yourPosition.x + cameraOffset.x, yourPosition.y + cameraOffset.y + curHeight*.1f, yourPosition.z + cameraOffset.z);
        cam.position.x = camPosition.x;
        cam.position.y += (camPosition.y - cam.position.y) * lerp;
        cam.position.z = camPosition.z;
        cam.update();

        Gdx.gl20.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        Gdx.gl20.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl20.glClearDepthf(cam.far*10);

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
        backgroundMesh.dispose();
        backgroundShader.dispose();
        backgroundShader.dispose();
    }

    void drawBackground() {
        //stop 3D objects from clipping the background
        Gdx.gl20.glDisable(GL20.GL_DEPTH_TEST);

        backgroundTexture.bind();
        backgroundShader.begin();
        backgroundShader.setUniformi("u_texture", 0);
        backgroundMesh.render(backgroundShader, GL20.GL_TRIANGLES);
        backgroundShader.end();
    }

    void drawTerrain() {
        Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE0);
        mapShader.begin();
        mapShader.setUniformi("u_texture", 0);
        mapShader.setUniformMatrix("u_mvpMatrix", cam.combined);
        map.getMesh().render(mapShader, GL20.GL_TRIANGLES);
        mapShader.end();
    }
}
