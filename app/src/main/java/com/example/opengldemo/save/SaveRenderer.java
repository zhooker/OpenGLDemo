package com.example.opengldemo.save;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Size;

import com.example.opengldemo.R;
import com.example.opengldemo.util.CubeUtil;
import com.example.opengldemo.util.ProgramUtil;
import com.example.opengldemo.util.TextureHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * BasicRenderer
 */
public class SaveRenderer implements GLSurfaceView.Renderer
{
	private float[] mModelMatrix = new float[16];
	private float[] mViewMatrix = new float[16];
	private float[] mProjectionMatrix = new float[16];
	private float[] mMVPMatrix = new float[16];

	/** Store our model data in a float buffer. */
	private final FloatBuffer mCubePositions;
    private final FloatBuffer mCubeColors;

    protected int mSimpleProgramHandle;
    protected int mPerVertexProgramHandle;
	private int mMVPMatrixHandle;
    private int mModelMatrixHandle;
	private int mPositionHandle;
	private int mTextureHandle;

	protected int mTextureID;
    protected int mFrameBufferID;
    protected int mRenderBufferID;

	private final int mBytesPerFloat = 4;
	private final int mPositionDataSize = 3;
	private final int mTextureDataSize = 2;

    private Context context;
    private Size size;

	public SaveRenderer(Context context)
	{
	    this.context = context;
		// Initialize the buffers.
		mCubePositions = ByteBuffer.allocateDirect(cubePositionData.length * mBytesPerFloat)
        .order(ByteOrder.nativeOrder()).asFloatBuffer();
		mCubePositions.put(cubePositionData).position(0);

        mCubeColors = ByteBuffer.allocateDirect(cubeColorData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeColors.put(cubeColorData).position(0);
	}

	@Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config)
	{
		// Set the background clear color to black.
		GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		// Use culling to remove back faces.
		GLES30.glEnable(GLES30.GL_CULL_FACE);

		// Enable depth testing
		GLES30.glEnable(GLES30.GL_DEPTH_TEST);

		// Position the eye in front of the origin.
		final float eyeX = 0.0f;
		final float eyeY = 0.0f;
		final float eyeZ = -1.5f;

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
		mPerVertexProgramHandle = ProgramUtil.createAndLinkProgram(context, "save/vertex_texture.glsl", "save/fragment_texture.glsl",
				new String[] {"a_Position",  "aTextureCoordinate"});

        mSimpleProgramHandle = ProgramUtil.createAndLinkProgram(context, "save/vertex_simple.glsl", "save/fragment_simple.glsl",
                new String[] {"a_Position", "a_Color"});

        // Set program handles for cube drawing.
        mMVPMatrixHandle = GLES30.glGetUniformLocation(mPerVertexProgramHandle, "u_VPMatrix");
        mModelMatrixHandle = GLES30.glGetUniformLocation(mPerVertexProgramHandle, "u_ModelMatrix");
        mPositionHandle = GLES30.glGetAttribLocation(mPerVertexProgramHandle, "a_Position");
        mTextureHandle = GLES30.glGetAttribLocation(mPerVertexProgramHandle, "aTextureCoordinate");

        //mTextureID = TextureHelper.loadTexture(context, R.drawable.wall);
        initFrameBuffer(128, 128);
	}

	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height)
	{
		// Set the OpenGL viewport to the same size as the surface.
		GLES30.glViewport(0, 0, width, height);

        size = new Size(width,height);

		// Create a new perspective projection matrix. The height will stay the same
		// while the width will vary as per aspect ratio.
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
        // Do a complete rotation every 10 seconds.
        long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);

        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferID);
        GLES30.glUseProgram(mSimpleProgramHandle);
        GLES30.glViewport(0, 0, 128, 128);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        //GLES30.glClearColor(1, 1, 1, 1);
        drawCube2();

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES30.glUseProgram(mPerVertexProgramHandle);
        GLES30.glViewport(0, 0, size.getWidth(), size.getHeight());
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        //GLES30.glClearColor(0, 0, 0, 1);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureID);
        GLES30.glUniform1i(mTextureHandle, 0);

        // rotate cube.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -6.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        drawCube();

        // rotate cube.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -6.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, 90, 1.0f, 0.0f, 0.0f);
        drawCube();

        // rotate cube.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -6.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, -90, 1.0f, 0.0f, 0.0f);
        drawCube();

        // rotate cube.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -6.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, 90, 0.0f, 1.0f, 0.0f);
        drawCube();

        // rotate cube.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -6.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, -90, 0.0f, 1.0f, 0.0f);
        drawCube();

        // rotate cube.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -6.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, 180, 0.0f, 1.0f, 0.0f);
        drawCube();
	}

	protected void initFrameBuffer(int w, int h) {
        // 生成Texture
        int[] mFrameBufferTextures = new int[1];
        GLES20.glGenTextures(1, mFrameBufferTextures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFrameBufferTextures[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, w, h, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        // 生成Renderbuffer
        int [] renderbuffers = new int[1];
        GLES20.glGenRenderbuffers(1, renderbuffers, 0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderbuffers[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, w, h);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);

        // 生成FrameBuffer
        int[] mFrameBuffers = new int[1];
        GLES20.glGenFramebuffers(1, mFrameBuffers, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
        // 关联FrameBuffer和Texture、RenderBuffer
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, mFrameBufferTextures[0], 0);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, renderbuffers[0]);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        mFrameBufferID = mFrameBuffers[0];
        mTextureID = mFrameBufferTextures[0];
    }


	/**
	 * Draws a cube.
	 */
    protected void drawCube()
	{
		// Pass in the position information
		mCubePositions.position(0);
        GLES30.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES30.GL_FLOAT, false,
        		5*mBytesPerFloat, mCubePositions);
        GLES30.glEnableVertexAttribArray(mPositionHandle);

        mCubePositions.position(3);
        GLES30.glVertexAttribPointer(mTextureHandle, mTextureDataSize, GLES30.GL_FLOAT, false,
                5*mBytesPerFloat, mCubePositions);
        GLES30.glEnableVertexAttribArray(mTextureHandle);

        GLES30.glUniformMatrix4fv(mModelMatrixHandle, 1, false, mModelMatrix, 0);

        // Draw the cube.
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, 6);
	}

    protected void drawCube2()
    {
        int positionHandle = GLES30.glGetAttribLocation(mSimpleProgramHandle, "a_Position");
        mCubePositions.position(0);
        GLES30.glVertexAttribPointer(positionHandle, mPositionDataSize, GLES30.GL_FLOAT, false,
                5*mBytesPerFloat, mCubePositions);
        GLES30.glEnableVertexAttribArray(positionHandle);

        int colorHandle = GLES30.glGetAttribLocation(mSimpleProgramHandle, "a_Color");
        mCubeColors.position(0);
        GLES30.glVertexAttribPointer(colorHandle, 4, GLES30.GL_FLOAT, false,
                0, mCubeColors);
        GLES30.glEnableVertexAttribArray(colorHandle);

        // Draw the cube.
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, 6);
    }

	public Bitmap readImage(int width,int height) {
        //GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferID);
        IntBuffer ib = IntBuffer.allocate(width * height);
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ib);
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        result.copyPixelsFromBuffer(ib);
        //GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        return result;
    }

    // X, Y, Z
    public static final float[] cubePositionData =
            {
                    0.0f, 0.0f, 1.0f,         0.5f, 0.5f,
                    -1.0f, -1.0f, 1.0f,       0f, 1f,
                    1.0f, -1.0f, 1.0f,        1f, 1f,
                    1.0f, 1.0f, 1.0f,         1f, 0f,
                    -1.0f, 1.0f, 1.0f,        0f, 0f,
                    -1.0f, -1.0f, 1.0f,       0f, 1f
            };

    public static final float[] cubeColorData =
            {
                    // Front face (red)
                    1.0f, 0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f, 1.0f,
                    0.0f, 0.0f, 1.0f, 1.0f,
                    0.0f, 0.0f, 1.0f, 1.0f,
                    0.0f, 0.0f, 1.0f, 1.0f,
                    0.0f, 0.0f, 1.0f, 1.0f,
            };

}
