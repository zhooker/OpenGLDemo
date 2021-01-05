package com.example.opengldemo.decoder.gl2;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class VideoPlayView extends GLSurfaceView {

    private Nv21Renderer cameraNv21Renderer = null;

    public VideoPlayView(Context ctx) {
        super(ctx);
        init();
    }

    public VideoPlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        this.setFocusableInTouchMode(true);
        this.setEGLContextClientVersion(2);

        this.setEGLConfigChooser(8, 8, 8, 8, 0, 0);
        this.setDebugFlags(DEBUG_CHECK_GL_ERROR);

        cameraNv21Renderer = new Nv21Renderer();
        this.setRenderer(cameraNv21Renderer);
        this.setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    public void DrawBitmap(byte[] byData, int iWidth, int iHeight) {
        cameraNv21Renderer.putData(byData, iWidth, iHeight);
        requestRender();
    }
}