package com.example.opengldemo.filter;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Size;

import com.example.opengldemo.util.L;
import com.example.opengldemo.util.TextureHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * BaseCameraRenderer
 */
public abstract class BaseCameraRenderer implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener
{

    protected CameraManager mCameraManager;
    protected SurfaceTexture surfaceTexture;
    protected GLSurfaceView glSurfaceView;

    protected int mOESTextureId = -1;
    protected int mFBOTextureId = -1;
    protected int mFrameBufferID = -1;
    protected float[] transformMatrix = new float[16];

    protected Size size;

    public BaseCameraRenderer(GLSurfaceView glSurfaceView) {
        this.glSurfaceView = glSurfaceView;
        this.mCameraManager = new CameraManager();
    }


    @Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config) 
	{
        L.d("onSurfaceCreated");
        mOESTextureId = TextureHelper.loadOESTexture(null);
        surfaceTexture = new SurfaceTexture(mOESTextureId);
        surfaceTexture.setOnFrameAvailableListener(this);

        if (mCameraManager.openCamera(Camera.CameraInfo.CAMERA_FACING_FRONT)) {
            mCameraManager.setDisplayOrientation(90);
            try {
                mCameraManager.setPreviewTexture(surfaceTexture);
                Camera.Parameters parameters = mCameraManager.getParameters();
                if (parameters != null) {
                    initCameraInfo(parameters);
                    mCameraManager.setParameters(parameters);
                }
                mCameraManager.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	}

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height)
    {
        L.d("onSurfaceChanged");
        if (size == null || width != size.getWidth() || height != size.getHeight()) {
            size = new Size(width, height);
            initFrameBuffer(size.getWidth(), size.getHeight());
        }
    }

    @Override
	public void onDrawFrame(GL10 glUnused) 
	{
        if (surfaceTexture != null) {
            //更新纹理图像
            surfaceTexture.updateTexImage();
            //获取外部纹理的矩阵，用来确定纹理的采样位置，没有此矩阵可能导致图像翻转等问题
            surfaceTexture.getTransformMatrix(transformMatrix);
        }
    }

    protected void initCameraInfo(Camera.Parameters parameters){
        parameters.setPreviewSize(1920, 1080);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        glSurfaceView.requestRender();
    }

    public void closeRenderer() {
        mCameraManager.closeCamera();
        if (surfaceTexture != null) {
            surfaceTexture.release();
        }
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
        mFBOTextureId = mFrameBufferTextures[0];
    }
}
