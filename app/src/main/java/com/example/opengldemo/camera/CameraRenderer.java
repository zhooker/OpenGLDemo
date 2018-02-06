package com.example.opengldemo.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.example.opengldemo.R;
import com.example.opengldemo.util.CubeUtil;
import com.example.opengldemo.util.ProgramUtil;
import com.example.opengldemo.util.TextureHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glGenerateMipmap;
import static android.opengl.GLUtils.texImage2D;

/**
 * BasicRenderer
 */
public class CameraRenderer extends BaseCameraRenderer
{

    private float[] mModelMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

	/** Store our model data in a float buffer. */
	private final FloatBuffer mCubePositions;

    protected int mProgramHandle;
	private int mPositionHandle;
    private int mMVPMatrixHandle;
	private int aTextureCoordLocation;
    private int uTextureMatrixLocation;
    private int uTextureSamplerLocation;

    private int mOESTextureId;

	private final int mBytesPerFloat = 4;
	private final int mPositionDataSize = 2;


	public CameraRenderer(GLSurfaceView glSurfaceView)
	{
        super(glSurfaceView);
        // Initialize the buffers.
		mCubePositions = ByteBuffer.allocateDirect(cubePositionData.length * mBytesPerFloat)
        .order(ByteOrder.nativeOrder()).asFloatBuffer();							
		mCubePositions.put(cubePositionData).position(0);
	}
	
	protected String getVertexShader()
	{
		final String vertexShader =
			"uniform mat4 uTextureMatrix;   \n"
          + "uniform mat4 u_MVPMatrix;      \n"
		  + "attribute vec4 a_Position;            \n"
		  + "attribute vec4 aTextureCoordinate;    \n"
		  + "varying vec2 vTextureCoord;        \n"
		  + "void main()                    \n"
		  + "{                              \n"
		  + "   vTextureCoord = (uTextureMatrix * aTextureCoordinate).xy;       \n"
		  + "   gl_Position = a_Position;              \n"
		  + "}                                                                  \n";
		
		return vertexShader;
	}
	
	protected String getFragmentShader()
	{
		final String fragmentShader =
           "#extension GL_OES_EGL_image_external : require \n"
		  + "precision mediump float;       \n"
          + "varying vec2 vTextureCoord;               \n"
          + "uniform samplerExternalOES uTextureSampler;            \n"
          + "//out vec4 FragColor;            \n"
		  + "void main()                    \n"
		  + "{                              \n"
		  + "   //FragColor = vec4(1.0f,1.0f,0.0f,1.0f);\n"
          + "   gl_FragColor = texture2D(uTextureSampler,vTextureCoord);        \n"
		  + "}                              \n";
		
		return fragmentShader;
	}

    @Override
    protected int getTextureID() {
        return mOESTextureId;
    }

    @Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config)
	{
        // init texture
        mOESTextureId = createOESTextureObject();
	    super.onSurfaceCreated(glUnused,config);
		// Set the background clear color to black.
		//GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		
		// Use culling to remove back faces.
		//GLES30.glEnable(GLES30.GL_CULL_FACE);

		// Enable depth testing
		//GLES30.glEnable(GLES30.GL_DEPTH_TEST);

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
		mProgramHandle = ProgramUtil.createAndLinkProgram(getVertexShader(), getFragmentShader(),
				new String[] {"a_Position",  "aTextureCoordinate"});

        // Set program handles for cube drawing.
        mPositionHandle = GLES30.glGetAttribLocation(mProgramHandle, "a_Position");
        aTextureCoordLocation = GLES30.glGetAttribLocation(mProgramHandle, "aTextureCoordinate");
        mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
        uTextureMatrixLocation = GLES30.glGetUniformLocation(mProgramHandle, "uTextureMatrix");
        uTextureSamplerLocation = GLES30.glGetUniformLocation(mProgramHandle, "uTextureSampler");

    }
		
	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) 
	{
	    super.onSurfaceChanged(glUnused,width,height);
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
	    super.onDrawFrame(glUnused);
		GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        // Set our per-vertex lighting program.
        GLES30.glUseProgram(mProgramHandle);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mOESTextureId);
        GLES30.glUniform1i(uTextureSamplerLocation, 0);

        //将纹理矩阵传给片段着色器
        GLES30.glUniformMatrix4fv(uTextureMatrixLocation, 1, false, transformMatrix, 0);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        drawCube();
	}
	
	/**
	 * Draws a cube.
	 */
    protected void drawCube()
	{		
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

    public int createOESTextureObject() {
        int[] tex = new int[1];
        //生成一个纹理
        GLES30.glGenTextures(1, tex, 0);
        //将此纹理绑定到外部纹理上
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0]);
        //设置纹理过滤参数
        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        // Read in the resource
//        final Bitmap bitmap = BitmapFactory.decodeResource(
//                glSurfaceView.getContext().getResources(), R.drawable.wall, options);
//        texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
//        glGenerateMipmap(GL_TEXTURE_2D);
//        bitmap.recycle();

        //解除纹理绑定
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        return tex[0];
    }

    public int createTextureObject() {
        int[] tex = new int[1];
        //生成一个纹理
        GLES30.glGenTextures(1, tex, 0);
        //将此纹理绑定到外部纹理上
        GLES30.glBindTexture(GL_TEXTURE_2D, tex[0]);
        //设置纹理过滤参数
        GLES30.glTexParameterf(GL_TEXTURE_2D,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLES30.glTexParameterf(GL_TEXTURE_2D,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES30.glTexParameterf(GL_TEXTURE_2D,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameterf(GL_TEXTURE_2D,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        // Read in the resource
//        final Bitmap bitmap = BitmapFactory.decodeResource(
//                glSurfaceView.getContext().getResources(), R.drawable.wall, options);
//        texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
//        glGenerateMipmap(GL_TEXTURE_2D);
//        bitmap.recycle();

        //解除纹理绑定
        GLES30.glBindTexture(GL_TEXTURE_2D, 0);
        return tex[0];
    }
}
