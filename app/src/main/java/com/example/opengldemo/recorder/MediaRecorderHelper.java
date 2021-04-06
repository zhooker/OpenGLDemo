package com.example.opengldemo.recorder;

import android.media.MediaRecorder;
import android.view.Surface;

import java.io.IOException;

public class MediaRecorderHelper {

    private static final int VIDEO_BIT_RATE = (int) (1.5 * 1024 * 1024);
    private static final int VIDEO_FRAME_RATE = 25;
    private static final int AUDIO_BIT_RATE = 44800;

    private MediaRecorder mMediaRecorder;

    public Surface startRecord(String filename, int width, int height) {
        if (mMediaRecorder != null) {
            return null;
        }

        mMediaRecorder = new MediaRecorder();
//        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(filename);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setVideoEncodingBitRate(VIDEO_BIT_RATE);
        mMediaRecorder.setVideoSize(width, height);
        mMediaRecorder.setVideoFrameRate(VIDEO_FRAME_RATE);
//        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
//        mMediaRecorder.setAudioEncodingBitRate(AUDIO_BIT_RATE);
        mMediaRecorder.setOrientationHint(0);

        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            return mMediaRecorder.getSurface();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void stopRecord() {
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.stop();
                mMediaRecorder.release();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mMediaRecorder = null;
            }
        }
    }
}
