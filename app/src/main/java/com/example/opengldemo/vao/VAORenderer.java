package com.example.opengldemo.vao;

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
 * BasicRenderer
 */
public class VAORenderer implements GLSurfaceView.Renderer
{
	private float[] mModelMatrix = new float[16];
	private float[] mViewMatrix = new float[16];
	private float[] mProjectionMatrix = new float[16];
	private float[] mMVPMatrix = new float[16];

	/** Store our model data in a float buffer. */
	private final FloatBuffer mCubePositions;
	private final FloatBuffer mCubeColors;

    protected int mPerVertexProgramHandle;
	private int mMVPMatrixHandle;
	private int mPositionHandle;
	private int mColorHandle;

	private final int mBytesPerFloat = 4;
	private final int mPositionDataSize = 3;
	private final int mColorDataSize = 4;

	protected int mVBO_id;
	protected int mEBO_id;
	protected int mVAO_id;


	public VAORenderer()
	{
		// Initialize the buffers.
		mCubePositions = ByteBuffer.allocateDirect(cubePositionData.length * mBytesPerFloat)
        .order(ByteOrder.nativeOrder()).asFloatBuffer();							
		mCubePositions.put(cubePositionData).position(0);
		
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
				new String[] {"a_Position",  "a_Color", "a_Normal"});

        // Set program handles for cube drawing.
        initHandles();

		mVBO_id = initVBOs();
		mEBO_id = initEBOs();
		mVAO_id = initVAO_VBO_EBO();
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
		GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
                
        // Do a complete rotation every 10 seconds.
        long time = SystemClock.uptimeMillis() % 10000L;        
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);
        
        // Set our per-vertex lighting program.
        GLES30.glUseProgram(mPerVertexProgramHandle);

        // rotate cube.
        mCubeColors.position(0);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -6.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        drawCube();

        // rotate cube.
        mCubeColors.position(4*6);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -6.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, 90, 1.0f, 0.0f, 0.0f);
        drawCube();

        // rotate cube.
        mCubeColors.position(8*6);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -6.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, -90, 1.0f, 0.0f, 0.0f);
        drawCube();

        // rotate cube.
        mCubeColors.position(12*6);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -6.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, 90, 0.0f, 1.0f, 0.0f);
        drawCube();

        // rotate cube.
        mCubeColors.position(16*6);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -6.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, -90, 0.0f, 1.0f, 0.0f);
        drawCube();

        // rotate cube.
        mCubeColors.position(20*6);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -6.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, 180, 0.0f, 1.0f, 0.0f);
        drawCube();
	}

	protected void initHandles() {
        // Set program handles for cube drawing.
        mMVPMatrixHandle = GLES30.glGetUniformLocation(mPerVertexProgramHandle, "u_MVPMatrix");
        mPositionHandle = GLES30.glGetAttribLocation(mPerVertexProgramHandle, "a_Position");
        mColorHandle = GLES30.glGetAttribLocation(mPerVertexProgramHandle, "a_Color");
    }

    protected int initVBOs() {
		int[] vboIds = new int[1];
		GLES30.glGenBuffers ( 1, vboIds , 0);
		GLES30.glBindBuffer (  GLES30.GL_ARRAY_BUFFER, vboIds[0] );
		GLES30.glBufferData (  GLES30.GL_ARRAY_BUFFER, cubePositionData.length * mBytesPerFloat,
				mCubePositions,  GLES30.GL_STATIC_DRAW );
		GLES30.glBindBuffer (  GLES30.GL_ARRAY_BUFFER,0 );
		return vboIds[0];
	}

	protected int initEBOs() {
		int[] eboIds = new int[1];
		GLES30.glGenBuffers ( 1, eboIds , 0);
		GLES30.glBindBuffer ( GLES30.GL_ELEMENT_ARRAY_BUFFER, eboIds[0]);
		GLES30.glBufferData ( GLES30.GL_ELEMENT_ARRAY_BUFFER,
				indices.limit() * 1,
				indices, GLES30.GL_STATIC_DRAW );
		GLES30.glBindBuffer (  GLES30.GL_ELEMENT_ARRAY_BUFFER,0 );
		return eboIds[0];
	}

	protected int initVAO_VBO_EBO() {
		// Generate VBO Ids and load the VBOs with data
		int[] vboIds = new int[3];

		// 注意这里有3个，所以第一个参数要写3
		GLES30.glGenBuffers ( 3, vboIds , 0);
		GLES30.glBindBuffer (  GLES30.GL_ARRAY_BUFFER, vboIds[0] );
		mCubePositions.position ( 0 );
		GLES30.glBufferData (  GLES30.GL_ARRAY_BUFFER, cubePositionData.length * 4,
				mCubePositions,  GLES30.GL_STATIC_DRAW );

		GLES30.glBindBuffer (  GLES30.GL_ARRAY_BUFFER, vboIds[1] );
		colors.position ( 0 );
		GLES30.glBufferData (  GLES30.GL_ARRAY_BUFFER, colors.limit() * mBytesPerFloat,
				colors,  GLES30.GL_STATIC_DRAW );

		GLES30.glBindBuffer ( GLES30.GL_ELEMENT_ARRAY_BUFFER, vboIds[2]);
		indices.position ( 0 );
		GLES30.glBufferData ( GLES30.GL_ELEMENT_ARRAY_BUFFER, 1 * 6,
				indices, GLES30.GL_STATIC_DRAW );

		// 创建 VAO
		int[] vaoIds = new int[1];
		GLES30.glGenVertexArrays(1, vaoIds,0);
		GLES30.glBindVertexArray(vaoIds[0]);

		// 绑定传数据时要按顺序。
		GLES30.glBindBuffer ( GLES30.GL_ARRAY_BUFFER, vboIds[0] );
		GLES30.glEnableVertexAttribArray(mPositionHandle);
		GLES30.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES30.GL_FLOAT, false,
				0, 0);

		GLES30.glBindBuffer ( GLES30.GL_ARRAY_BUFFER, vboIds[1] );
		GLES30.glEnableVertexAttribArray(mColorHandle);
		GLES30.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES30.GL_FLOAT, false,
				0, 0);
		GLES30.glBindBuffer ( GLES30.GL_ELEMENT_ARRAY_BUFFER, vboIds[2] );

		// Reset to the default VAO
		GLES30.glBindVertexArray ( 0 );
		// 这里可以解绑，不过应该在VAO外面
		GLES30.glBindBuffer ( GLES30.GL_ARRAY_BUFFER, 0 );
		GLES30.glBindBuffer ( GLES30.GL_ELEMENT_ARRAY_BUFFER, 0 );

		return vaoIds[0];
	}

	/**
	 * Draws a cube.
	 */
    protected void drawCube()
	{
		// 绑定GL_ARRAY_BUFFER ， 并传递数据，glVertexAttribPointer中传递的是0不是buffer
//		GLES30.glBindBuffer ( GLES30.GL_ARRAY_BUFFER, mVBO_id );
//		GLES30.glEnableVertexAttribArray(mPositionHandle);
//		GLES30.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES30.GL_FLOAT, false,
//       		0, 0);
//		GLES30.glBindBuffer ( GLES30.GL_ARRAY_BUFFER, 0 );

        
        // Pass in the color information
//        GLES30.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES30.GL_FLOAT, false,
//        		0, mCubeColors);
//        GLES30.glEnableVertexAttribArray(mColorHandle);
        
        // Calculate MVP matrix
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

		//GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, cubePositionData.length / mPositionDataSize);

		// 使用VBO时，要先绑定再使用，使用完之后最好解绑
//		GLES30.glBindBuffer ( GLES30.GL_ELEMENT_ARRAY_BUFFER, mEBO_id );
//		GLES30.glDrawElements(GLES30.GL_TRIANGLE_FAN, 6, GLES30.GL_UNSIGNED_BYTE, 0);
//		GLES30.glBindBuffer ( GLES30.GL_ELEMENT_ARRAY_BUFFER, 0 );

		// 使用VAO绘画
		GLES30.glBindVertexArray(mVAO_id);
		GLES30.glDrawElements(GLES30.GL_TRIANGLE_FAN, 6, GLES30.GL_UNSIGNED_BYTE, 0);
		GLES30.glBindVertexArray(0);
	}

    // X, Y, Z
    public static final float[] cubePositionData =
            {
                    0.0f, 0.0f, 1.0f,
                    -1.0f, -1.0f, 1.0f,
                    1.0f, -1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f,
                    -1.0f, 1.0f, 1.0f,
                    -1.0f, -1.0f, 1.0f,
            };

	FloatBuffer colors = FloatBuffer.wrap(new float[]
			{
					// Front face (red)
					1.0f, 0.0f, 1.0f, 1.0f,
					1.0f, 0.0f, 0.0f, 1.0f,
					1.0f, 0.0f, 0.0f, 1.0f,
					1.0f, 0.0f, 0.0f, 1.0f,
					1.0f, 0.0f, 0.0f, 1.0f,
					1.0f, 0.0f, 0.0f, 1.0f,
			});

	ByteBuffer indices = ByteBuffer.wrap(new byte[]{
		0,1,2,3,4,5,
	});
}
