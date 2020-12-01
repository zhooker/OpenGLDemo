package com.example.opengldemo.demo;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import com.example.opengldemo.filter.CameraManager;
import com.example.opengldemo.util.AssetsUtils;
import com.example.opengldemo.util.TextureHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraPreviewActivity extends BaseActivity {

    @Override
    protected GLSurfaceView.Renderer getRenderer() {
        return new MyRenderer(this, getGLSurfaceView());
    }

    private static class MyRenderer implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

        private static final float[] VERTEX = {
                 1,     1,      0,
                -1,     1,      0,
                -1,    -1,      0,
                 1,    -1,      0,
        };

        private static final float[] TEXTURE = {
                1.0f, 0.0f,
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f
        };

        protected Context mContext;
        protected int mProgram;
        protected int mPositionHandle;
        protected final FloatBuffer mVertexBuffer;
        protected final FloatBuffer mTextureBuffer;

        protected CameraManager mCameraManager;
        protected SurfaceTexture surfaceTexture;
        protected GLSurfaceView glSurfaceView;
        protected int textureId;
        protected int mTextureHandle;
        protected int mTextureCoordindate;
        protected float[] transformMatrix = new float[16];

        MyRenderer(Context context, GLSurfaceView glSurfaceView) {
            this.mContext = context;
            this.glSurfaceView = glSurfaceView;
            this.mCameraManager = new CameraManager();

            mVertexBuffer = ByteBuffer.allocateDirect(VERTEX.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(VERTEX);
            mVertexBuffer.position(0);

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

            mProgram = GLES20.glCreateProgram();
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, AssetsUtils.read(mContext, "demo/vertex_camera.glsl"));
            int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, AssetsUtils.read(mContext, "demo/fragment_camera.glsl"));
            GLES20.glAttachShader(mProgram, vertexShader);
            GLES20.glAttachShader(mProgram, fragmentShader);
            GLES20.glLinkProgram(mProgram);

            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");
            mTextureCoordindate = GLES30.glGetAttribLocation(mProgram, "a_TextureCoordinates");
            mTextureHandle = GLES30.glGetUniformLocation(mProgram, "u_TextureUnit");

            textureId = TextureHelper.loadOESTexture(null);
            surfaceTexture = new SurfaceTexture(textureId);
            surfaceTexture.setOnFrameAvailableListener(this);

            if (mCameraManager.openCamera(Camera.CameraInfo.CAMERA_FACING_BACK)) {
                mCameraManager.setDisplayOrientation(270);
                try {
                    mCameraManager.setPreviewTexture(surfaceTexture);
                    Camera.Parameters parameters = mCameraManager.getParameters();
                    if (parameters != null) {
                        parameters.setPreviewSize(1280, 720);
                        mCameraManager.setParameters(parameters);
                    }
                    mCameraManager.startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onSurfaceChanged(GL10 unused, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
        }

        @Override
        public void onDrawFrame(GL10 unused) {
            if (surfaceTexture != null) {
                //更新纹理图像
                surfaceTexture.updateTexImage();
                //获取外部纹理的矩阵，用来确定纹理的采样位置，没有此矩阵可能导致图像翻转等问题
                surfaceTexture.getTransformMatrix(transformMatrix);
            }


            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            GLES20.glUseProgram(mProgram);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
            GLES30.glUniform1i(mTextureHandle, 0);

            int textureTranformHandle = GLES30.glGetUniformLocation(mProgram, "uTextureMatrix");
            GLES30.glUniformMatrix4fv(textureTranformHandle, 1, false, transformMatrix, 0);

            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);

            GLES30.glVertexAttribPointer(mTextureCoordindate, 2, GLES30.GL_FLOAT, false,0, mTextureBuffer);
            GLES30.glEnableVertexAttribArray(mTextureCoordindate);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);
        }

        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            this.glSurfaceView.requestRender();
        }
    }
}