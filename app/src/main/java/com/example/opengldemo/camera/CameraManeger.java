package com.example.opengldemo.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import java.io.IOException;

import static android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;

/**
 * Created by zhuangsj on 18-2-6.
 */

public class CameraManeger {
    private Camera mCamera;

    public void OpenCamera(SurfaceTexture surfaceTexture) {
        try {
            mCamera = Camera.open(CAMERA_FACING_FRONT);
            mCamera.setPreviewTexture(surfaceTexture);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
