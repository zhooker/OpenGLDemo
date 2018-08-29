package com.example.opengldemo.blend;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.opengldemo.R;
import com.example.opengldemo.util.ProgramUtil;
import com.example.opengldemo.util.TextureHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * This class implements our custom renderer. Note that the GL10 parameter passed in is unused for OpenGL ES 2.0
 * renderers -- the static class GLES30 is used instead.
 */
public class BlendRenderer implements GLSurfaceView.Renderer {

    protected final int mBytesPerFloat = 4;
    protected final int mPositionDataSize = 3;

    private Context mContext;

    protected int textureId;
    protected int textureGrassId;
    protected int mTextureHandle;
    protected int mTextureCoordindate;
    protected int mMVPMatrixHandle;
    protected int mPositionHandle;
    protected int mPerVertexProgramHandle;

    protected final FloatBuffer mCubePositions;
    private final FloatBuffer mCubesTexture;

    protected float[] mModelMatrix = new float[16];
    protected float[] mViewMatrix = new float[16];
    protected float[] mProjectionMatrix = new float[16];
    protected float[] mMVPMatrix = new float[16];

    public BlendRenderer(Context context) {
        mCubePositions = ByteBuffer.allocateDirect(cubePositionData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubePositions.put(cubePositionData).position(0);

        mCubesTexture = ByteBuffer.allocateDirect(cubeTextureData.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubesTexture.put(cubeTextureData).position(0);

        this.mContext = context;
    }

    protected String getVertexShader() {
        // Define our per-pixel lighting shader.
        final String perPixelVertexShader =
                "#version 300 es                \n"
                        + "uniform mat4 u_MVPMatrix;      \n"
                        + "layout (location = 0) in vec4 a_Position;            \n"
                        + "layout (location = 1) in vec2 a_TextureCoordinates;  \n"
                        + "out vec2 v_TextureCoordinates; \n"
                        + "void main()                                                \n"
                        + "{                                                          \n"
                        + "   gl_Position = u_MVPMatrix * a_Position;                 \n"
                        + "   v_TextureCoordinates = a_TextureCoordinates;            \n"
                        + "}                                                          \n";

        return perPixelVertexShader;
    }

    protected String getFragmentShader() {
        final String perPixelFragmentShader =
                "#version 300 es                \n"
                        + "precision mediump float;       \n"
                        + "uniform sampler2D u_TextureUnit;         \n"
                        + "in vec2 v_TextureCoordinates;            \n"
                        + "out vec4 FragColor;            \n"
                        + "void main()                    \n"
                        + "{                              \n"
                        + "   FragColor = texture(u_TextureUnit, v_TextureCoordinates); \n"
                        + "}                                                            \n";

        return perPixelFragmentShader;
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {

        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES30.glEnable(GLES30.GL_CULL_FACE);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);

        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = -1.5f;

        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;

        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);


        mPerVertexProgramHandle = ProgramUtil.createAndLinkProgram(getVertexShader(), getFragmentShader(),
                new String[]{"a_Position", "a_Color", "a_Normal"});

        mMVPMatrixHandle = GLES30.glGetUniformLocation(mPerVertexProgramHandle, "u_MVPMatrix");
        mPositionHandle = GLES30.glGetAttribLocation(mPerVertexProgramHandle, "a_Position");
        mTextureCoordindate = GLES30.glGetAttribLocation(mPerVertexProgramHandle, "a_TextureCoordinates");
        mTextureHandle = GLES30.glGetUniformLocation(mPerVertexProgramHandle, "u_TextureUnit");

        textureId = TextureHelper.loadTexture(mContext, R.drawable.blending_transparent_window);
        textureGrassId = TextureHelper.loadTexture(mContext, R.drawable.grass);
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
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
    public void onDrawFrame(GL10 glUnused) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        GLES30.glEnable(GLES30.GL_BLEND);
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);

        GLES30.glUseProgram(mPerVertexProgramHandle);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -4.1f);
        drawCube(false);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, -0.0f, -0.5f, -4.0f);
        drawCube(true);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, -1.0f, 0.0f, -3.5f);
        drawCube(false);
    }


    protected void drawCube(boolean grass) {
        mCubesTexture.position(0);
        GLES30.glVertexAttribPointer(mTextureCoordindate, 2, GLES30.GL_FLOAT, false,
                0, mCubesTexture);
        GLES30.glEnableVertexAttribArray(mTextureCoordindate);

        mCubePositions.position(0);
        GLES30.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES30.GL_FLOAT, false,
                0, mCubePositions);

        GLES30.glEnableVertexAttribArray(mPositionHandle);

        if (grass) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureGrassId);
            GLES30.glUniform1i(mTextureHandle, 1);
        } else {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);
            GLES30.glUniform1i(mTextureHandle, 0);
        }


        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Draw the cube.
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6);
    }

    public static final float[] cubePositionData =
            {
                    -1.0f, 1.0f, 0.0f,
                    -1.0f, -1.0f, 0.0f,
                    1.0f, 1.0f, 0.0f,
                    -1.0f, -1.0f, 0.0f,
                    1.0f, -1.0f, 0.0f,
                    1.0f, 1.0f, 0.0f
            };

    public static final float[] cubeTextureData =
            {
                    0, 0,
                    0, 1.0f,
                    1.0f, 0,
                    0, 1.0f,
                    1.0f, 1.0f,
                    1.0f, 0
            };
}
