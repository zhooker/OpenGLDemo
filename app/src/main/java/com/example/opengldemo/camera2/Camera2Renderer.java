package com.example.opengldemo.camera2;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.opengldemo.R;
import com.example.opengldemo.util.ProgramUtil;
import com.example.opengldemo.util.TextureHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_TEXTURE_2D;

/**
 * CameraFilterRenderer
 */
public class Camera2Renderer implements GLSurfaceView.Renderer
{
    protected final int mBytesPerFloat = 4;
    private float[] mModelMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    protected int mProgramHandle;
    protected int mPositionHandle;
    protected int mMVPMatrixHandle;
    protected int aTextureCoordLocation;
    protected int uTextureSamplerLocation;
    protected int uTextureSamplerLocation1;
    protected int uTextureSamplerLocation2;

    protected int mTextureId;
    protected int mTextureId1;
    protected int mTextureId2;

    protected GLSurfaceView glSurfaceView;
    private final FloatBuffer mCubePositions;

	public Camera2Renderer(GLSurfaceView glSurfaceView)
	{
        this.glSurfaceView = glSurfaceView;
        // Initialize the buffers.
		mCubePositions = ByteBuffer.allocateDirect(cubePositionData.length * mBytesPerFloat)
        .order(ByteOrder.nativeOrder()).asFloatBuffer();							
		mCubePositions.put(cubePositionData).position(0);
	}

    @Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config)
	{
		//GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		//GLES30.glEnable(GLES30.GL_CULL_FACE);
		//GLES30.glEnable(GLES30.GL_DEPTH_TEST);

        // Position the eye in front of the origin.
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 2.0f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;


        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        // init program
		mProgramHandle = ProgramUtil.createAndLinkProgram(glSurfaceView.getContext(), "camera/vertex_texture.glsl", "camera/fragment_texture.glsl",
				new String[] {"a_Position",  "aTextureCoordinate"});

        // Set program handles for cube drawing.
        mPositionHandle = GLES30.glGetAttribLocation(mProgramHandle, "a_Position");
        aTextureCoordLocation = GLES30.glGetAttribLocation(mProgramHandle, "aTextureCoordinate");
        mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
        uTextureSamplerLocation = GLES30.glGetUniformLocation(mProgramHandle, "uTextureSampler");
        uTextureSamplerLocation1 = GLES30.glGetUniformLocation(mProgramHandle, "uTextureSampler1");
        uTextureSamplerLocation2 = GLES30.glGetUniformLocation(mProgramHandle, "uTextureSampler2");

        mTextureId = TextureHelper.loadTexture(glSurfaceView.getContext(), R.drawable.wall);
    }
		
	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) 
	{
		// Set the OpenGL viewport to the same size as the surface.
		GLES30.glViewport(0, 0, width, height);

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
	public void onDrawFrame(GL10 glUnused) 
	{
		GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        // Set our per-vertex lighting program.
        GLES30.glUseProgram(mProgramHandle);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GL_TEXTURE_2D, mTextureId);
        GLES30.glUniform1i(uTextureSamplerLocation, 0);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Pass in the position information
        mCubePositions.position(0);
        GLES30.glVertexAttribPointer(mPositionHandle, 3, GLES30.GL_FLOAT, false,
                5*mBytesPerFloat, mCubePositions);
        GLES30.glEnableVertexAttribArray(mPositionHandle);

        mCubePositions.position(3);
        GLES30.glVertexAttribPointer(aTextureCoordLocation, 2, GLES30.GL_FLOAT, false,
                5*mBytesPerFloat, mCubePositions);
        GLES30.glEnableVertexAttribArray(aTextureCoordLocation);

        // Draw the cube.
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6);
	}

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
}
