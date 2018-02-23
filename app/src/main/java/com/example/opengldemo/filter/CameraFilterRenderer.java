package com.example.opengldemo.filter;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.opengldemo.util.ProgramUtil;
import com.example.opengldemo.util.TextureHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_TEXTURE_2D;

/**
 * CameraFilterRenderer 2018/2/23
 * 第一次直接用预览的纹理与滤镜的纹理混合，效果不好，预览有乱点;
 * 第二次把预览的数据渲染到FBO，再绘画出来，效果比较好。
 */
public class CameraFilterRenderer extends BaseCameraRenderer
{
    private final int mBytesPerFloat = 4;
    private float[] mModelMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

	/** Store our model data in a float buffer. */
	private final FloatBuffer mCubePositions;

    protected int mProgramHandle;
    protected int mCameraProgramHandle;

    protected int currFilterIndex = 0;
    protected int[] mFilterTextureId = new int[FILTER_LIST.length];
    protected static final String[] FILTER_LIST = {
            "filter_image/vivid.png",
            "filter_image/vivid_warm.png",
            "filter_image/vivid_cool.png",
            "filter_image/mono.png",
            "filter_image/noir.png",
            "filter_image/dramatic.png",
            "filter_image/dramatic_warm.png",
            "filter_image/dramatic_cool.png"
    };

	public CameraFilterRenderer(GLSurfaceView glSurfaceView)
	{
        super(glSurfaceView);
        // Initialize the buffers.
		mCubePositions = ByteBuffer.allocateDirect(cubePositionData.length * mBytesPerFloat)
        .order(ByteOrder.nativeOrder()).asFloatBuffer();							
		mCubePositions.put(cubePositionData).position(0);
	}

	public void changeNextFilter() {
        currFilterIndex = (currFilterIndex + 1) % FILTER_LIST.length;
    }

    @Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config)
	{
	    super.onSurfaceCreated(glUnused,config);

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
        mCameraProgramHandle = ProgramUtil.createAndLinkProgram(glSurfaceView.getContext(), "filter/vertex_texture.glsl", "filter/fragment_simple.glsl",
                new String[] {"a_Position",  "aTextureCoordinate"});
		mProgramHandle = ProgramUtil.createAndLinkProgram(glSurfaceView.getContext(), "filter/vertex_texture.glsl", "filter/fragment_texture.glsl",
				new String[] {"a_Position",  "aTextureCoordinate"});

        for (int i = 0; i < FILTER_LIST.length; i++) {
            mFilterTextureId[i] = TextureHelper.loadTexture(glSurfaceView.getContext(), FILTER_LIST[i]);
        }
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

        GLES30.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferID);
        GLES30.glUseProgram(mCameraProgramHandle);
        GLES30.glViewport(0, 0, size.getWidth(), size.getHeight());
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        int textureParamHandleCameraFBO = GLES30.glGetUniformLocation(mCameraProgramHandle, "uTextureSampler");
        int textureCoordinateHandleCameraFBO = GLES30.glGetAttribLocation(mCameraProgramHandle, "aTextureCoordinate");
        int positionHandleCameraFBO = GLES30.glGetAttribLocation(mCameraProgramHandle, "a_Position");
        int textureTranformHandleCameraFBO = GLES30.glGetUniformLocation(mCameraProgramHandle, "uTextureMatrix");

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mOESTextureId);
        GLES30.glUniform1i(textureParamHandleCameraFBO, 0);

        //将纹理矩阵传给片段着色器
        GLES30.glUniformMatrix4fv(textureTranformHandleCameraFBO, 1, false, transformMatrix, 0);

        // Pass in the position information
        mCubePositions.position(0);
        GLES30.glVertexAttribPointer(positionHandleCameraFBO, 3, GLES30.GL_FLOAT, false,
                5*mBytesPerFloat, mCubePositions);
        GLES30.glEnableVertexAttribArray(positionHandleCameraFBO);

        mCubePositions.position(3);
        GLES30.glVertexAttribPointer(textureCoordinateHandleCameraFBO, 2, GLES30.GL_FLOAT, false,
                5*mBytesPerFloat, mCubePositions);
        GLES30.glEnableVertexAttribArray(textureCoordinateHandleCameraFBO);

        // Draw the cube.
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6);

        // Draw the real scene
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES30.glUseProgram(mProgramHandle);
        GLES30.glViewport(0, 0, size.getWidth(), size.getHeight());
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        int textureParamHandleCamera = GLES30.glGetUniformLocation(mProgramHandle, "uTextureSampler");
        int textureParamHandleCamera0 = GLES30.glGetUniformLocation(mProgramHandle, "uTextureSampler0");
        int textureCoordinateHandleCamera = GLES30.glGetAttribLocation(mProgramHandle, "aTextureCoordinate");
        int positionHandleCamera = GLES30.glGetAttribLocation(mProgramHandle, "a_Position");
        int textureTranformHandleCamera = GLES30.glGetUniformLocation(mProgramHandle, "uTextureMatrix");

        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(GL_TEXTURE_2D, mFBOTextureId);
        GLES30.glUniform1i(textureParamHandleCamera, 1);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
        GLES30.glBindTexture(GL_TEXTURE_2D, mFilterTextureId[currFilterIndex]);
        GLES30.glUniform1i(textureParamHandleCamera0, 2);

        Matrix.setIdentityM(transformMatrix,0);
        GLES30.glUniformMatrix4fv(textureTranformHandleCamera, 1, false, transformMatrix, 0);

        // Pass in the position information
        mCubePositions.position(0);
        GLES30.glVertexAttribPointer(positionHandleCamera, 3, GLES30.GL_FLOAT, false,
                5*mBytesPerFloat, mCubePositions);
        GLES30.glEnableVertexAttribArray(positionHandleCamera);

        mCubePositions.position(3);
        GLES30.glVertexAttribPointer(textureCoordinateHandleCamera, 2, GLES30.GL_FLOAT, false,
                5*mBytesPerFloat, mCubePositions);
        GLES30.glEnableVertexAttribArray(textureCoordinateHandleCamera);

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
