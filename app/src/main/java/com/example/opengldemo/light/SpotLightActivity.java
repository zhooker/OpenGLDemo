package com.example.opengldemo.light;

import android.opengl.GLSurfaceView;

import com.example.opengldemo.util.BaseRendererActivity;

public class SpotLightActivity extends BaseRendererActivity {

    @Override
    protected GLSurfaceView.Renderer getRenderer() {
        return new SpotLightRenderer();
    }
}
