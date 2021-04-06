package com.example.opengldemo.recorder;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import android.view.Surface;
import android.widget.ImageView.ScaleType;

import com.example.opengldemo.recorder.opengl.GpuImageI420Filter;
import com.example.opengldemo.recorder.opengl.OpenGlUtils;
import com.example.opengldemo.recorder.opengl.Rotation;
import com.example.opengldemo.recorder.render.EglCore;
import com.example.opengldemo.util.Size;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.concurrent.CountDownLatch;

@TargetApi(17)
public class CameraRenderer implements Handler.Callback {
    public static final String TAG = "CameraRenderer";

    private static final int MSG_RENDER = 2;
    private static final int MSG_DESTROY = 3;

    private HandlerThread mGLThread;
    private GLHandler mGLHandler;
    private final FloatBuffer mGLCubeBuffer;
    private final FloatBuffer mGLTextureBuffer;
    private EglCore mEglCore;
    private SurfaceTexture mSurfaceTexture;
    private Size mSurfaceSize = new Size();
    private Size mInputSurfaceSize = new Size();
    private Size mLastInputSize = new Size();
    private Size mLastOutputSize = new Size();
    private GpuImageI420Filter mYUVFilter;

    private EGLSurface mRecordSurface;
    private MediaRecorderHelper mediaRecorderHelper;
    private volatile boolean isStopRecord = false;

    public CameraRenderer() {
        mGLCubeBuffer = ByteBuffer.allocateDirect(OpenGlUtils.CUBE.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mGLCubeBuffer.put(OpenGlUtils.CUBE).position(0);

        mGLTextureBuffer = ByteBuffer.allocateDirect(OpenGlUtils.TEXTURE.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mGLTextureBuffer.put(OpenGlUtils.TEXTURE).position(0);

        mediaRecorderHelper = new MediaRecorderHelper();
       // mSurfaceTexture = new SurfaceTexture((int) (Math.random() * 63 + 1));
    }

    public void start(SurfaceTexture surfaceTexture, int width, int height) {
        mSurfaceTexture = surfaceTexture;
        mSurfaceSize = new Size(width, height);
        mInputSurfaceSize = new Size(1280, 720);

        if (mGLThread == null) {
            mGLThread = new HandlerThread(TAG);
            mGLThread.start();
            mGLHandler = new GLHandler(mGLThread.getLooper(), this);
        }
    }

    public void stop() {
        mGLHandler.obtainMessage(MSG_DESTROY).sendToTarget();
    }

    public void onRenderVideoFrame(final byte[] data) {
        if (!isStopRecord) {
            try {
                mGLHandler.obtainMessage(MSG_RENDER, data).sendToTarget();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initGlComponent(Object eglContext) {
        if (mSurfaceTexture == null) {
            return;
        }

        // 创建的时候，增加判断，防止这边创建的时候，传入的EGLContext已经被销毁了。
        try {
            if (eglContext instanceof javax.microedition.khronos.egl.EGLContext) {
                mEglCore = new EglCore((javax.microedition.khronos.egl.EGLContext) eglContext, new Surface(mSurfaceTexture));
            } else {
                mEglCore = new EglCore((android.opengl.EGLContext) eglContext, new Surface(mSurfaceTexture));
            }
        } catch (Exception e) {
            Log.e(TAG, "create EglCore failed.", e);
            return;
        }

        mEglCore.makeCurrent(null);
        mYUVFilter = new GpuImageI420Filter();
        mYUVFilter.init();
    }

    private void renderInternal(byte[] data) {

        if (mEglCore == null) {
            initGlComponent(null);
        }

        if (mEglCore == null) {
            return;
        }

        if (mLastInputSize.width != mInputSurfaceSize.width || mLastInputSize.height != mInputSurfaceSize.height
                || mLastOutputSize.width != mSurfaceSize.width || mLastOutputSize.height != mSurfaceSize.height) {
            mLastInputSize = new Size(mInputSurfaceSize.width, mInputSurfaceSize.height);
            mLastOutputSize = new Size(mSurfaceSize.width, mSurfaceSize.height);

            Pair<float[], float[]> cubeAndTextureBuffer = OpenGlUtils.calcCubeAndTextureBuffer(ScaleType.CENTER_CROP,
                    Rotation.ROTATION_90, false, mLastInputSize.width, mLastInputSize.height, mLastOutputSize.width, mLastOutputSize.height);
            mGLCubeBuffer.clear();
            mGLCubeBuffer.put(cubeAndTextureBuffer.first);
            mGLTextureBuffer.clear();
            mGLTextureBuffer.put(cubeAndTextureBuffer.second);
        }

        renderContent(data, null);
        if (mRecordSurface != null) {
            renderContent(data, mRecordSurface);
        }
    }

    public void startRecord(final String filename) {
        mGLHandler.post(new Runnable() {
            @Override
            public void run() {
                Surface surface = mediaRecorderHelper.startRecord(filename, 720, 1280);
                if (mEglCore == null) {
                    initGlComponent(null);
                }
                if (surface != null) {
                    mRecordSurface = mEglCore.createEGLSurface(surface);
                }
                isStopRecord = false;
            }
        });
    }

    public void stopRecord() {
        final EGLSurface tempSurface = mRecordSurface;
        mRecordSurface = null;
        isStopRecord = true;
        if (tempSurface != null) {
            mGLHandler.post(new Runnable() {
                @Override
                public void run() {
                    mEglCore.destroyEGLSurface(tempSurface);
                    mediaRecorderHelper.stopRecord();
                }
            });
        }
    }

    private void renderContent(byte[] data, EGLSurface eglSurface) {
        mEglCore.makeCurrent(eglSurface);
        GLES20.glViewport(0, 0, mLastOutputSize.width, mLastOutputSize.height);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        GLES20.glClearColor(0, 0, 0, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        mYUVFilter.loadYuvDataToTexture(data, mLastInputSize.width, mLastInputSize.height);
        mYUVFilter.onDraw(OpenGlUtils.NO_TEXTURE, mGLCubeBuffer, mGLTextureBuffer);
        mEglCore.swapBuffer(eglSurface);
    }

    private void uninitGlComponent() {
        if (mYUVFilter != null) {
            mYUVFilter.destroy();
            mYUVFilter = null;
        }
        if (mEglCore != null) {
            mEglCore.unmakeCurrent();
            mEglCore.destroy();
            mEglCore = null;
        }
    }

    private void destroyInternal() {
        uninitGlComponent();

        if (Build.VERSION.SDK_INT >= 18) {
            mGLHandler.getLooper().quitSafely();
        } else {
            mGLHandler.getLooper().quit();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_RENDER:
                renderInternal((byte[]) msg.obj);
                break;
            case MSG_DESTROY:
                destroyInternal();
                break;
        }
        return false;
    }

    public static class GLHandler extends Handler {
        public GLHandler(Looper looper, Callback callback) {
            super(looper, callback);
        }

        public void runAndWaitDone(final Runnable runnable) {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            post(new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                    countDownLatch.countDown();
                }
            });

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
