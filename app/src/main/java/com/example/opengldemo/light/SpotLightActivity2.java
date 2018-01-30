package com.example.opengldemo.light;

import android.opengl.GLSurfaceView;

import com.example.opengldemo.util.BaseRendererActivity;

public class SpotLightActivity2 extends BaseRendererActivity {

    @Override
    protected GLSurfaceView.Renderer getRenderer() {
        return new SpotLightRenderer2();
    }
}
