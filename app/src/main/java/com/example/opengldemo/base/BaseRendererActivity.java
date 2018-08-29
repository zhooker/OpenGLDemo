package com.example.opengldemo.base;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.opengldemo.util.L;

public abstract class BaseRendererActivity extends BaseActivity {

    protected static final String TAG = "zhuangsj";
    protected final int CONTEXT_CLIENT_VERSION = 2;

    protected GLSurfaceView mGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLSurfaceView = new GLSurfaceView(this);

        if (detectOpenGLES30()) {
            // Tell the surface view we want to create an OpenGL ES 3.0-compatible
            // context, and set an OpenGL ES 3.0-compatible renderer.
            mGLSurfaceView.setEGLContextClientVersion(CONTEXT_CLIENT_VERSION);
            mGLSurfaceView.setRenderer(getRenderer());
        } else {
            Toast.makeText(this, "OpenGL ES 3.0 not supported on device.  Exiting...", Toast.LENGTH_LONG).show();
            finish();

        }

        setContentView(mGLSurfaceView);
    }

    private boolean detectOpenGLES30() {
        ActivityManager am =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        L.d("OpenGL ES version = 0x" + Integer.toHexString(info.reqGlEsVersion));
        return (info.reqGlEsVersion >= 0x30000);
    }

    @Override
    protected void onResume() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();
        mGLSurfaceView.onPause();
    }

    protected abstract GLSurfaceView.Renderer getRenderer();

}
