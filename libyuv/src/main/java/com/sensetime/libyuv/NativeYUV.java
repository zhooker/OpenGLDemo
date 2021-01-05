package com.sensetime.libyuv;

import android.view.Surface;

public class NativeYUV {

    static {
        System.loadLibrary("native-yuv");
    }

    /**
     * 转换类型
     */
    public interface Type {
        int YUV420P_TO_RGB24 = 0;
        int NV12_TO_RGB24 = 1;
        int NV21_TO_RGB24 = 2;
    }

    public native void yuv2rgb(String yuvPath, int type, int width, int height, Surface surface);

    public static native byte[] convertYuv2Rgb(byte[] input, int width, int height);

    public static native void yuv2Surface(byte[] input, int width, int height, Surface surface);
}
