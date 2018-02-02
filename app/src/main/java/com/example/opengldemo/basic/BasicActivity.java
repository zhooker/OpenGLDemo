package com.example.opengldemo.basic;

import android.opengl.GLSurfaceView;

import com.example.opengldemo.util.BaseRendererActivity;

public class BasicActivity extends BaseRendererActivity {

    @Override
    protected GLSurfaceView.Renderer getRenderer() {
        return new BasicRenderer();
    }
}
