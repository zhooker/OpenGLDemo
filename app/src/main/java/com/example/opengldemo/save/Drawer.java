package com.example.opengldemo.save;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.example.opengldemo.util.FileUtil;
import com.example.opengldemo.util.L;
import com.example.opengldemo.util.ProgramUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGenerateMipmap;
import static android.opengl.GLUtils.texImage2D;

/**
 * Created by zhuangsj on 18-2-8.
 */

public class Drawer {

    int mProgramHandle;
    int mPositionHandle;
    int mMVPMatrixHandle;
    int aTextureCoordLocation;
    int uTextureSamplerLocation;
    final FloatBuffer mCubePositions;

    // X, Y, S, T
    public static final float[] cubePositionData =
            {
                    1f,  1f,  0.0f, 1f,  1f,
                    -1f,  1f, 0.0f, 0f,  1f,
                    -1f, -1f, 0.0f, 0f,  0f,
                    1f,  1f,  0.0f, 1f,  1f,
                    -1f, -1f, 0.0f, 0f,  0f,
                    1f, -1f,  0.0f, 1f,  0f
            };

    private Context context;
    public Drawer(Context context) {
        this.context = context;
        mCubePositions = ByteBuffer.allocateDirect(cubePositionData.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubePositions.put(cubePositionData).position(0);

        mProgramHandle = ProgramUtil.createAndLinkProgram(context, "save/vertex_texture.glsl", "save/fragment_texture.glsl" ,
                new String[] {"a_Position",  "aTextureCoordinate"});

        mPositionHandle = GLES30.glGetAttribLocation(mProgramHandle, "a_Position");
        aTextureCoordLocation = GLES30.glGetAttribLocation(mProgramHandle, "aTextureCoordinate");
        mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
        uTextureSamplerLocation = GLES30.glGetUniformLocation(mProgramHandle, "uTextureSampler");
    }

    public Bitmap draw(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] mFrameBuffers = new int[1];
        int[] mFrameBufferTextures = new int[1];

        GLES20.glGenFramebuffers(1, mFrameBuffers, 0);
        GLES20.glGenTextures(1, mFrameBufferTextures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFrameBufferTextures[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, mFrameBufferTextures[0], 0);

        GLES20.glViewport(0, 0, width, height);
        int textureId = loadTexture(bitmap);

        drawFrame(textureId);

        IntBuffer ib = IntBuffer.allocate(width * height);
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ib);
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        result.copyPixelsFromBuffer(ib);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glDeleteTextures(1, new int[]{textureId}, 0);
        GLES20.glDeleteFramebuffers(mFrameBuffers.length, mFrameBuffers, 0);
        GLES20.glDeleteTextures(mFrameBufferTextures.length, mFrameBufferTextures, 0);

        return result;
    }

    public void drawFrame(int mTextureId)
    {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        // Set our per-vertex lighting program.
        GLES30.glUseProgram(mProgramHandle);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GL_TEXTURE_2D, mTextureId);
        GLES30.glUniform1i(uTextureSamplerLocation, 0);

        // Pass in the position information
        mCubePositions.position(0);
        GLES30.glVertexAttribPointer(mPositionHandle, 3, GLES30.GL_FLOAT, false,
                5*4, mCubePositions);
        GLES30.glEnableVertexAttribArray(mPositionHandle);

        mCubePositions.position(3);
        GLES30.glVertexAttribPointer(aTextureCoordLocation, 2, GLES30.GL_FLOAT, false,
                5*4, mCubePositions);
        GLES30.glEnableVertexAttribArray(aTextureCoordLocation);

        // Draw the cube.
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6);
    }

    public int loadTexture(Bitmap bitmap) {
        final int[] textureObjectIds = new int[1];
        glGenTextures(1, textureObjectIds, 0);

        if (textureObjectIds[0] == 0) {
            L.d("Could not generate a new OpenGL texture object.");
            return 0;
        }

        glBindTexture(GL_TEXTURE_2D, textureObjectIds[0]);

        GLES30.glTexParameterf(GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLES30.glTexParameterf(GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES30.glTexParameterf(GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameterf(GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);

        glGenerateMipmap(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, 0);

        return textureObjectIds[0];
    }

    public void doSaveImage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                doSaveImage("/storage/emulated/0/games/gomoku/img/nopack/download/BD208310B2148A9B91FAE878B377C928.png");
            }
        }).run();

    }

    public void doSaveImage(String path) {
        Bitmap input = BitmapFactory.decodeFile(path);
        L.d("input = " + (input == null? null : (input.getWidth() + "x" + input.getHeight())));
        Bitmap result = draw(input);
        L.d("result = " + (result == null? null : (result.getWidth() + "x" + result.getHeight())));
        FileUtil.saveBitmap2File(result, "teeeest.jpg");
    }
}

