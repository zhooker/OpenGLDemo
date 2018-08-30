package com.example.opengldemo.solar.system;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.example.opengldemo.R;
import com.example.opengldemo.texture.TextureRenderer;

/**
 * BasicRenderer
 */
public class SolarRenderer extends TextureRenderer {

    private Planet m_Earth;
    private final int mSlice = 50;

    public SolarRenderer(Context context) {
        super(context);
        m_Earth = new Planet(50, mSlice, 1f, 1.0f);
    }

    @Override
    protected int getTextureId() {
        return R.drawable.earth;
    }

    @Override
    protected void onDrawCube(float degree) {
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
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -5.0f);
        Matrix.rotateM(mModelMatrix, 0, degree, 1.0f, 0.0f, 1.0f);
        Matrix.rotateM(mModelMatrix, 0, degree, 0.0f, 1.0f, 0.0f);
        drawCube();

        // 画光源
        onDrawLight();
    }

    @Override
    protected void drawCube() {
        //super.drawCube();
        m_Earth.getM_TextureData().position(0);
        GLES30.glVertexAttribPointer(mTextureCoordindate, 2, GLES30.GL_FLOAT, false,
                0, m_Earth.getM_TextureData());
        GLES30.glEnableVertexAttribArray(mTextureCoordindate);


        // Set the active textureId unit to textureId unit 0.
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);
        GLES30.glUniform1i(mTextureHandle, 0);

        // 坐标
        m_Earth.getM_VertexData().position(0);
        GLES30.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES30.GL_FLOAT, false,
                0, m_Earth.getM_VertexData());
        GLES30.glEnableVertexAttribArray(mPositionHandle);

        // 颜色,用常量
        GLES30.glVertexAttrib4f(mColorHandle, 1f, 1f, 1f, 1f);
        GLES30.glDisableVertexAttribArray(mColorHandle);

        // 坐标向量
        m_Earth.getM_NormalData().position(0);
        GLES30.glVertexAttribPointer(mNormalHandle, mNormalDataSize, GLES30.GL_FLOAT, false,
                0, m_Earth.getM_NormalData());
        GLES30.glEnableVertexAttribArray(mNormalHandle);


        // 传递 ModelView Matrix
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        GLES30.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);

        // 传递 ModelViewProjection Matrix
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Draw the cube.
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, (mSlice + 1) * 2 * (mSlice - 1) + 2);
    }
}
