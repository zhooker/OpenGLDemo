package com.example.opengldemo.demo.camera;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.opengldemo.demo.camera.gl.OpenglUtils;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * CameraFilterRenderer
 */
public class CameraNv21Renderer extends CameraRendererBase {

    private int cameraTextureYID;
    private int cameraTextureUVID;

    protected int uTextureSamplerYLocation;
    protected int uTextureSamplerUVLocation;

    protected int previewWidth = 1280;
    protected int previewHeight = 720;
    protected ByteBuffer frameRenderBuffer = null;

    private int bwSize;
    private boolean isReady = false;
    private byte[] nv21Data;
    protected ReentrantLock frameLock = new ReentrantLock();

    public CameraNv21Renderer(GLSurfaceView glSurfaceView) {
        super(glSurfaceView);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        super.onSurfaceCreated(glUnused, config);

        int[] textureNames = new int[2];

        GLES20.glGenTextures(1, textureNames, 0);
        cameraTextureYID = textureNames[0];

        GLES20.glGenTextures(1, textureNames, 1);
        cameraTextureUVID = textureNames[1];

        uTextureSamplerYLocation = GLES20.glGetUniformLocation(mProgramHandle, "uTextureSamplerY");
        uTextureSamplerUVLocation = GLES20.glGetUniformLocation(mProgramHandle, "uTextureSamplerUV");
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        super.onSurfaceChanged(glUnused, width, height);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        //super.onDrawFrame(glUnused);

        if (!isReady) {
            return;
        }

        if (!textureInit) {
            initializeTexture();
            textureInit = true;
        }

        updateRenderBuffer();

        updateRenderTexture();

        super.onDrawFrame(glUnused);
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
                frameRenderBuffer = OpenglUtils.makeByteBuffer(size * 6);
                bwSize = size;
            }
            isReady = true;
        } finally {
            frameLock.unlock();
        }
    }

    @Override
    protected void onBindTexture() {
        //super.onBindTexture();
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, cameraTextureYID);
        GLES20.glUniform1i(uTextureSamplerYLocation, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, cameraTextureUVID);
        GLES20.glUniform1i(uTextureSamplerUVLocation, 1);
    }

    @Override
    protected String getVertexFile() {
        return "demo/camera/vertex_texture.glsl";
    }

    @Override
    protected String getFragmentFile() {
        return "demo/camera/fragment_texture.glsl";
    }

    @Override
    protected float[] getCubePositionData() {
        // X, Y, S, T
        return new float[]{
                1f,     1f,     0.0f,   0f, 0f,
                -1f,    1f,     0.0f,   0f, 1f,
                -1f,    -1f,    0.0f,   1f, 1f,
                1f,     1f,     0.0f,   0f, 0f,
                -1f,    -1f,    0.0f,   1f, 1f,
                1f,     -1f,    0.0f,   1f, 0f,
        };
    }
}
