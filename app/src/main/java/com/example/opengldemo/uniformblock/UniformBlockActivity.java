package com.example.opengldemo.uniformblock;

import android.opengl.GLSurfaceView;
import com.example.opengldemo.base.BaseRendererActivity;

public class UniformBlockActivity extends BaseRendererActivity {

    @Override
    protected GLSurfaceView.Renderer getRenderer() {
        return new UniformBlockRenderer();
    }
}
