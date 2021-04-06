package com.example.opengldemo.recorder;

import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

import com.example.opengldemo.R;
import com.example.opengldemo.base.BaseActivity;
import com.example.opengldemo.util.L;

import java.io.IOException;

/**
 * 通过实时预览 与 实时渲染 两个画面，来做预览对比
 */
public class RecorderActivity extends BaseActivity implements Camera.PreviewCallback {


    protected SurfaceView mSurfaceView;
    protected TextureView textureView;
    protected CameraRenderer cameraRenderer = new CameraRenderer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);

        textureView = findViewById(R.id.texture);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                cameraRenderer.start(surface, width, height);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });

        mSurfaceView = findViewById(R.id.surfaceview);
        SurfaceHolder mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        mSurfaceHolder.addCallback(mSurfaceCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraRenderer.stop();
    }

    /**
     * data = 3110400, size = 1920x1080
     * data = 1382400, size = 1280x720
     */
    protected Camera initCamera() {
        try {
            Camera camera = Camera.open();
            camera.setDisplayOrientation(90);

            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(1280, 720);
            camera.setParameters(parameters);

            camera.autoFocus(null);
            camera.setPreviewDisplay(mSurfaceView.getHolder());
            camera.setPreviewCallback(RecorderActivity.this);
            return camera;
        } catch (IOException e) {
            L.d(e.toString());
        }
        return null;
    }

    private SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {
        private Camera camera;
        private boolean isPreview = false;

        /**
         *  在 Surface 首次创建时被立即调用：活得叫焦点时。一般在这里开启画图的线程
         * @param surfaceHolder 持有当前 Surface 的 SurfaceHolder 对象
         */
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            camera = initCamera();
            if (camera != null) {
                camera.startPreview();
                isPreview = true;
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        /**
         *  在 Surface 被销毁时立即调用：失去焦点时。一般在这里将画图的线程停止销毁
         * @param surfaceHolder 持有当前 Surface 的 SurfaceHolder 对象
         */
        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            if (camera != null) {
                if (isPreview) {//正在预览
                    camera.stopPreview();
                }
                camera.setPreviewCallback(null);
                camera.release();
                camera = null;
            }
        }
    };

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Camera.Size size = camera.getParameters().getPreviewSize();
        L.d("preview size = " + size.width + "x" + size.height + ", format = " + camera.getParameters().getPreviewFormat());
//        cameraNv21Renderer.putData(data, 1280, 720);
//        rgbTextureView.onFrame(data, 1280, 720);
        cameraRenderer.onRenderVideoFrame(data);
    }

    public void onStartRecord(View view) {
        cameraRenderer.startRecord("/sdcard/record_test.mp4");
    }

    public void onStopRecord(View view) {
        cameraRenderer.stopRecord();
    }
}
