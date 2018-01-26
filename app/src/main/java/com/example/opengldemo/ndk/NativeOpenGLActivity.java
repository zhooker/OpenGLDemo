package com.example.opengldemo.ndk;

import android.os.Bundle;
import com.example.opengldemo.TriangleView;
import com.example.opengldemo.util.BaseActivity;

public class NativeOpenGLActivity extends BaseActivity {

    TriangleView mTriangleView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTriangleView = new TriangleView(this);
        mTriangleView.init();
        setContentView(mTriangleView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTriangleView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTriangleView.onResume();
    }
}
