package com.example.opengldemo.demo.camera.gl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author zhaohongbin
 * @date 2018/5/31
 */
public class CameraGLSurfaceView extends GLSurfaceView {

    private RendererController mRendererController;
    private int mPreviewHeight;
    private int mPreviewWidth;

    public CameraGLSurfaceView(Context context) {

        super(context);
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 24, 8);
        setRenderer(new MyRenderer());
        //主动渲染
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        mRendererController = new RendererController();
    }

    public CameraGLSurfaceView(Context context, AttributeSet attrs) {

        super(context, attrs);
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 24, 8);
        setRenderer(new MyRenderer());
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        mRendererController = new RendererController();
    }

    public void setPreviewSize(int width, int height) {

        this.mPreviewWidth = width;
        this.mPreviewHeight = height;
    }

    public void onFrame(byte[] data, int width, int height) {

        mRendererController.onFrame(data, width, height);
    }

    private class MyRenderer implements Renderer {

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {

            mRendererController.init();
            mRendererController.setOrientation(270);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {

            mRendererController.configScreen(mPreviewWidth, mPreviewWidth);
            //获取预览尺寸
            GLES20.glDisable(GLES20.GL_CULL_FACE);
        }

        @Override
        public void onDrawFrame(GL10 gl) {

            GLES20.glClearColor(0, 0, 0, 1);
            GLES20.glDepthMask(true);
            GLES20.glColorMask(true, true, true, true);
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            mRendererController.drawFrameBackground();
            GLES20.glFinish();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        setMeasuredDimension(mPreviewWidth, mPreviewHeight);
    }
}
