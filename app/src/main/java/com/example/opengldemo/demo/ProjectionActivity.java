package com.example.opengldemo.demo;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.opengldemo.util.AssetsUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ProjectionActivity extends BaseActivity {

    @Override
    protected GLSurfaceView.Renderer getRenderer() {
        return new MyRenderer(this);
    }

    private static class MyRenderer implements GLSurfaceView.Renderer {

        private static final float[] VERTEX = {
                0f,     1f,     0,
                -1f,   -1f,     0,
                1f,    -1f,     0,
        };

        private static final float[] COLOR = {
                1.0f, 0.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 1.0f,
        };

        protected Context mContext;
        protected int mProgram;
        protected int mPositionHandle;
        protected int mColorHandle;
        protected final FloatBuffer mVertexBuffer;
        protected final FloatBuffer mColorBuffer;


        protected float[] mModelMatrix = new float[16];
        protected float[] mViewMatrix = new float[16];
        protected float[] mProjectionMatrix = new float[16];
        protected float[] mMVPMatrix = new float[16];
        protected int mMVPMatrixHandle;

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
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, AssetsUtils.read(mContext, "demo/vertex_projection.glsl"));
            int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, AssetsUtils.read(mContext, "demo/fragment_projection.glsl"));
            GLES20.glAttachShader(mProgram, vertexShader);
            GLES20.glAttachShader(mProgram, fragmentShader);
            GLES20.glLinkProgram(mProgram);

            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");
            mColorHandle = GLES30.glGetAttribLocation(mProgram, "a_Color");
            mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgram, "u_MVPMatrix");

            final float eyeX = 0.0f;
            final float eyeY = 0.0f;
            final float eyeZ = 2.0f;

            final float lookX = 0.0f;
            final float lookY = 0.0f;
            final float lookZ = -1.0f;

            final float upX = 0.0f;
            final float upY = 1.0f;
            final float upZ = 0.0f;

            Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
        }

        @Override
        public void onSurfaceChanged(GL10 unused, int width, int height) {
            GLES20.glViewport(0, 0, width, height);

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
        public void onDrawFrame(GL10 unused) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            GLES20.glUseProgram(mProgram);

            GLES20.glVertexAttribPointer(mColorHandle, 4, GLES30.GL_FLOAT, false, 0, mColorBuffer);
            GLES20.glEnableVertexAttribArray(mColorHandle);

            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);

            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
            GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
        }
    }
}