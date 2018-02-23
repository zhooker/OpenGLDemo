package com.example.opengldemo.filter;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;

import com.example.opengldemo.util.BaseRendererActivity;

public class CameraFilterActivity extends BaseRendererActivity implements View.OnClickListener {

    protected CameraFilterRenderer mCameraFilterRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLSurfaceView.setOnClickListener(this);
    }

    @Override
    protected GLSurfaceView.Renderer getRenderer() {
         if (mCameraFilterRenderer == null)
             mCameraFilterRenderer = new CameraFilterRenderer(mGLSurfaceView);
        return mCameraFilterRenderer;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraFilterRenderer != null)
            mCameraFilterRenderer.closeRenderer();
    }

    @Override
    public void onClick(View v) {
        mCameraFilterRenderer.changeNextFilter();
    }
}
