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
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
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
    float rotationSpeed = 0.05f;
    Texture[] textureMaps = new Texture[2];

    GameMap map;

    DecalBatch decals;
    Decal decal;

    Vector3 yourPosition;
    Vector3 cameraOffset = new Vector3(-1, 5, -1);

    float lerp = 0.2f;

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

        cam = new PerspectiveCamera(55f, 2f * (4f/3f), 2f);

        yourPosition = new Vector3(0, 0, 0);
        float angle = 180;
        //cam.rotate(axis, angle);

        cam.position.set(-5,5,0);
        cam.lookAt(2,-2,2);

        decals = new DecalBatch(new CameraGroupStrategy(cam));
        decal = Decal.newDecal(1, 1, new TextureRegion(new Texture("kaitou.png")), true);
        decal.setPosition(1,2,1);
        decals.add(decal);
    }

    Vector3 axis = new Vector3(0, 1, 0);

    @Override
    public void render () {
        handleInput();
        //matrix.setToRotation(axis, angle);
        float curHeight = map.getHeightAtPoint(yourPosition.x, yourPosition.z);
        //System.out.println(curHeight);
        Vector3 camPosition = new Vector3(yourPosition.x + cameraOffset.x, yourPosition.y + cameraOffset.y + curHeight*.1f, yourPosition.z + cameraOffset.z);
        cam.position.x += (camPosition.x - cam.position.x) * lerp;
        cam.position.y += (camPosition.y - cam.position.y) * lerp;
        cam.position.z += (camPosition.z - cam.position.z) * lerp;
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

        decal.lookAt(cam.position, cam.up);
        decals.add(decal);
        decals.flush();

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
            cam.rotateAround(yourPosition, axis, -rotationSpeed);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            cam.rotateAround(yourPosition, axis, rotationSpeed);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.F)) {
            doCameraOrbit();
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

    void doCameraOrbit() {
        Vector3 camDirection = cam.direction; //new Vector3(cam.position).sub(yourPosition).nor();
        Vector3 camUp = new Vector3(cam.up);
        Vector3 camRight = new Vector3(camDirection).crs(camUp);
        Vector3 camRightB = new Vector3(camRight);
        camUp = camRightB.crs(camDirection);
        Matrix4 matUpYaw = matrixFromAxisAngle(camUp.nor(), 0.05f);
        Matrix4 matRightPitch = matrixFromAxisAngle(camRight.nor(), 0.00f);

        Vector3 finalCam = new Vector3(cam.position).sub(yourPosition).rot(matUpYaw).rot(matRightPitch).add(yourPosition);
        cam.position.set(finalCam);

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

        mat.val[Matrix4.M00] = c + axis.x*axis.x*t;
        mat.val[Matrix4.M11] = c + axis.y*axis.y*t;
        mat.val[Matrix4.M22] = c + axis.z*axis.z*t;


        float tmp1 = axis.x*axis.y*t;
        float tmp2 = axis.z*s;
        mat.val[Matrix4.M10] = tmp1 + tmp2;
        mat.val[Matrix4.M01] = tmp1 - tmp2;
        tmp1 = axis.x*axis.z*t;
        tmp2 = axis.y*s;
        mat.val[Matrix4.M20] = tmp1 - tmp2;
        mat.val[Matrix4.M02] = tmp1 + tmp2;    tmp1 = axis.y*axis.z*t;
        tmp2 = axis.x*s;
        mat.val[Matrix4.M21] = tmp1 + tmp2;
        mat.val[Matrix4.M12] = tmp1 - tmp2;

        return mat;
    }

//    /** * Rotates the camera around a point in 3D by 1 degree each call.
//     * @author radioking from Badlogic forum (libGDX)
//     * @param origin The vector to rotate.
//     * @param clockwise defines rotation direction
//     */
//    public void orbitPoint(Vector3 origin, boolean clockwise) {
//        // (1) get intersection point for
//        // camera viewing direction and xz-plane
//        cam.lookAt(origin.x, origin.y, origin.z);
//        cam.update();
//        cam.apply(gl);
//        camViewRay.set(cam.position, cam.direction);
//        Intersector.intersectRayPlane(camViewRay, xzPlane, lookAtPoint);
//
//        // (2) calculate radius between
//        // camera position projected on xz-plane
//        // and the intersection point from (1)
//        orbitRadius = lookAtPoint.dst(cameraPosition.set(cam.position));
//
//        // (3) move camera to intersection point from (1)
//        cam.position.set(lookAtPoint);
//
//        // (4) rotate camera by 1° around y-axis
//        // according to winding clockwise/counter-clockwise
//        if(clockwise){ cam.rotate(-1, 0, 1, 0); } else { cam.rotate(1, 0, 1, 0); }
//
//        // (5) move camera back by radius
//        orbitReturnVector.set(cam.direction.tmp().mul(-orbitRadius));
//        cam.translate(orbitReturnVector.x, orbitReturnVector.y, orbitReturnVector.z);
//    }
}
