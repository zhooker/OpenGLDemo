package com.example.opengldemo.light;

import android.opengl.GLSurfaceView;

import com.example.opengldemo.base.BaseRendererActivity;

public class SpotLightActivity2 extends BaseRendererActivity {

    @Override
    protected GLSurfaceView.Renderer getRenderer() {
        return new LightRenderer2(this);
    }
}
