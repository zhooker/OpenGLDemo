package com.example.opengldemo.camera;

import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;

import com.example.opengldemo.util.L;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * BaseCameraRenderer
 */
public class BaseCameraRenderer implements GLSurfaceView.Renderer
{

    protected Camera mCamera;
    protected SurfaceTexture surfaceTexture;
    protected GLSurfaceView glSurfaceView;
    protected float[] transformMatrix = new float[16];

    public BaseCameraRenderer(GLSurfaceView glSurfaceView) {
        this.glSurfaceView = glSurfaceView;
    }

    @Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config) 
	{
        surfaceTexture = new SurfaceTexture(getTextureID());

        openCamera();
	}

    protected int getTextureID() {
        return 0;
    }
		
	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) 
	{
        surfaceTexture.setOnFrameAvailableListener(onFrameAvailableListener);
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

    private void openCamera(){
        try {
            mCamera = getCameraInstance();
            mCamera.setPreviewTexture(surfaceTexture);
            mCamera.startPreview();
        }catch (IOException e){

        }
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(1);
            Camera.Parameters parameters = c.getParameters();
            parameters.set("orientation", "portrait");
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            parameters.setPictureFormat(PixelFormat.JPEG);
            parameters.setPreviewSize(1280, 720);
            c.setDisplayOrientation(90);
            c.setParameters(parameters);
        } catch (Exception e){
            L.d("open camera error : " + e.getMessage());
        }
        return c;
    }

    private SurfaceTexture.OnFrameAvailableListener onFrameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {

        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            glSurfaceView.requestRender();
        }
    };
}
