package com.example.opengldemo.recorder.render;

import android.opengl.EGLSurface;
import android.view.Surface;

/**
 * Android有两套EGL的类，为了方便使用，将它们抽象出来，仅提供以下接口。
 *
 * @param <T>
 */
public interface EGLHelper<T> {
    /**
     * 返回EGLContext，用于创建共享EGLContext等。
     */
    T getContext();

    /**
     *
     * @param surface
     * @return
     */
    EGLSurface createEGLSurface(Surface surface);

    /**
     *
     * @param eglSurface
     */
    void destroyEGLSurface(EGLSurface eglSurface);

    /**
     * 将EGLContext绑定到当前线程，以及Helper中保存的draw Surface和read Surface。
     */
    void makeCurrent(EGLSurface eglSurface);

    /**
     * 解除当前线程绑定的EGLContext、draw Surface、read Surface。
     */
    void unmakeCurrent();

    /**
     * 将渲染的内容刷到绑定的绘制目标上。
     */
    boolean swapBuffers(EGLSurface eglSurface);

    /**
     * 销毁创建的EGLContext以及相关的资源。
     */
    void destroy();
}
