package com.example.opengldemo.vao;

import android.opengl.GLSurfaceView;

import com.example.opengldemo.base.BaseRendererActivity;

public class VAOActivity extends BaseRendererActivity {

    @Override
    protected GLSurfaceView.Renderer getRenderer() {
        return new VAORenderer();
    }
}
