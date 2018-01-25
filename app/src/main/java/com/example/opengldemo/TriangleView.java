package com.example.opengldemo;

import android.content.Context;
import android.opengl.EGLConfig;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by zhooker on 2018/1/25.
 */

public class TriangleView extends GLSurfaceView  {

    public TriangleView(Context context) {
        super(context);
    }

    public TriangleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    void init() {
        setEGLConfigChooser(8,8,8,0,16,0);
        setEGLContextClientVersion(3);
        setRenderer(new TriangleRender());
    }
    class TriangleRender implements  GLSurfaceView.Renderer{

        @Override
        public void onSurfaceCreated(GL10 gl10, javax.microedition.khronos.egl.EGLConfig eglConfig) {
            TriangleLib.init();
        }

        @Override
        public void onSurfaceChanged(GL10 gl10, int width, int height) {
            TriangleLib.resize(width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl10) {
            TriangleLib.step();
        }
    }
}
