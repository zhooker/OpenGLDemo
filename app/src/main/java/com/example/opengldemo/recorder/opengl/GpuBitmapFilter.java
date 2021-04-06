package com.example.opengldemo.recorder.opengl;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.FloatBuffer;

public class GpuBitmapFilter extends GPUImageFilter {

    private int mTextureId = OpenGlUtils.NO_TEXTURE;

    public GpuBitmapFilter() {
        super(NO_FILTER_VERTEX_SHADER, NO_FILTER_FRAGMENT_SHADER);
    }

    @Override
    public void onInit() {
        super.onInit();
    }

    public void loadBitmap(Bitmap bitmap, int width, int height) {
        int[] textures = new int[1];
        if (mTextureId == OpenGlUtils.NO_TEXTURE) {
            GLES20.glGenTextures(1, textures, 0);
            OpenGlUtils.bindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
            textures[0] = mTextureId;
        }
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        mTextureId = textures[0];
    }

    @Override
    public void onDraw(int textureId, FloatBuffer cubeBuffer, FloatBuffer textureBuffer) {
        super.onDraw(mTextureId, cubeBuffer, textureBuffer);
    }

    @Override
    protected void onUninit() {
        super.onUninit();
    }
}
