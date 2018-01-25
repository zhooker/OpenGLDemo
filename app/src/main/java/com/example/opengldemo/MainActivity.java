package com.example.opengldemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TriangleView mTriangleView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
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
