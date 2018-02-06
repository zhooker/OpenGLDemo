package com.example.opengldemo.camera;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.example.opengldemo.basic.BasicRenderer;
import com.example.opengldemo.util.BaseRendererActivity;

public class CameraActivity extends BaseRendererActivity {


    private CameraView mCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mCameraView = new CameraView(this);
//        setContentView ( mCameraView );
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mCameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mCameraView.onPause();
    }

    @Override
    protected GLSurfaceView.Renderer getRenderer() {
        return new CameraRenderer(mGLSurfaceView);
    }
}
