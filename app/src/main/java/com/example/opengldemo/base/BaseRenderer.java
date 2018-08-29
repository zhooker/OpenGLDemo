package com.example.opengldemo.base;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.example.opengldemo.util.CubeUtil;
import com.example.opengldemo.util.ProgramUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author zhuangsj
 * @created 2018/8/29
 */
public class BaseRenderer implements GLSurfaceView.Renderer {

    protected Context mContext;

    protected final int mBytesPerFloat = 4;
    protected final int mPositionDataSize = 3;
    protected final int mColorDataSize = 4;

    protected float[] mModelMatrix = new float[16];
    protected float[] mViewMatrix = new float[16];
    protected float[] mProjectionMatrix = new float[16];
    protected float[] mMVPMatrix = new float[16];

    protected int mPerVertexProgramHandle;
    protected int mMVPMatrixHandle;
    protected int mPositionHandle;
    protected int mColorHandle;

    protected final FloatBuffer mCubePositions;
    protected final FloatBuffer mCubeColors;

    public BaseRenderer(Context context) {
        this.mContext = context;

        final float[] positions = getCubePosition();
        mCubePositions = ByteBuffer.allocateDirect(positions.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubePositions.put(positions).position(0);

        final float[] colors = getCubeColor();
        mCubeColors = ByteBuffer.allocateDirect(colors.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeColors.put(colors).position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Set the background clear color to black.
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // Use culling to remove back faces.
        GLES30.glEnable(GLES30.GL_CULL_FACE);

        // Enable depth testing
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);

        // Position the eye in front of the origin.
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = -1.5f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        initPrograms();
        initHandlers();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // Set the OpenGL viewport to the same size as the surface.
        GLES30.glViewport(0, 0, width, height);

        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        // Do a complete rotation every 10 seconds.
        long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);

        onDrawCube(angleInDegrees);
    }


    protected void initPrograms() {
        mPerVertexProgramHandle = ProgramUtil.createAndLinkProgram(mContext, getVertexShader(), getFragmentShader(), new String[]{"a_Position", "a_Color", "a_Normal"});
    }

    protected void initHandlers() {
        mMVPMatrixHandle = GLES30.glGetUniformLocation(mPerVertexProgramHandle, "u_MVPMatrix");
        mPositionHandle = GLES30.glGetAttribLocation(mPerVertexProgramHandle, "a_Position");
        mColorHandle = GLES30.glGetAttribLocation(mPerVertexProgramHandle, "a_Color");
    }

    protected String getVertexShader() {
        return "base/vertex_base.glsl";
    }

    protected String getFragmentShader() {
        return "base/fragment_base.glsl";
    }

    protected float[] getCubePosition() {
        return new float[]{
                0.0f, 0.0f, 1.0f,
                -1.0f, -1.0f, 1.0f,
                1.0f, -1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
                -1.0f, 1.0f, 1.0f,
                -1.0f, -1.0f, 1.0f,
        };
    }

    protected float[] getCubeColor() {
        return CubeUtil.cubeColorData;
    }

    protected void onDrawCube(float degree) {
        GLES30.glUseProgram(mPerVertexProgramHandle);

        mCubeColors.position(0);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -6.0f);
        drawCube();
    }

    protected void drawCube() {
        // Pass in the position information
        mCubePositions.position(0);
        GLES30.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES30.GL_FLOAT, false,
                0, mCubePositions);
        GLES30.glEnableVertexAttribArray(mPositionHandle);

        // Pass in the color information
        GLES30.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES30.GL_FLOAT, false,
                0, mCubeColors);
        GLES30.glEnableVertexAttribArray(mColorHandle);

        // Calculate MVP matrix
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Draw the cube.
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, mCubePositions.capacity() / mPositionDataSize);
    }
}
