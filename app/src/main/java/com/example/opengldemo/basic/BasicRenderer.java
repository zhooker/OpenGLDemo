package com.example.opengldemo.basic;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.example.opengldemo.base.BaseRenderer;

/**
 * BasicRenderer
 */
public class BasicRenderer extends BaseRenderer {

    public BasicRenderer(Context context) {
        super(context);
    }

    @Override
    protected void onDrawCube(float degree) {
        GLES30.glUseProgram(mPerVertexProgramHandle);

        // rotate cube.
        mCubeColors.position(0);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -6.0f);
        Matrix.rotateM(mModelMatrix, 0, degree, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, degree, 0.0f, 1.0f, 0.0f);
        drawCube();

        // rotate cube.
        mCubeColors.position(4 * 6);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -6.0f);
        Matrix.rotateM(mModelMatrix, 0, degree, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, degree, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, 90, 1.0f, 0.0f, 0.0f);
        drawCube();

        // rotate cube.
        mCubeColors.position(8 * 6);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -6.0f);
        Matrix.rotateM(mModelMatrix, 0, degree, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, degree, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, -90, 1.0f, 0.0f, 0.0f);
        drawCube();

        // rotate cube.
        mCubeColors.position(12 * 6);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -6.0f);
        Matrix.rotateM(mModelMatrix, 0, degree, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, degree, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, 90, 0.0f, 1.0f, 0.0f);
        drawCube();

        // rotate cube.
        mCubeColors.position(16 * 6);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -6.0f);
        Matrix.rotateM(mModelMatrix, 0, degree, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, degree, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, -90, 0.0f, 1.0f, 0.0f);
        drawCube();

        // rotate cube.
        mCubeColors.position(20 * 6);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -6.0f);
        Matrix.rotateM(mModelMatrix, 0, degree, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, degree, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, 180, 0.0f, 1.0f, 0.0f);
        drawCube();
    }
}
