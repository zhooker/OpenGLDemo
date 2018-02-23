package com.example.opengldemo.camera;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.example.opengldemo.R;
import com.example.opengldemo.util.BaseActivity;
import com.example.opengldemo.util.L;
import java.io.IOException;

public class CameraActivity extends BaseActivity implements Camera.PreviewCallback {

    protected GLSurfaceView mGLSurfaceView;
    protected SurfaceView mSurfaceView;

    @Override
    protected void onCreate ( Bundle savedInstanceState )
    {
        super.onCreate ( savedInstanceState );
        setContentView (R.layout.activity_camera2 );

        mGLSurfaceView = findViewById(R.id.glsurfaceview);
        mGLSurfaceView.setEGLContextClientVersion ( 2 );
        mGLSurfaceView.setRenderer (new CameraRenderer(mGLSurfaceView));

        mSurfaceView = findViewById(R.id.surfaceview);
        SurfaceHolder mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        mSurfaceHolder.addCallback(mSurfaceCallback);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mGLSurfaceView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
            camera.setPreviewCallback(CameraActivity.this);
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
            if(camera != null){
                if(isPreview){//正在预览
                    camera.stopPreview();
                }
                camera.release();
                camera = null;
            }
        }
    };

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Camera.Size size = camera.getParameters().getPreviewSize();
        L.d("data = " + (data == null ? null : data.length) + ", format = " + camera.getParameters().getPreviewFormat());
        if (size != null)
            L.d("size = " + size.width + "x" + size.height);
    }
}
