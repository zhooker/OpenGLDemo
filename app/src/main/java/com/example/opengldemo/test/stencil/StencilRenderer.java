package com.example.opengldemo.test.stencil;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.example.opengldemo.util.CubeUtil;
import com.example.opengldemo.util.ProgramUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * StencilRenderer  模版测试  2018/2/3
 */
public class StencilRenderer implements GLSurfaceView.Renderer
{
	private float[] mModelMatrix = new float[16];
	private float[] mViewMatrix = new float[16];
	private float[] mProjectionMatrix = new float[16];
	private float[] mMVPMatrix = new float[16];

	/** Store our model data in a float buffer. */
	private final FloatBuffer mCubePositions;
	private final FloatBuffer mCubeColors;

    protected int mStencilProgramHandle;
    protected int mPerVertexProgramHandle;
	private int mMVPMatrixHandle;
	private int mPositionHandle;
	private int mColorHandle;

	private final int mBytesPerFloat = 4;
	private final int mPositionDataSize = 3;
	private final int mColorDataSize = 4;


	public StencilRenderer()
	{
		// Initialize the buffers.
		mCubePositions = ByteBuffer.allocateDirect(CubeUtil.cubePositionData.length * mBytesPerFloat)
        .order(ByteOrder.nativeOrder()).asFloatBuffer();							
		mCubePositions.put(CubeUtil.cubePositionData).position(0);
		
		mCubeColors = ByteBuffer.allocateDirect(CubeUtil.cubeColorData.length * mBytesPerFloat)
        .order(ByteOrder.nativeOrder()).asFloatBuffer();							
		mCubeColors.put(CubeUtil.cubeColorData).position(0);
	}
	
	protected String getVertexShader()
	{
		final String vertexShader =
			"#version 300 es                \n"
		  + "uniform mat4 u_MVPMatrix;      \n"
		  + "in vec4 a_Position;            \n"
		  + "in vec4 a_Color;               \n"
		  + "out vec4 v_Color;              \n"
		  + "void main()                    \n"
		  + "{                              \n"
		  + "   v_Color = a_Color;                                       \n"
		  + "   gl_Position = u_MVPMatrix * a_Position;                            \n"     
		  + "}                                                                     \n"; 
		
		return vertexShader;
	}
	
	protected String getFragmentShader()
	{
		final String fragmentShader =
			"#version 300 es                \n"
		  + "precision mediump float;       \n"
          + "in vec4 v_Color;               \n"
          + "out vec4 FragColor;            \n"
		  + "void main()                    \n"
		  + "{                              \n"
		  + "   FragColor = v_Color;        \n"
		  + "}                              \n";
		
		return fragmentShader;
	}

    protected String getFragmentShader2()
    {
        final String fragmentShader =
                "#version 300 es                \n"
                        + "precision mediump float;       \n"
                        + "out vec4 FragColor;            \n"
                        + "void main()                    \n"
                        + "{                              \n"
                        + "   FragColor = vec4(0.04, 0.28, 0.26, 1.0);        \n"
                        + "}                              \n";

        return fragmentShader;
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
		mPerVertexProgramHandle = ProgramUtil.createAndLinkProgram(getVertexShader(), getFragmentShader(),
				new String[] {"a_Position",  "a_Color"});

		// init stencil program
        mStencilProgramHandle = ProgramUtil.createAndLinkProgram(getVertexShader(), getFragmentShader2(),
                new String[] {"a_Position",  "a_Color"});

        // Set program handles for cube drawing.
        initHandles();
	}
		
	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) 
	{
		// Set the OpenGL viewport to the same size as the surface.
		GLES30.glViewport(0, 0, width, height);

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
	    // 启动模版测试
        GLES30.glEnable(GLES30.GL_STENCIL_TEST);
        /**
         * 默认的行为不会更新模板缓冲,都是 KEEP
         * glStencilOp(GLenum sfail, GLenum dpfail, GLenum dppass)
         * sfail：模板测试失败时采取的行为。
         * dpfail：模板测试通过，但深度测试失败时采取的行为。
         * dppass：模板测试和深度测试都通过时采取的行为。
         */
        GLES30.glStencilOp(GLES30.GL_KEEP, GLES30.GL_KEEP, GLES30.GL_REPLACE);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT | GLES30.GL_STENCIL_BUFFER_BIT);

        // Do a complete rotation every 10 seconds.
        long time = SystemClock.uptimeMillis() % 10000L;        
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);

        // rotate cube.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -6.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        // 保证了箱子的每个片段都会将模板缓冲的模板值更新为1。因为片段永远会通过模板测试，在绘制片段的地方，模板缓冲会被更新为参考值。
        GLES30.glStencilFunc(GLES30.GL_ALWAYS, 1, 0xFF);
        // 设置的值会与 mask 作与(AND)运算
        GLES30.glStencilMask(0xFF);

        // 使用原来的program，正常绘画
        GLES30.glUseProgram(mPerVertexProgramHandle);
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        drawCube();

        // 保证我们只绘制箱子上模板值不为1的部分，即只绘制箱子在之前绘制的箱子之外的部分。
        GLES30.glStencilFunc(GLES30.GL_NOTEQUAL, 1, 0xFF);
        // 禁止模板缓冲的写入，都会是0
        GLES30.glStencilMask(0x00);
        // 禁用了深度测试，让放大的箱子，即边框，不会被地板所覆盖。
        GLES30.glDisable(GLES30.GL_DEPTH_TEST);

        // 稍微放大一下，因为要画在上一次的外面，就显示成一个边框。
        Matrix.scaleM(mModelMatrix, 0,  1.1f, 1.1f, 1.1f);
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        // 使用另一个program，主要是为了画边框
        GLES30.glUseProgram(mStencilProgramHandle);
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        drawCube();

        // 恢复模版测试的与运算
        GLES30.glStencilMask(0xFF);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
	}

	protected void initHandles() {
        // Set program handles for cube drawing.
        mMVPMatrixHandle = GLES30.glGetUniformLocation(mPerVertexProgramHandle, "u_MVPMatrix");
        mPositionHandle = GLES30.glGetAttribLocation(mPerVertexProgramHandle, "a_Position");
        mColorHandle = GLES30.glGetAttribLocation(mPerVertexProgramHandle, "a_Color");
    }
	
	/**
	 * Draws a cube.
	 */
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

        // Draw the cube.
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 36);
	}
}
