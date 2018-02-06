package com.example.opengldemo.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import java.io.IOException;

/**
 * Created by zhooker on 2018/2/6.
 */

public class CameraManager {

    private static final int RETRY_COUNT = 3;

    protected Camera mCamera;

    public boolean openCamera(int camera){
        try {
            mCamera = Camera.open(camera);
        }catch (Exception e){
            return false;
        }
        return true;
    }

    public void setDisplayOrientation(int orientation) {
        if (mCamera != null) {
            mCamera.setDisplayOrientation(orientation);
        }
    }

    public void setPreviewTexture(SurfaceTexture surfaceTexture) throws IOException {
        if (mCamera != null) {
            mCamera.setPreviewTexture(surfaceTexture);
        }
    }

    public Camera.Parameters getParameters () {
        if (mCamera != null)
            return mCamera.getParameters();
        return null;
    }

    public void setParameters(Camera.Parameters parameters) {
        if (mCamera != null)
            mCamera.setParameters(parameters);
    }

    public void startPreview() {
        if (mCamera != null)
            mCamera.startPreview();
    }

    public void stopPreview() {
        if (mCamera != null)
            mCamera.stopPreview();
    }

    public void closeCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
}
