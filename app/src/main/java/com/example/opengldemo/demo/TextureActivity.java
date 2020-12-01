package com.example.opengldemo.demo;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.example.opengldemo.R;
import com.example.opengldemo.util.AssetsUtils;
import com.example.opengldemo.util.TextureHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class TextureActivity extends BaseActivity {

    @Override
    protected GLSurfaceView.Renderer getRenderer() {
        return new MyRenderer(this);
    }

    private static class MyRenderer implements GLSurfaceView.Renderer {

        private static final float[] VERTEX = {
                1,     1,      1,
                -1,    1,      1,
                -1,    -1,     1,
                1,    -1,      1,
        };

        private static final float[] COLOR = {
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,

                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,

                0.0f, 0.0f, 1.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 1.0f,

                1.0f, 0.0f, 1.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 1.0f,

                1.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 1.0f, 0.0f, 1.0f,

                0.0f, 1.0f, 1.0f, 1.0f,
                0.0f, 1.0f, 1.0f, 1.0f,
                0.0f, 1.0f, 1.0f, 1.0f,
                0.0f, 1.0f, 1.0f, 1.0f,
        };

        private float[] TEXTURE = {
                1.0f, 0.0f,
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f
        };

        protected Context mContext;
        protected int mProgram;
        protected int mPositionHandle;
        protected int mColorHandle;
        protected final FloatBuffer mVertexBuffer;
        protected final FloatBuffer mColorBuffer;
        protected final FloatBuffer mTextureBuffer;

        protected int textureId;
        protected int mTextureHandle;
        protected int mTextureCoordindate;

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

            mTextureBuffer = ByteBuffer.allocateDirect(TEXTURE.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(TEXTURE);
            mTextureBuffer.position(0);
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
            GLES30.glEnable(GLES30.GL_CULL_FACE);

            mProgram = GLES20.glCreateProgram();
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, AssetsUtils.read(mContext, "demo/vertex_texture.glsl"));
            int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, AssetsUtils.read(mContext, "demo/fragment_texture.glsl"));
            GLES20.glAttachShader(mProgram, vertexShader);
            GLES20.glAttachShader(mProgram, fragmentShader);
            GLES20.glLinkProgram(mProgram);

            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");
            mColorHandle = GLES30.glGetAttribLocation(mProgram, "a_Color");
            mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgram, "u_MVPMatrix");
            mTextureCoordindate = GLES30.glGetAttribLocation(mProgram, "a_TextureCoordinates");
            mTextureHandle = GLES30.glGetUniformLocation(mProgram, "u_TextureUnit");

            textureId = TextureHelper.loadTexture(mContext, R.drawable.boxwood);

            final float eyeX = 0.0f;
            final float eyeY = 0.0f;
            final float eyeZ = 4.0f;

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

            long time = SystemClock.uptimeMillis() % 10000L;
            float degree = (360.0f / 10000.0f) * ((int) time);

            GLES20.glUseProgram(mProgram);

            // rotate cube.
            mColorBuffer.position(0);
            mVertexBuffer.position(0);
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.rotateM(mModelMatrix, 0, degree, 1.0f, 0.0f, 0.0f);
            Matrix.rotateM(mModelMatrix, 0, degree, 0.0f, 1.0f, 0.0f);
            drawSquare();

            // rotate cube.
            mColorBuffer.position(4 * 4);
            mVertexBuffer.position(0);
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.rotateM(mModelMatrix, 0, degree, 1.0f, 0.0f, 0.0f);
            Matrix.rotateM(mModelMatrix, 0, degree, 0.0f, 1.0f, 0.0f);
            Matrix.rotateM(mModelMatrix, 0, 90, 1.0f, 0.0f, 0.0f);
            drawSquare();

            // rotate cube.
            mColorBuffer.position(8 * 4);
            mVertexBuffer.position(0);
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.rotateM(mModelMatrix, 0, degree, 1.0f, 0.0f, 0.0f);
            Matrix.rotateM(mModelMatrix, 0, degree, 0.0f, 1.0f, 0.0f);
            Matrix.rotateM(mModelMatrix, 0, -90, 1.0f, 0.0f, 0.0f);
            drawSquare();

            // rotate cube.
            mColorBuffer.position(12 * 4);
            mVertexBuffer.position(0);
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.rotateM(mModelMatrix, 0, degree, 1.0f, 0.0f, 0.0f);
            Matrix.rotateM(mModelMatrix, 0, degree, 0.0f, 1.0f, 0.0f);
            Matrix.rotateM(mModelMatrix, 0, 90, 0.0f, 1.0f, 0.0f);
            drawSquare();

            // rotate cube.
            mColorBuffer.position(16 * 4);
            mVertexBuffer.position(0);
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.rotateM(mModelMatrix, 0, degree, 1.0f, 0.0f, 0.0f);
            Matrix.rotateM(mModelMatrix, 0, degree, 0.0f, 1.0f, 0.0f);
            Matrix.rotateM(mModelMatrix, 0, -90, 0.0f, 1.0f, 0.0f);
            drawSquare();

            // rotate cube.
            mColorBuffer.position(20 * 4);
            mVertexBuffer.position(0);
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.rotateM(mModelMatrix, 0, degree, 1.0f, 0.0f, 0.0f);
            Matrix.rotateM(mModelMatrix, 0, degree, 0.0f, 1.0f, 0.0f);
            Matrix.rotateM(mModelMatrix, 0, 180, 0.0f, 1.0f, 0.0f);
            drawSquare();
        }

        private void drawSquare() {
            mTextureBuffer.position(0);
            GLES30.glVertexAttribPointer(mTextureCoordindate, 2, GLES30.GL_FLOAT, false,0, mTextureBuffer);
            GLES30.glEnableVertexAttribArray(mTextureCoordindate);

            // Set the active textureId unit to textureId unit 0.
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);
            GLES30.glUniform1i(mTextureHandle, 0);

            // draw square
            GLES20.glVertexAttribPointer(mColorHandle, 4, GLES30.GL_FLOAT, false, 0, mColorBuffer);
            GLES20.glEnableVertexAttribArray(mColorHandle);

            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);

            Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
            GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);
        }
    }
}