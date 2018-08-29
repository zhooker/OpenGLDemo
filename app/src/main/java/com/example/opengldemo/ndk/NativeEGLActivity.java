package com.example.opengldemo.ndk;

import android.opengl.GLSurfaceView;

import com.example.opengldemo.base.BaseRendererActivity;

import javax.microedition.khronos.opengles.GL10;

public class NativeEGLActivity extends BaseRendererActivity {

    @Override
    protected GLSurfaceView.Renderer getRenderer() {
        return new TriangleRender();
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
