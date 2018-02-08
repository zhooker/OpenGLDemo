package com.example.opengldemo.save;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import com.example.opengldemo.R;
import com.example.opengldemo.util.BaseActivity;
import com.example.opengldemo.util.L;

public class SaveActivity extends BaseActivity implements View.OnClickListener {

    protected SaveRenderer mSaveRenderer;
    protected GLSurfaceView mGLSurfaceView;
    protected ImageView mImageView;

    @Override
    protected void onCreate ( Bundle savedInstanceState )
    {
        super.onCreate ( savedInstanceState );
        setContentView (R.layout.activity_save );

        mImageView = findViewById(R.id.imageview);
        mImageView.setOnClickListener(this);
        mGLSurfaceView = findViewById(R.id.glsurfaceview);
        mGLSurfaceView.setEGLContextClientVersion ( 2 );

        mSaveRenderer = new SaveRenderer(this);
        mGLSurfaceView.setRenderer(mSaveRenderer);
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

    Drawer drawer;
    @Override
    public void onClick(View v) {
        L.d("click");

        final int width = mGLSurfaceView.getWidth();
        final int height = mGLSurfaceView.getHeight();


        mGLSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
//                drawer = new Drawer(mGLSurfaceView.getContext());
//                Bitmap input = BitmapFactory.decodeFile("/storage/emulated/0/games/gomoku/img/nopack/download/BD208310B2148A9B91FAE878B377C928.png");
//                L.d("input = " + (input == null? null : (input.getWidth() + "x" + input.getHeight())));
//                final Bitmap result = drawer.draw(input);
//                L.d("result = " + (result == null? null : (result.getWidth() + "x" + result.getHeight())));

                L.d("intput = " + (width + "x" + height));
                final Bitmap result = mSaveRenderer.readImage(width,height);
                L.d("result = " + (result == null? null : (result.getWidth() + "x" + result.getHeight())));

                mImageView.post(new Runnable() {
                    @Override
                    public void run() {
                        mImageView.setImageBitmap(result);
                    }
                });
            }
        });
    }
}

