package com.example.opengldemo.camera;

import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;

import com.example.opengldemo.util.L;
import com.example.opengldemo.util.TextureHelper;

import java.io.IOException;

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

    protected int mOESTextureId;
    protected float[] transformMatrix = new float[16];

    public BaseCameraRenderer(GLSurfaceView glSurfaceView) {
        this.glSurfaceView = glSurfaceView;
        this.mCameraManager = new CameraManager();
    }

    @Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config) 
	{
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
}
