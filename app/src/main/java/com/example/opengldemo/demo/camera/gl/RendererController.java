package com.example.opengldemo.demo.camera.gl;

/**
 * Created by DIY on 2017/4/5.
 */

public class RendererController {

    private SourceNV21Renderer mSourceNV21Renderer;

    public void init() {

        mSourceNV21Renderer = new SourceNV21Renderer();
    }

    public void configScreen(int width, int height) {

        synchronized (mSourceNV21Renderer) {
            mSourceNV21Renderer.configScreen(width, height);
        }
    }

    public void setOrientation(int orientation) {

        if (null != mSourceNV21Renderer) {
            mSourceNV21Renderer.setOrientation(orientation);
        }
    }

    public void drawFrameBackground() {

        synchronized (mSourceNV21Renderer) {
            mSourceNV21Renderer.draw();
        }
    }

    public void onFrame(byte[] data, int width, int height) {

        if (mSourceNV21Renderer == null) {
            return;
        }
        synchronized (mSourceNV21Renderer) {
            mSourceNV21Renderer.setPictureSize(width, height);
            mSourceNV21Renderer.setNV21Data(data, width, height);
        }
    }
}
