package com.example.opengldemo.camera;

import android.opengl.GLSurfaceView;

import com.example.opengldemo.util.BaseRendererActivity;

public class CameraActivity extends BaseRendererActivity {

    protected  CameraRenderer mCameraRenderer;

    @Override
    protected GLSurfaceView.Renderer getRenderer() {
         if (mCameraRenderer == null)
             mCameraRenderer = new CameraRenderer(mGLSurfaceView);
        return mCameraRenderer;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraRenderer != null)
            mCameraRenderer.closeRenderer();
    }
}
