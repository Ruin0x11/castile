package com.ruin.castile;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.Ray;
import com.ruin.castile.chara.AnimationType;
import com.ruin.castile.chara.Direction;
import com.ruin.castile.displayable.CharaDisplayable;
import com.ruin.castile.map.*;

public class Castile extends ApplicationAdapter {
    public static final int MAP_WIDTH = 6;
    public static final int MAP_LENGTH = 6;

    public static final int MAP_SIZE = MAP_WIDTH * MAP_LENGTH;

    public static final int ROTATION_SPEED = 5;

    ShaderProgram backgroundShader, mapShader;

    Mesh backgroundMesh;
    Texture backgroundTexture;
    Camera cam;

    GameMap map;

    DecalBatch decals;
    Decal decal;

    Vector3 yourPosition;
    Vector3 yourPositionPending;
    Vector3 cameraOffset = new Vector3(-20, 20, -20);

    Vector3 movementVector = new Vector3(0, 0, 0);

    int pendingAngle = 0;
    boolean clockwise = false;

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

        float[] c = {0, 0, 0.75f, 1,
                0, 0, 0.75f, 1,
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

        map = new GameMapImpl(9, 9);

        GameMapTessellator tessellator = new GameMapTessellator();
        GameMapMesh mapMesh = tessellator.generateMapMesh(map);
        map.setMesh(mapMesh);

        cam = new PerspectiveCamera(10f, 2f * (4f / 3f), 2f);

        yourPosition = new Vector3(0, 0, 0);
        yourPositionPending = yourPosition.cpy();

        cam.position.set(cameraOffset);
        cam.lookAt(yourPosition);

        decals = new DecalBatch(new CameraGroupStrategy(cam));
        chara = new CharaDisplayable(new Vector3(0,1.5f,0), "kaitou");
        chara.animate(AnimationType.IDLE, Direction.SOUTH, 1);
    }

    @Override
    public void render() {
        handleInput();
        //matrix.setToRotation(axis, angle);

        if(pendingAngle > 0) {

            if(clockwise) {
                orbitPoint(yourPosition, 5, new Vector3(0, 1, 0));
            }
            else {
                orbitPoint(yourPosition, -5, new Vector3(0, 1, 0));
            }
            pendingAngle -= 5;
        }

        if(!movementVector.isZero())
            move(movementVector);
        float curHeight = map.getHeightAtPoint(yourPosition.x+0.5f, yourPosition.z+0.5f);
        yourPosition.y = curHeight*0.1f;

        chara.moveTo(yourPosition.cpy().add(0,.5f,0));

        //System.out.println(curHeight);
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

        drawBackground();
        drawTerrain();
        drawCharas();

        Gdx.graphics.setTitle("Frames / Second : " + Gdx.graphics.getFramesPerSecond() + " | " + yourPosition + " " + yourPositionPending);
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
            angle = 1;
            orbitPoint(yourPosition, angle, new Vector3(0, 1, 0));
        } else if (Gdx.input.isKeyPressed(Input.Keys.J)) {
            angle = -1;
            orbitPoint(yourPosition, angle, new Vector3(0, 1, 0));
        } else {
            angle = 0;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.I)) {
            orbitPointCamera(yourPosition, 1);
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.K)) {
            orbitPointCamera(yourPosition, -1);
        }
        if(pendingAngle == 0) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
                clockwise = false;
                pendingAngle = 45;
            }
            else if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
                clockwise = true;
                pendingAngle = 45;
            }
        }
    }

    public void move(Vector3 vec) {
        //TODO: Needs to be in "world Y axis is up" space
        Vector3 direction = vec.cpy().traMul(cam.view);
        yourPosition.add(direction.x, 0, direction.z);
        Vector3 norm = vec.cpy().nor();
        float angle = MathUtils.atan2(norm.z, norm.x);
        int octant = MathUtils.round( 8 * angle / (2*MathUtils.PI) + 8 + 2 ) % 8;

        chara.animate(AnimationType.IDLE, Direction.values()[octant], 1);
    }

    @Override
    public void dispose() {
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

    void drawCharas() {
        Gdx.gl20.glDisable(GL20.GL_DEPTH_TEST);
        Decal decal = chara.getDecals()[0];
        decal.lookAt(cam.position, cam.up);
        decal.setDimensions(0.5f, 1.0f);
        decals.add(decal);
        decals.flush();
    }

    void doCameraOrbit(float yaw, float pitch) {
        Vector3 camDirection = new Vector3(cam.position).sub(yourPosition).nor();
        Vector3 camUp = cam.up.nor();
        Vector3 camRight = new Vector3(camDirection).crs(camUp);
        Vector3 camRightB = new Vector3(camRight);
        camUp = camRightB.crs(camDirection);
        Matrix4 matUpYaw = matrixFromAxisAngle(camUp.nor(), yaw);
        Matrix4 matRightPitch = matrixFromAxisAngle(camRight.nor(), pitch);

        Vector3 camPosition = new Vector3(cam.position).sub(yourPosition).rot(matUpYaw).rot(matRightPitch).add(yourPosition);
        cam.position.set(camPosition.x, 20, camPosition.z);
        cam.lookAt(yourPosition);
    }

    public Matrix4 matrixFromAxisAngle(Vector3 axis, float angle) {

        Matrix4 mat = new Matrix4();

        float c = MathUtils.cos(angle);
        float s = MathUtils.sin(angle);
        float t = 1.0f - c;
        //  if axis is not already normalised then uncomment this
        // double magnitude = Math.sqrt(a1.x*a1.x + a1.y*a1.y + a1.z*a1.z);
        // if (magnitude==0) throw error;
        // a1.x /= magnitude;
        // a1.y /= magnitude;
        // a1.z /= magnitude;

        mat.val[Matrix4.M00] = c + axis.x * axis.x * t;
        mat.val[Matrix4.M11] = c + axis.y * axis.y * t;
        mat.val[Matrix4.M22] = c + axis.z * axis.z * t;


        float tmp1 = axis.x * axis.y * t;
        float tmp2 = axis.z * s;
        mat.val[Matrix4.M10] = tmp1 + tmp2;
        mat.val[Matrix4.M01] = tmp1 - tmp2;
        tmp1 = axis.x * axis.z * t;
        tmp2 = axis.y * s;
        mat.val[Matrix4.M20] = tmp1 - tmp2;
        mat.val[Matrix4.M02] = tmp1 + tmp2;
        tmp1 = axis.y * axis.z * t;
        tmp2 = axis.x * s;
        mat.val[Matrix4.M21] = tmp1 + tmp2;
        mat.val[Matrix4.M12] = tmp1 - tmp2;

        return mat;
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
