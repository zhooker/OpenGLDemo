package com.example.opengldemo.light;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES30;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import com.example.opengldemo.util.CubeUtil;
import com.example.opengldemo.util.ProgramUtil;

/**
 * This class implements our custom renderer. Note that the GL10 parameter passed in is unused for OpenGL ES 2.0
 * renderers -- the static class GLES30 is used instead.
 */
public class SpotLightRenderer implements GLSurfaceView.Renderer
{
	/** Used for debug logs. */
	private static final String TAG = "TextureRenderer";
	
	/**
	 * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
	 * of being located at the center of the universe) to world space.
	 */
	protected float[] mModelMatrix = new float[16];

	/**
	 * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
	 * it positions things relative to our eye.
	 */
	protected float[] mViewMatrix = new float[16];

	/** Store the projection matrix. This is used to project the scene onto a 2D viewport. */
	protected float[] mProjectionMatrix = new float[16];
	
	/** Allocate storage for the final combined matrix. This will be passed into the shader program. */
	protected float[] mMVPMatrix = new float[16];
	
	/** 
	 * Stores a copy of the model matrix specifically for the light position.
	 */
	protected float[] mLightModelMatrix = new float[16];
	
	/** Store our model data in a float buffer. */
	protected final FloatBuffer mCubePositions;
	protected final FloatBuffer mCubeColors;
	protected final FloatBuffer mCubeNormals;
	
	/** This will be used to pass in the transformation matrix. */
	protected int mMVPMatrixHandle;
	
	/** This will be used to pass in the modelview matrix. */
	protected int mMVMatrixHandle;
	
	/** This will be used to pass in the light position. */
	protected int mLightPosHandle;
	
	/** This will be used to pass in model position information. */
	protected int mPositionHandle;
	
	/** This will be used to pass in model color information. */
	protected int mColorHandle;
	
	/** This will be used to pass in model normal information. */
	protected int mNormalHandle;

	/** How many bytes per float. */
	protected final int mBytesPerFloat = 4;
	
	/** Size of the position data in elements. */
	protected final int mPositionDataSize = 3;
	
	/** Size of the color data in elements. */
	protected final int mColorDataSize = 4;
	
	/** Size of the normal data in elements. */
	protected final int mNormalDataSize = 3;
	
	/** Used to hold a light centered on the origin in model space. We need a 4th coordinate so we can get translations to work when
	 *  we multiply this by our transformation matrices. */
	protected final float[] mLightPosInModelSpace = new float[] {0.0f, 0.0f, 0.0f, 1.0f};
	
	/** Used to hold the current position of the light in world space (after transformation via model matrix). */
	protected final float[] mLightPosInWorldSpace = new float[4];
	
	/** Used to hold the transformed position of the light in eye space (after transformation via modelview matrix) */
	protected final float[] mLightPosInEyeSpace = new float[4];
	
	/** This is a handle to our per-vertex cube shading program. */
	protected int mPerVertexProgramHandle;
		
	/** This is a handle to our light point program. */
    protected int mPointProgramHandle;
						
	/**
	 * Initialize the model data.
	 */
	public SpotLightRenderer()
	{
		// Initialize the buffers.
		mCubePositions = ByteBuffer.allocateDirect(CubeUtil.cubePositionData.length * mBytesPerFloat)
        .order(ByteOrder.nativeOrder()).asFloatBuffer();							
		mCubePositions.put(CubeUtil.cubePositionData).position(0);
		
		mCubeColors = ByteBuffer.allocateDirect(CubeUtil.cubeColorData.length * mBytesPerFloat)
        .order(ByteOrder.nativeOrder()).asFloatBuffer();							
		mCubeColors.put(CubeUtil.cubeColorData).position(0);
		
		mCubeNormals = ByteBuffer.allocateDirect(CubeUtil.cubeNormalData.length * mBytesPerFloat)
        .order(ByteOrder.nativeOrder()).asFloatBuffer();							
		mCubeNormals.put(CubeUtil.cubeNormalData).position(0);
	}
	
	protected String getVertexShader()
	{
		// TODO: Explain why we normalize the vectors, explain some of the vector math behind it all. Explain what is eye space.
		final String vertexShader =
			"#version 300 es                \n"
		  + "uniform mat4 u_MVPMatrix;      \n"
          + "uniform mat4 u_MVMatrix;       \n"
		  + "uniform vec3 u_LightPos;       \n"
		  + "in vec4 a_Position;            \n"
		  + "in vec4 a_Color;               \n"
		  + "in vec3 a_Normal;              \n"
		  + "out vec4 v_Color;              \n"
		  + "void main()                    \n"
		  + "{                              \n"		
		// Transform the vertex into eye space.
		  + "   vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);              \n"
		// Transform the normal's orientation into eye space.
		  + "   vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));     \n"
		// Will be used for attenuation.
		  + "   float distance = length(u_LightPos - modelViewVertex);             \n"
		// Get a lighting direction vector from the light to the vertex.
		  + "   vec3 lightVector = normalize(u_LightPos - modelViewVertex);        \n"
		// Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
		// pointing in the same direction then it will get max illumination.
		  + "   float diffuse = max(dot(modelViewNormal, lightVector), 0.1);       \n" 	  		  													  
		// Attenuate the light based on distance.
		  + "   diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance * distance)));  \n"
		// Multiply the color by the illumination level. It will be interpolated across the triangle.
		  + "   v_Color = a_Color * diffuse;                                       \n" 	 
		// gl_Position is a special variable used to store the final position.
		// Multiply the vertex by the matrix to get the final point in normalized screen coordinates.		
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

		// Set the view matrix. This matrix can be said to represent the camera position.
		// NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
		// view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
		Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);		


		mPerVertexProgramHandle = ProgramUtil.createAndLinkProgram(getVertexShader(), getFragmentShader(),
				new String[] {"a_Position",  "a_Color", "a_Normal"});								                                							       
        
        // Define a simple shader program for our point.
        final String pointVertexShader =
        	"#version 300 es                \n"
          +	"uniform mat4 u_MVPMatrix;      \n"
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
        		new String[] {"a_Position"});                 
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

        // Set program handles for cube drawing.
        initHandles();
        
        // Calculate position of the light. Rotate and then push into the distance.
        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, -5.0f);      
        Matrix.rotateM(mLightModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, 2.0f);
               
        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0);                        
        
        // Draw some cubes.        
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 4.0f, 0.0f, -7.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 0.0f, 0.0f);
        drawCube();

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, -4.0f, 0.0f, -7.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        drawCube();

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 4.0f, -7.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);
        drawCube();

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, -4.0f, -7.0f);
        drawCube();
        
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -5.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1.0f, 1.0f, 0.0f);        
        drawCube();      
        
        // Draw a point to indicate the light.
        GLES30.glUseProgram(mPointProgramHandle);
        drawLight();
	}

	protected void initHandles() {
        // Set program handles for cube drawing.
        mMVPMatrixHandle = GLES30.glGetUniformLocation(mPerVertexProgramHandle, "u_MVPMatrix");
        mMVMatrixHandle = GLES30.glGetUniformLocation(mPerVertexProgramHandle, "u_MVMatrix");
        mLightPosHandle = GLES30.glGetUniformLocation(mPerVertexProgramHandle, "u_LightPos");
        mPositionHandle = GLES30.glGetAttribLocation(mPerVertexProgramHandle, "a_Position");
        mColorHandle = GLES30.glGetAttribLocation(mPerVertexProgramHandle, "a_Color");
        mNormalHandle = GLES30.glGetAttribLocation(mPerVertexProgramHandle, "a_Normal");
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
        
        // Pass in the normal information
        mCubeNormals.position(0);
        GLES30.glVertexAttribPointer(mNormalHandle, mNormalDataSize, GLES30.GL_FLOAT, false,
        		0, mCubeNormals);
        
        GLES30.glEnableVertexAttribArray(mNormalHandle);
        
		// This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);   
        
        // Pass in the modelview matrix.
        GLES30.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);
        
        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        // Pass in the combined matrix.
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        
        // Pass in the light position in eye space.        
        GLES30.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);
        
        // Draw the cube.
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 36);
	}	
	
	/**
	 * Draws a point representing the position of the light.
	 */
	protected void drawLight()
	{
		final int pointMVPMatrixHandle = GLES30.glGetUniformLocation(mPointProgramHandle, "u_MVPMatrix");
        final int pointPositionHandle = GLES30.glGetAttribLocation(mPointProgramHandle, "a_Position");
        
		// Pass in the position.
		GLES30.glVertexAttrib3f(pointPositionHandle, mLightPosInModelSpace[0], mLightPosInModelSpace[1], mLightPosInModelSpace[2]);

		// Since we are not using a buffer object, disable vertex arrays for this attribute.
        GLES30.glDisableVertexAttribArray(pointPositionHandle);
		
		// Pass in the transformation matrix.
		Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mLightModelMatrix, 0);
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
		GLES30.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, mMVPMatrix, 0);
		
		// Draw the point.
		GLES30.glDrawArrays(GLES30.GL_POINTS, 0, 1);
	}
}
