package com.example.opengldemo.solar.system;

import android.opengl.GLSurfaceView;

import com.example.opengldemo.base.BaseRendererActivity;

public class SolarActivity extends BaseRendererActivity {

    @Override
    protected GLSurfaceView.Renderer getRenderer() {
        return new SolarRenderer(this);
    }
}
