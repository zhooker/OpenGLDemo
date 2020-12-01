package com.example.opengldemo.demo;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import com.example.opengldemo.util.AssetsUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SimpleSquareActivity extends BaseActivity {

    @Override
    protected GLSurfaceView.Renderer getRenderer() {
        return new MyRenderer(this);
    }

    private static class MyRenderer implements GLSurfaceView.Renderer {

        private static final float[] VERTEX = {
                1,     1,      0,
                -1,    1,      0,
                -1,    -1,     0,
                1,    -1,      0,
        };

        private static final float[] COLOR = {
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
        };

        protected Context mContext;
        protected int mProgram;
        protected int mPositionHandle;
        protected int mColorHandle;
        protected final FloatBuffer mVertexBuffer;
        protected final FloatBuffer mColorBuffer;

        MyRenderer(Context context) {
            this.mContext = context;

            mVertexBuffer = ByteBuffer.allocateDirect(VERTEX.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(VERTEX);
            mVertexBuffer.position(0);

            mColorBuffer = ByteBuffer.allocateDirect(COLOR.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(COLOR);
            mColorBuffer.position(0);
        }

        static int loadShader(int type, String shaderCode) {
            int shader = GLES20.glCreateShader(type);
            GLES20.glShaderSource(shader, shaderCode);
            GLES20.glCompileShader(shader);
            return shader;
        }

        @Override
        public void onSurfaceCreated(GL10 unused, EGLConfig config) {
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

            mProgram = GLES20.glCreateProgram();
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, AssetsUtils.read(mContext, "demo/vertex_simple.glsl"));
            int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, AssetsUtils.read(mContext, "demo/fragment_simple.glsl"));
            GLES20.glAttachShader(mProgram, vertexShader);
            GLES20.glAttachShader(mProgram, fragmentShader);
            GLES20.glLinkProgram(mProgram);

            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");
            mColorHandle = GLES30.glGetAttribLocation(mProgram, "a_Color");
        }

        @Override
        public void onSurfaceChanged(GL10 unused, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
        }

        @Override
        public void onDrawFrame(GL10 unused) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            GLES20.glUseProgram(mProgram);

            GLES20.glVertexAttribPointer(mColorHandle, 4, GLES30.GL_FLOAT, false, 0, mColorBuffer);
            GLES20.glEnableVertexAttribArray(mColorHandle);

            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);
        }
    }
}