package com.ruin.castile;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ruin.castile.chara.AnimationType;
import com.ruin.castile.chara.Direction;
import com.ruin.castile.displayable.CharaDisplayable;
import com.ruin.castile.map.*;
import com.ruin.castile.util.MPDLoader;

public class Castile extends ApplicationAdapter {
    public static final int MAP_WIDTH = 6;
    public static final int MAP_LENGTH = 6;

    public static final int MAP_SIZE = MAP_WIDTH * MAP_LENGTH;

    public static final int ROTATION_SPEED = 5;

    ShaderProgram backgroundShader, mapShader;

    Mesh backgroundMesh;
    Texture backgroundTexture;
    Camera cam;
    Viewport viewport;

    GameMap map;

    DecalBatch decals;
    Decal decal;

    Vector3 yourPosition;
    Vector3 yourPositionPending;
    Vector3 cameraOffset = new Vector3(-20, 20, -20);

    Vector3 movementVector = new Vector3(0, 0, 0);

    int pendingAngle = 0;
    boolean clockwise = false;
    boolean moving = false;
    int charaDir = -1;

    float lerp = 0.2f;

    CharaDisplayable chara;

    @Override
    public void create() {

        FileHandle file = Gdx.files.internal("shaders/bg_vert.glsl");
        String backgroundVertexShader = file.readString();
        file = Gdx.files.internal("shaders/bg_frag.glsl");
        String backgroundFragmentShader = file.readString();

        backgroundShader = new ShaderProgram(backgroundVertexShader, backgroundFragmentShader);
        if (backgroundShader.isCompiled() == false) {
            Gdx.app.log("ShaderTest", backgroundShader.getLog());
            Gdx.app.exit();
        }

        float[] c = {0.25f, 0.25f, 0.50f, 1,
                0.25f, 0.25f, 0.50f, 1,
                0, 0, 0, 1,
                0, 0, 0, 1};

        backgroundMesh = new Mesh(true, 4, 6, VertexAttribute.Position(), VertexAttribute.ColorUnpacked(), VertexAttribute.TexCoords(0));
        backgroundMesh.setVertices(new float[]{-1.0f, -1.0f, 0, c[0], c[1], c[2], c[3], 0, 1,
                1.0f, -1.0f, 0, c[4], c[5], c[6], c[7], 1, 1,
                1.0f, 1.0f, 0, c[8], c[9], c[10], c[11], 1, 0,
                -1.0f, 1.0f, 0, c[12], c[13], c[14], c[15], 0, 0});
        backgroundMesh.setIndices(new short[]{0, 1, 2, 2, 3, 0});
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

        //map = new GameMapImpl(9, 9);
        map = MPDLoader.load("mp00101.mpd");

        GameMapTessellator tessellator = new GameMapTessellator();
        GameMapMesh mapMesh = tessellator.generateMapMesh(map);
        map.setMesh(mapMesh);

        cam = new PerspectiveCamera(10f, 2f * (4f / 3f), 2f);
        viewport = new FillViewport(1024, 768, cam);

        yourPosition = new Vector3(0, 0, 0);
        yourPositionPending = yourPosition.cpy();

        cam.position.set(cameraOffset);
        cam.lookAt(yourPosition);

        decals = new DecalBatch(new CameraGroupStrategy(cam));
        chara = new CharaDisplayable(new Vector3(0,1.5f,0), "castile");
        chara.animate(AnimationType.IDLE, Direction.SOUTH, 1);
    }

    @Override
    public void render() {
        handleInput();
        //matrix.setToRotation(axis, angle);

        if(pendingAngle > 0) {

            if(clockwise) {
                orbitPoint(yourPosition, ROTATION_SPEED, new Vector3(0, 1, 0));
            }
            else {
                orbitPoint(yourPosition, -ROTATION_SPEED, new Vector3(0, 1, 0));
            }
            pendingAngle -= ROTATION_SPEED;
        }

        if(!movementVector.isZero())
            move(movementVector);
        else
            moving = false;

        float curHeight = map.getHeightAtPoint(yourPosition.x+0.5f, yourPosition.z+0.5f);
        yourPosition.y = curHeight*0.1f;

        chara.moveTo(yourPosition.cpy().add(0,.5f,0));

        Vector3 diff = yourPosition.cpy().sub(yourPositionPending);
        yourPositionPending.x += diff.x * lerp;
        yourPositionPending.y += diff.y * lerp;
        yourPositionPending.z += diff.z * lerp;

        Vector3 lastPosition = yourPositionPending.cpy().add(cameraOffset);
        cam.position.set(lastPosition);
        cam.update();

        movementVector.set(0, 0, 0);

        Gdx.gl20.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        Gdx.gl20.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl20.glClearDepthf(cam.far * 10);

        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl20.glEnable(GL20.GL_TEXTURE_2D);
        Gdx.gl20.glEnable(GL20.GL_BLEND);
        Gdx.gl20.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        viewport.apply();

        drawBackground();
        drawTerrain();
        drawCharas();

        Gdx.graphics.setTitle("Frames / Second : " + Gdx.graphics.getFramesPerSecond() + " | " + yourPosition + " " + yourPositionPending);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    float angle = 0;

    private void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            movementVector.add(-0.05f, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            movementVector.add(0.05f, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            movementVector.add(0, 0, 0.05f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            movementVector.add(0, 0, -0.05f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.L)) {
            orbitPoint(yourPosition, 1, new Vector3(0, 1, 0));
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.J)) {
            orbitPoint(yourPosition, -1, new Vector3(0, 1, 0));
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.I)) {
            orbitPointCamera(yourPosition, 1);
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.K)) {
            orbitPointCamera(yourPosition, -1);
        }
        else if (Gdx.input.isKeyJustPressed(Input.Keys.A) && pendingAngle == 0) {
            clockwise = false;
            pendingAngle = 45;
        }
        else if (Gdx.input.isKeyJustPressed(Input.Keys.D) && pendingAngle == 0) {
            clockwise = true;
            pendingAngle = 45;
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            Vector3 orbitReturnVector = new Vector3(cam.direction).scl(-0.5f);
            cameraOffset.add(orbitReturnVector);
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            Vector3 orbitReturnVector = new Vector3(cam.direction).scl(0.5f);
            cameraOffset.add(orbitReturnVector);
        }
    }

    public void move(Vector3 vec) {
        //modified "Camera.update()" with world Y axis as up vector instead of camera's
        Vector3 tmp = new Vector3();
        Matrix4 view = new Matrix4();
        Vector3 dir = new Vector3(cam.direction.x, 0, cam.direction.z);
        view.setToLookAt(cam.position, tmp.set(cam.position).add(dir), new Vector3(0, 1, 0));

        Vector3 direction = vec.cpy().traMul(view);
        yourPosition.add(direction.x, 0, direction.z);
        Vector3 norm = vec.cpy().nor();
        float angle = MathUtils.atan2(norm.z, norm.x);
        int octant = MathUtils.round( 8 * angle / (2*MathUtils.PI) + 8 + 2 ) % 8;

        if(!moving || charaDir != octant) {
            chara.animate(AnimationType.IDLE, Direction.values()[octant], 1);
            moving = true;
            charaDir = octant;
        }
    }

    @Override
    public void dispose() {
        backgroundMesh.dispose();
        backgroundShader.dispose();
        backgroundShader.dispose();
        map.getMesh().dispose();
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

    void drawCharas() {
        Gdx.gl20.glDisable(GL20.GL_DEPTH_TEST);
        Decal decal = chara.getDecals()[0];
        decal.lookAt(cam.position, cam.up);
        decal.setDimensions(0.5f, 1.0f);
        decals.add(decal);
        decals.flush();
    }

    final Plane xzPlane = new Plane(new Vector3(0, 1, 0), 0);

    /**
     * Rotates the camera around a point in 3D by 1 degree each call.
     *
     * @param origin The vector to rotate.
     * @param angle  defines rotation direction
     * @author radioking from Badlogic forum (libGDX)
     */
    public void orbitPoint(Vector3 origin, float angle, Vector3 axis) {
        // (1) get intersection point for
        // camera viewing direction and xz-plane
        cam.lookAt(origin.x, origin.y, origin.z);
        cam.update();

        Vector3 intersectionPoint = new Vector3();
        Ray camViewRay = new Ray(cameraOffset, cam.direction);
        Intersector.intersectRayPlane(camViewRay, xzPlane, intersectionPoint);

        // (2) calculate radius between
        // camera position projected on xz-plane
        // and the intersection point from (1)
        float orbitRadius = intersectionPoint.dst(new Vector3(cameraOffset));

        // (3) move camera to intersection point from (1)
        cameraOffset.set(intersectionPoint);

        // (4) rotate camera by 1° around y-axis
        // according to winding clockwise/counter-clockwise
        cam.rotate(axis, angle);

        // (5) move camera back by radius
        Vector3 orbitReturnVector = new Vector3(cam.direction).scl(-orbitRadius);
        cameraOffset.add(orbitReturnVector);
    }

    public void orbitPointCamera(Vector3 origin, float angle) {
        // (1) get intersection point for
        // camera viewing direction and xz-plane
        cam.lookAt(origin.x, origin.y, origin.z);
        cam.update();

        Vector3 intersectionPoint = new Vector3();
        Ray camViewRay = new Ray(cameraOffset, cam.direction);
        Intersector.intersectRayPlane(camViewRay, xzPlane, intersectionPoint);

        // (2) calculate radius between
        // camera position projected on xz-plane
        // and the intersection point from (1)
        float orbitRadius = intersectionPoint.dst(new Vector3(cameraOffset));

        // (3) move camera to intersection point from (1)
        cameraOffset.set(intersectionPoint);

        Vector3 camDirection = new Vector3(cam.position).sub(origin).nor();
        Vector3 camUp = cam.up.nor();
        Vector3 camRight = new Vector3(camDirection).crs(camUp);

        // (4) rotate camera by 1° around y-axis
        // according to winding clockwise/counter-clockwise
        cam.rotate(camRight, angle);

        // (5) move camera back by radius
        Vector3 orbitReturnVector = new Vector3(cam.direction).scl(-orbitRadius);
        cameraOffset.add(orbitReturnVector);
    }
}
