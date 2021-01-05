package com.example.opengldemo.decoder.gl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class VideoPlayView extends GLSurfaceView {

    private RGBRenderer m_Renderer = null;

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

        m_Renderer = new RGBRenderer();
        this.setRenderer(m_Renderer);
        this.setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    public void DrawBitmap(byte[] byData, int iWidth, int iHeight) {
        m_Renderer.DrawBitmap(byData, iWidth, iHeight, 0);
        requestRender();
    }

    public void DrawClean() {
        m_Renderer.DrawClean();
        requestRender();
    }
}