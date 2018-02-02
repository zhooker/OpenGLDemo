package com.example.opengldemo.test;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.opengldemo.util.BaseActivity;
import com.example.opengldemo.util.L;

public class TestActivity extends BaseActivity {

    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tv = new TextView(this);
        tv.setText("Hello");
        setContentView(tv);



        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            MappedByteBufferTest.MappedByteBufferTest();
                            MappedByteBufferTest.BufferTest();
                            MappedByteBufferTest.BufferedInputStreamTest();
                        } catch (Exception e) {
                            e.printStackTrace();
                            L.d("test error " + e.getMessage());
                        }
                    }
                }).start();
            }
        });
    }
}
