package com.example.opengldemo.light;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.example.opengldemo.base.BaseRenderer;
import com.example.opengldemo.util.CubeUtil;
import com.example.opengldemo.util.ProgramUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * @author zhuangsj
 * @created 2018/8/29
 */
public class LightRenderer extends BaseRenderer {

    protected final int mNormalDataSize = 3;

    protected final FloatBuffer mCubeNormals;

    protected final float[] mLightPosInModelSpace = new float[]{0.0f, 0.0f, 0.0f, 1.0f};
    protected final float[] mLightPosInWorldSpace = new float[4];
    protected final float[] mLightPosInEyeSpace = new float[4];

    protected int mPointProgramHandle;
    protected int mMVMatrixHandle;
    protected int mLightPosHandle;
    protected int mNormalHandle;

    protected float[] mLightModelMatrix = new float[16];

    public LightRenderer(Context context) {
        super(context);

        final float[] normal = getCubeNormal();
        mCubeNormals = ByteBuffer.allocateDirect(normal.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeNormals.put(normal).position(0);
    }

    @Override
    protected void initPrograms() {
        super.initPrograms();

        // Define a simple shader program for our point.
        final String pointVertexShader =
                "#version 300 es                \n"
                        + "uniform mat4 u_MVPMatrix;      \n"
                        + "in vec4 a_Position;            \n"
                        + "void main()                    \n"
                        + "{                              \n"
                        + "   gl_Position = u_MVPMatrix   \n"
                        + "               * a_Position;   \n"
                        + "   gl_PointSize = 5.0;         \n"
                        + "}                              \n";

        final String pointFragmentShader =
                "#version 300 es                \n"
                        + "precision mediump float;       \n"
                        + "out vec4 FragColor;            \n"
                        + "void main()                    \n"
                        + "{                              \n"
                        + "   FragColor = vec4(1.0,       \n"
                        + "   1.0, 1.0, 1.0);             \n"
                        + "}                              \n";

        mPointProgramHandle = ProgramUtil.createAndLinkProgram(pointVertexShader, pointFragmentShader,
                new String[]{"a_Position"});
    }

    @Override
    protected void initHandlers() {
        super.initHandlers();
        mMVMatrixHandle = GLES30.glGetUniformLocation(mPerVertexProgramHandle, "u_MVMatrix");
        mLightPosHandle = GLES30.glGetUniformLocation(mPerVertexProgramHandle, "u_LightPos");
        mNormalHandle = GLES30.glGetAttribLocation(mPerVertexProgramHandle, "a_Normal");
    }

    @Override
    protected String getVertexShader() {
        return "light/vertex_light.glsl";
    }

    @Override
    protected String getFragmentShader() {
        return "light/fragment_light.glsl";
    }

    @Override
    protected float[] getCubePosition() {
        return CubeUtil.cubePositionData;
    }

    protected float[] getCubeNormal() {
        return CubeUtil.cubeNormalData;
    }

    @Override
    protected void onDrawCube(float degree) {
        //super.onDrawCube(degree);
        GLES30.glUseProgram(mPerVertexProgramHandle);

        // 计算并传递 光源 位置
        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, -5.0f);
        Matrix.rotateM(mLightModelMatrix, 0, degree, 0.0f, 1.0f, 0.0f);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, 2.0f);

        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0);
        GLES30.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

        // Draw some cubes.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 4.0f, 0.0f, -7.0f);
        Matrix.rotateM(mModelMatrix, 0, degree, 1.0f, 0.0f, 0.0f);
        drawCube();

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, -4.0f, 0.0f, -7.0f);
        Matrix.rotateM(mModelMatrix, 0, degree, 0.0f, 1.0f, 0.0f);
        drawCube();

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 4.0f, -7.0f);
        Matrix.rotateM(mModelMatrix, 0, degree, 0.0f, 0.0f, 1.0f);
        drawCube();

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, -4.0f, -7.0f);
        drawCube();

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -5.0f);
        Matrix.rotateM(mModelMatrix, 0, degree, 1.0f, 1.0f, 0.0f);
        drawCube();

        // 画光源
        onDrawLight();
    }

    @Override
    protected void drawCube() {
        // 坐标
        mCubePositions.position(0);
        GLES30.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES30.GL_FLOAT, false,
                0, mCubePositions);
        GLES30.glEnableVertexAttribArray(mPositionHandle);

        // 颜色
        mCubeColors.position(0);
        GLES30.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES30.GL_FLOAT, false,
                0, mCubeColors);
        GLES30.glEnableVertexAttribArray(mColorHandle);

        // 坐标向量
        mCubeNormals.position(0);
        GLES30.glVertexAttribPointer(mNormalHandle, mNormalDataSize, GLES30.GL_FLOAT, false,
                0, mCubeNormals);
        GLES30.glEnableVertexAttribArray(mNormalHandle);


        // 传递 ModelView Matrix
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        GLES30.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);

        // 传递 ModelViewProjection Matrix
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Draw the cube.
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 36);
    }

    protected void onDrawLight() {
        GLES30.glUseProgram(mPointProgramHandle);

        final int pointMVPMatrixHandle = GLES30.glGetUniformLocation(mPointProgramHandle, "u_MVPMatrix");
        final int pointPositionHandle = GLES30.glGetAttribLocation(mPointProgramHandle, "a_Position");

        // 坐标
        GLES30.glVertexAttrib3f(pointPositionHandle, mLightPosInModelSpace[0], mLightPosInModelSpace[1], mLightPosInModelSpace[2]);
        GLES30.glDisableVertexAttribArray(pointPositionHandle);

        // 传递 ModelViewProjection Matrix
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mLightModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES30.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Draw the point.
        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, 1);
    }
}
