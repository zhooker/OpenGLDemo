package com.example.opengldemo.texture;

import android.opengl.GLSurfaceView;

import com.example.opengldemo.util.BaseRendererActivity;

public class TextureActivity extends BaseRendererActivity {

    @Override
    protected GLSurfaceView.Renderer getRenderer() {
        return new TextureRenderer(this);
    }
}
