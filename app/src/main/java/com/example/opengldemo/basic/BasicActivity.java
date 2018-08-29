package com.example.opengldemo.basic;

import android.opengl.GLSurfaceView;

import com.example.opengldemo.base.BaseRenderer;
import com.example.opengldemo.base.BaseRendererActivity;

public class BasicActivity extends BaseRendererActivity {

    @Override
    protected GLSurfaceView.Renderer getRenderer() {
        return new BasicRenderer(this);
    }
}
