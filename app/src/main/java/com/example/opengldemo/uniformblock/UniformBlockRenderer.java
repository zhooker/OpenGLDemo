package com.example.opengldemo.uniformblock;

import android.opengl.GLES30;
import com.example.opengldemo.light.SpotLightRenderer2;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 *  UniformBlockRenderer 2018/2/2
 */
public class UniformBlockRenderer extends SpotLightRenderer2 {


    protected String getVertexShader()
    {
        // Define our per-pixel lighting shader.
        final String perPixelVertexShader =
                          "#version 300 es                \n"
                                  + " layout (std140) uniform Matrices  \n"
                                  + "  {                                \n"
                                  + "      mat4 u_MMatrix;              \n"
                                  + "      mat4 u_VMatrix;              \n"
                                  + "      mat4 u_PMatrix;              \n"
                                  + " };                                \n"
                        + "in vec4 a_Position;            \n"
                        + "in vec4 a_Color;               \n"
                        + "in vec3 a_Normal;              \n"

                        + "out vec3 v_Position;           \n"
                        + "out vec4 v_Color;              \n"
                        + "out vec3 v_Normal;             \n"

                        // The entry point for our vertex shader.
                        + "void main()                                                \n"
                        + "{                                                          \n"
                                  + " mat4 modelview = u_VMatrix * u_MMatrix; \n"
                                  + " mat4 modelviewprojection = u_PMatrix * modelview; \n"
                        // Transform the vertex into eye space.
                        + "   v_Position = vec3(modelview * a_Position);             \n"
                        // Pass through the color.
                        + "   v_Color = a_Color;                                      \n"
                        // Transform the normal's orientation into eye space.
                        + "   v_Normal = vec3(modelview * vec4(a_Normal, 0.0));      \n"
                        // gl_Position is a special variable used to store the final position.
                        // Multiply the vertex by the matrix to get the final point in normalized screen coordinates.
                        + "   gl_Position = modelviewprojection * a_Position;                 \n"
                        + "}                                                          \n";

        return perPixelVertexShader;
    }

    protected int[] textureObjectIds = new int[1];
    protected FloatBuffer mMBuffer,mVBuffer,mPBuffer;

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        super.onSurfaceCreated(glUnused, config);
        // 初始化 buffer
        mMBuffer = ByteBuffer.allocateDirect(mMVPMatrix.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVBuffer = ByteBuffer.allocateDirect(mMVPMatrix.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mPBuffer = ByteBuffer.allocateDirect(mMVPMatrix.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        // 绑定 uniform buffer 到 binding point 0
        GLES30.glGenBuffers(1, textureObjectIds, 0);
        GLES30.glBindBuffer(GLES30.GL_UNIFORM_BUFFER, textureObjectIds[0]);
        GLES30.glBufferData(GLES30.GL_UNIFORM_BUFFER, 3 * 4 * 4 * mBytesPerFloat, null, GLES30.GL_STATIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_UNIFORM_BUFFER, 0);
        GLES30.glBindBufferRange(GLES30.GL_UNIFORM_BUFFER, 0, textureObjectIds[0], 0, 3 * 4 * 4 * mBytesPerFloat);

        // 绑定 program 到 binding point 0, 这样program才能与buffer相通
        int uniformBlockIndexRed    = GLES30.glGetUniformBlockIndex(mPerVertexProgramHandle, "Matrices");
        GLES30.glUniformBlockBinding(mPerVertexProgramHandle, uniformBlockIndexRed, 0);

        // 设置 mViewMatrix 到 Uniform Block
        mVBuffer.clear();
        mVBuffer.put(mViewMatrix).position(0);
        GLES30.glBindBuffer(GLES30.GL_UNIFORM_BUFFER, textureObjectIds[0]);
        GLES30.glBufferSubData(GLES30.GL_UNIFORM_BUFFER, 4 * 4 * mBytesPerFloat, 4 * 4 * mBytesPerFloat, mVBuffer);
        GLES30.glBindBuffer(GLES30.GL_UNIFORM_BUFFER, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        super.onSurfaceChanged(glUnused, width, height);
        // 设置 mProjectionMatrix 到 Uniform Block
        mPBuffer.clear();
        mPBuffer.put(mProjectionMatrix).position(0);
        GLES30.glBindBuffer(GLES30.GL_UNIFORM_BUFFER, textureObjectIds[0]);
        GLES30.glBufferSubData(GLES30.GL_UNIFORM_BUFFER, 2 * 4 * 4 * mBytesPerFloat, 4 * 4 * mBytesPerFloat, mPBuffer);
        GLES30.glBindBuffer(GLES30.GL_UNIFORM_BUFFER, 0);
    }

    @Override
    protected void drawCube()
    {
        // Pass in the position information
        mCubePositions.position(0);
        GLES30.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES30.GL_FLOAT, false,
                0, mCubePositions);

        GLES30.glEnableVertexAttribArray(mPositionHandle);

        // Pass in the color information
        mCubeColors.position(0);
        GLES30.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES30.GL_FLOAT, false,
                0, mCubeColors);

        GLES30.glEnableVertexAttribArray(mColorHandle);

        // Pass in the normal information
        mCubeNormals.position(0);
        GLES30.glVertexAttribPointer(mNormalHandle, mNormalDataSize, GLES30.GL_FLOAT, false,
                0, mCubeNormals);

        GLES30.glEnableVertexAttribArray(mNormalHandle);


//        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
//        GLES30.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);

//        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
//        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // 设置 mModelMatrix 到 Uniform Block
        mMBuffer.clear();
        mMBuffer.put(mModelMatrix).position(0);
        GLES30.glBindBuffer(GLES30.GL_UNIFORM_BUFFER, textureObjectIds[0]);
        GLES30.glBufferSubData(GLES30.GL_UNIFORM_BUFFER, 0, 4 * 4 * mBytesPerFloat, mMBuffer);
        GLES30.glBindBuffer(GLES30.GL_UNIFORM_BUFFER, 0);

        GLES30.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 36);
    }
}
