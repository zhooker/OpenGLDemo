package com.example.opengldemo.decoder;

import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.example.opengldemo.R;
import com.example.opengldemo.decoder.gl.VideoPlayView;

/**
 * 测试视频解码并渲染
 */
public class MediaDecoderActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private Button mBtnRePlay;
    private ProgressBar progressBar;
    private SurfaceView mSurfaceView;
    private SurfaceView mBodySurfaceView;

    private VideoDecoder videoDecoder;
    private VideoPlayView videoPlayView;

    private long lastTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decoder);

        // 初始化view
        initViews();

        try {
            videoDecoder = new VideoDecoder();
            videoDecoder.setDataSource("/sdcard/party.mp4");
            videoDecoder.setLdDecoderListener(new VideoDecoder.VideoDecoderListener() {

                @Override
                public void onDecodeFrame(byte[] data, long duration, long totalDuration) {
                    Log.w("zsj", "onDecodeFrame=====" + duration + "," + totalDuration);

                    videoPlayView.DrawBitmap(data, videoDecoder.getScreenSize().getWidth(), videoDecoder.getScreenSize().getHeight());

                    if (System.currentTimeMillis() - lastTime >= 100) {
                        progressBar.setProgress((int) (duration * 100f / totalDuration));
                        lastTime = System.currentTimeMillis();
                    }
                }

                @Override
                public void onDecodeFinish() {
                    mBtnRePlay.post(new Runnable() {
                        @Override
                        public void run() {
                            mBtnRePlay.setText("Replay");
                            progressBar.setProgress(100);
                        }
                    });
                }
            });

//            mediaPlayer.reset();
//            mediaPlayer.setDataSource(filepath);
//            mediaPlayer.prepare();

            // 修改尺寸
            FrameLayout container = (FrameLayout) findViewById(R.id.preview_container);
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) container.getLayoutParams();
            layoutParams.height = screenWidth * videoDecoder.getScreenSize().getHeight() / videoDecoder.getScreenSize().getWidth();
            container.setLayoutParams(layoutParams);
        } catch (Throwable throwable) {
            finish();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                videoDecoder.start();
            }
        }).start();
    }

    private void initViews() {
        progressBar = findViewById(R.id.progress);

        mBtnRePlay = findViewById(R.id.btn_replay);
        mBtnRePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaDecoderActivity.this.replay();
            }
        });

        mSurfaceView = findViewById(R.id.sv_face);
        mSurfaceView.setZOrderOnTop(true);
        mSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        mBodySurfaceView = findViewById(R.id.sv_body);
        mBodySurfaceView.setZOrderOnTop(true);
        mBodySurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        videoPlayView = findViewById(R.id.sfv_show);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//        mediaPlayer.setSurface(new Surface(surface));
//
//        textureDecoder.start();
//        MainThreadImpl.post(() -> mediaPlayer.start());
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        videoDecoder.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoPlayView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoPlayView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void replay() {
        if (mBtnRePlay.getText().toString().equals("Play")) {
            videoDecoder.resume();
            mBtnRePlay.setText("Pause");
        } else if (mBtnRePlay.getText().toString().equals("Pause")) {
//            currentDuration = mediaPlayer.getCurrentPosition();
            videoDecoder.pause();
            mBtnRePlay.setText("Play");
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    videoDecoder.start();
                }
            }).start();

            mBtnRePlay.setText("Pause");
        }
    }
}