package com.example.opengldemo.light;

import android.opengl.GLSurfaceView;

import com.example.opengldemo.base.BaseRendererActivity;

public class SpotLightActivity extends BaseRendererActivity {

    @Override
    protected GLSurfaceView.Renderer getRenderer() {
        return new LightRenderer(this);
    }
}
