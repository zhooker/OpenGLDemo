package com.example.opengldemo.blend;

import android.opengl.GLSurfaceView;

import com.example.opengldemo.texture.TextureRenderer;
import com.example.opengldemo.util.BaseRendererActivity;

public class BlendActivity extends BaseRendererActivity {

    @Override
    protected GLSurfaceView.Renderer getRenderer() {
        return new BlendRenderer(this);
    }
}
