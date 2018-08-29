package com.example.opengldemo.test.stencil;

import android.opengl.GLSurfaceView;

import com.example.opengldemo.base.BaseRendererActivity;

public class StencilActivity extends BaseRendererActivity {

    @Override
    protected GLSurfaceView.Renderer getRenderer() {
        return new StencilRenderer();
    }
}
