package com.example.opengldemo.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;

public abstract class BaseRendererActivity extends BaseActivity {

    protected static final String TAG = "zhuangsj";
    private final int CONTEXT_CLIENT_VERSION = 3;

    private GLSurfaceView mGLSurfaceView;

    @Override
    protected void onCreate ( Bundle savedInstanceState )
    {
        super.onCreate ( savedInstanceState );
        mGLSurfaceView = new GLSurfaceView ( this );

        if ( detectOpenGLES30() )
        {
            // Tell the surface view we want to create an OpenGL ES 3.0-compatible
            // context, and set an OpenGL ES 3.0-compatible renderer.
            mGLSurfaceView.setEGLContextClientVersion ( CONTEXT_CLIENT_VERSION );
            mGLSurfaceView.setRenderer (getRenderer());
        }
        else
        {
            Log.e ( TAG, "OpenGL ES 3.0 not supported on device.  Exiting..." );
            finish();

        }

        setContentView ( mGLSurfaceView );
    }

    private boolean detectOpenGLES30()
    {
        ActivityManager am =
                ( ActivityManager ) getSystemService ( Context.ACTIVITY_SERVICE );
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        return ( info.reqGlEsVersion >= 0x30000 );
    }

    @Override
    protected void onResume()
    {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause()
    {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();
        mGLSurfaceView.onPause();
    }

    protected abstract GLSurfaceView.Renderer getRenderer();

}