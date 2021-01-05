package com.example.opengldemo.decoder.gl2;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import com.example.opengldemo.util.ShaderUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Nv21Renderer
 * 利用双纹理方式渲染预览画面
 */
public class Nv21Renderer implements GLSurfaceView.Renderer {


    protected int previewWidth = 1920;
    protected int previewHeight = 1080;
    protected ByteBuffer frameRenderBuffer = null;
    protected ReentrantLock frameLock = new ReentrantLock();

    protected int bwSize;
    protected boolean isReady = false;
    protected byte[] nv21Data;

    protected int mProgramHandle;
    protected int mPositionHandle;
    protected int aTextureCoordLocation;
    protected int cameraTextureYID;
    protected int cameraTextureUVID;
    protected int uTextureSamplerYLocation;
    protected int uTextureSamplerUVLocation;

    protected final int mBytesPerFloat = 4;
    protected final FloatBuffer mCubePositions;

    public Nv21Renderer() {
        float[] cubePositionData = getCubePositionData();
        // Initialize the buffers.
        mCubePositions = ByteBuffer.allocateDirect(cubePositionData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubePositions.put(cubePositionData).position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        int[] textureNames = new int[2];

        GLES20.glGenTextures(1, textureNames, 0);
        cameraTextureYID = textureNames[0];

        GLES20.glGenTextures(1, textureNames, 1);
        cameraTextureUVID = textureNames[1];

        mProgramHandle = ShaderUtils.createProgram(getVertexFile(), getFragmentFile());

        // Set program handles for cube drawing.
        mPositionHandle = GLES30.glGetAttribLocation(mProgramHandle, "a_Position");
        aTextureCoordLocation = GLES30.glGetAttribLocation(mProgramHandle, "aTextureCoordinate");
        uTextureSamplerYLocation = GLES20.glGetUniformLocation(mProgramHandle, "uTextureSamplerY");
        uTextureSamplerUVLocation = GLES20.glGetUniformLocation(mProgramHandle, "uTextureSamplerUV");
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        if (!isReady) {
            return;
        }

        if (!textureInit) {
            initializeTexture();
            textureInit = true;
        }

        updateRenderBuffer();

        updateRenderTexture();


        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        // Set our per-vertex lighting program.
        GLES30.glUseProgram(mProgramHandle);

        // bind texture
        onBindTexture();

        // Pass in the position information
        mCubePositions.position(0);
        GLES30.glVertexAttribPointer(mPositionHandle, 3, GLES30.GL_FLOAT, false,
                5 * mBytesPerFloat, mCubePositions);
        GLES30.glEnableVertexAttribArray(mPositionHandle);

        mCubePositions.position(3);
        GLES30.glVertexAttribPointer(aTextureCoordLocation, 2, GLES30.GL_FLOAT, false,
                5 * mBytesPerFloat, mCubePositions);
        GLES30.glEnableVertexAttribArray(aTextureCoordLocation);

        // Draw the cube.
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6);
    }

    private boolean textureInit = false;

    private void initializeTexture() {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, cameraTextureYID);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        frameRenderBuffer.position(0);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, previewWidth, previewHeight, 0,
                GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, frameRenderBuffer);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, cameraTextureUVID);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        frameRenderBuffer.position(4 * (previewWidth / 2) * (previewHeight / 2));
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE_ALPHA, previewWidth / 2,
                previewHeight / 2, 0, GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE,
                frameRenderBuffer);
    }

    public void updateRenderBuffer() {
        frameLock.lock();
        try {
            frameRenderBuffer.position(0);
            if (nv21Data.length <= bwSize * 6) {
                frameRenderBuffer.put(nv21Data);
            }
            frameRenderBuffer.position(0);
        } finally {
            frameLock.unlock();
        }
    }

    public void updateRenderTexture() {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, cameraTextureYID);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        frameRenderBuffer.position(0);
        GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, previewWidth, previewHeight,
                GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, frameRenderBuffer);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, cameraTextureUVID);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        frameRenderBuffer.position(4 * (previewWidth / 2) * (previewHeight / 2));
        GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, previewWidth / 2, previewHeight / 2,
                GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, frameRenderBuffer);
    }

    public void putData(byte[] data, int width, int height) {

        frameLock.lock();
        try {
            if (nv21Data == null || nv21Data.length != data.length) {
                nv21Data = new byte[data.length];
            }
            System.arraycopy(data, 0, nv21Data, 0, data.length);
            if (frameRenderBuffer == null) {
                int size = (width / 2) * (height / 2);
                ByteBuffer bb = ByteBuffer.allocateDirect(size * 6);
                bb.position(0);
                frameRenderBuffer = bb;
                bwSize = size;
            }

            previewWidth = width;
            previewHeight = height;

            isReady = true;
        } finally {
            frameLock.unlock();
        }
    }

    protected void onBindTexture() {
        //super.onBindTexture();
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, cameraTextureYID);
        GLES20.glUniform1i(uTextureSamplerYLocation, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, cameraTextureUVID);
        GLES20.glUniform1i(uTextureSamplerUVLocation, 1);
    }

    protected float[] getCubePositionData() {
        // X, Y, S, T
        return new float[]{
                1f, 1f, 0.0f, 1f, 0f,
                -1f, 1f, 0.0f, 0f, 0f,
                -1f, -1f, 0.0f, 0f, 1f,
                1f, 1f, 0.0f, 1f, 0f,
                -1f, -1f, 0.0f, 0f, 1f,
                1f, -1f, 0.0f, 1f, 1f,
        };
    }

    protected String getVertexFile() {
        return "uniform mat4 u_MVPMatrix;\n" +
                "attribute vec4 a_Position;\n" +
                "attribute vec4 aTextureCoordinate;\n" +
                "varying vec2 vTextureCoord;\n" +
                "void main()\n" +
                "{\n" +
                "    vTextureCoord = (aTextureCoordinate).xy;\n" +
                " \tgl_Position = a_Position;\n" +
                "}";
    }

    protected String getFragmentFile() {
        return "precision mediump float;\n" +
                "varying vec2 vTextureCoord;\n" +
                "uniform sampler2D uTextureSamplerY;\n" +
                "uniform sampler2D uTextureSamplerUV;\n" +
                "\n" +
                "const lowp mat3 M = mat3( 1, 1, 1, 0, -.18732, 1.8556, 1.57481, -.46813, 0 );\n" +
                "\n" +
                "void main()\n" +
                "{\n" +
                "    lowp vec3 yuv;\n" +
                "    lowp vec3 rgb;\n" +
                "    yuv.x = texture2D(uTextureSamplerY, vTextureCoord).r;\n" +
                "    yuv.yz = texture2D(uTextureSamplerUV, vTextureCoord).ar - vec2(0.5, 0.5);\n" +
                "    rgb = M * yuv;\n" +
                "    gl_FragColor = vec4(rgb, 1.0);\n" +
                "}";
    }
}
