package com.example.opengldemo.decoder;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Size;

import com.sensetime.libyuv.NativeYUV;

import java.io.IOException;
import java.nio.ByteBuffer;

import static android.media.MediaExtractor.SEEK_TO_CLOSEST_SYNC;


public class VideoDecoder {

    private final int decodeColorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible;

    private MediaCodec mCodec;
    private MediaExtractor mMediaExtractor;
    private VideoDecoderListener mLdDecoderListener;

    private volatile boolean isRunning = false;
    private volatile boolean isPaused = false;
    private int currentTrackIndex = -1;
    private Size screenSize = new Size(-1, -1);

    public void setLdDecoderListener(VideoDecoderListener ldDecoderListener) {
        mLdDecoderListener = ldDecoderListener;
    }

    public void setDataSource(String srcVideoPath) {
        mMediaExtractor = new MediaExtractor();//数据解析器
        try {
            mMediaExtractor.setDataSource(srcVideoPath);
            for (int i = 0; i < mMediaExtractor.getTrackCount(); i++) {//遍历数据源音视频轨迹
                MediaFormat format = mMediaExtractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
//                format.setInteger(MediaFormat.KEY_COLOR_FORMAT, decodeColorFormat);
//                LogUtils.e("video====" + mime);//video/avc
                if (mime.startsWith("video/")) {
                    mMediaExtractor.selectTrack(i);

                    mCodec = MediaCodec.createDecoderByType(mime);
                    if (isColorFormatSupported(decodeColorFormat, mCodec.getCodecInfo().getCapabilitiesForType(mime))) {
                        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, decodeColorFormat);
                    }
                    screenSize = new Size(format.getInteger(MediaFormat.KEY_WIDTH), format.getInteger(MediaFormat.KEY_HEIGHT));
                    currentTrackIndex = i;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    long lastSystemTime = -1;
    long lastMediaTime = -1;

    public void start() {

        if (mMediaExtractor == null || mCodec == null) {
            return;
        }

        isPaused = false;
        isRunning = true;

        mMediaExtractor.seekTo(0, SEEK_TO_CLOSEST_SYNC);
        mCodec.reset();
        MediaFormat format = mMediaExtractor.getTrackFormat(currentTrackIndex);
        mCodec.configure(format, null, null, 0);
        mCodec.start();

        ByteBuffer[] inputBuffers = mCodec.getInputBuffers();
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        boolean isEOS = false;
        long sampleTime = 0, totalSampleTime = format.getLong(MediaFormat.KEY_DURATION);

        while (isRunning) {
            if (isPaused) {
                synchronized (VideoDecoder.this) {
                    try {
                        VideoDecoder.this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (!isRunning || Thread.interrupted()) {
                break;
            }

            if (!isEOS) {
                int inIndex = mCodec.dequeueInputBuffer(-1);
                if (inIndex >= 0) {
                    ByteBuffer buffer = inputBuffers[inIndex];
                    //把指定通道中的数据按偏移量读取到ByteBuffer中；读取的是一帧数据
                    int sampleSize = mMediaExtractor.readSampleData(buffer, 0);
                    //读取时间戳
                    long time = mMediaExtractor.getSampleTime();

                    if (time < 0) {
                        // dequeueOutputBuffer
                        mCodec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        isEOS = true;
                    } else {
                        sampleTime = time;

                        // 与系统时间同步
                        if (lastMediaTime > 0 && lastSystemTime > 0) {
                            try {
                                Thread.sleep((time - lastMediaTime) / 1000 - (System.currentTimeMillis() - lastSystemTime));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        lastMediaTime = time;
                        lastSystemTime = System.currentTimeMillis();


                        mCodec.queueInputBuffer(inIndex, 0, sampleSize, sampleTime, mMediaExtractor.getSampleFlags());
                        //读取一帧后必须调用，提取下一帧
                        mMediaExtractor.advance();
                    }
                }
            }

            int outIndex = mCodec.dequeueOutputBuffer(info, -1);
            switch (outIndex) {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
//                        LogUtils.e(">> output buffer changed ");
                    break;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
//                        LogUtils.e(">> output buffer changed ");
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
//                        LogUtils.e(">> dequeueOutputBuffer timeout ");
                    break;
                default:
                    Image image = mCodec.getOutputImage(outIndex);
                    byte[] rgb = convertBGRFromImage(image, screenSize.getWidth(), screenSize.getHeight());
                    image.close();
//                    ByteBuffer buffer = mCodec.getOutputBuffer(outIndex);
//                    byte[] rgb = new byte[(int) (1280*720*1.5f)];
//                    buffer.get(rgb);
                    mCodec.releaseOutputBuffer(outIndex, true);

                    if (mLdDecoderListener != null) {
                        mLdDecoderListener.onDecodeFrame(rgb, sampleTime, totalSampleTime);
                    }
                    break;
            }

            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                if (mLdDecoderListener != null) {
                    mLdDecoderListener.onDecodeFinish();
                }
                break;
            }
        }

        if (!isRunning) {
            release();
        }
    }

    public void resume() {
        isPaused = false;
        synchronized (VideoDecoder.this) {
            VideoDecoder.this.notify();
        }
    }

    public void pause() {
        isPaused = true;
    }

    public void stop() {
        if (mMediaExtractor == null || mCodec == null) {
            return;
        }

        isRunning = false;
        synchronized (VideoDecoder.this) {
            VideoDecoder.this.notify();
        }
    }

    protected void release() {
        if (null != mCodec) {
            mCodec.stop();
            mCodec.release();
            mCodec = null;
        }

        if (null != mMediaExtractor) {
            mMediaExtractor.release();
            mMediaExtractor = null;
        }
    }

    protected byte[] convertNV21FromImage(Image image) {
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];

        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    channelOffset = width * height + 1;
                    outputStride = 2;
                    break;
                case 2:
                    channelOffset = width * height;
                    outputStride = 2;
                    break;
            }
            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();
            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = w;
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
        }
        return data;
    }

    protected byte[] convertBGRFromImage(Image image, int width, int height) {

        byte[] input;
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();
        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();
        input = new byte[ySize + uSize + vSize];

        yBuffer.get(input, 0, ySize);
        vBuffer.get(input, ySize, vSize);
        uBuffer.get(input, ySize + vSize, uSize);

        return NativeYUV.convertYuv2Rgb(input, width, height);
//        byte[] output = new byte[screenSize.getWidth() * screenSize.getHeight() * 3];
//        int nvOff = width * height;
//        int i, j, yIndex = 0;
//        int y, u, v;
//        int r, g, b, nvIndex = 0;
//        for (i = 0; i < height; i++) {
//            for (j = 0; j < width; j++, ++yIndex) {
//                nvIndex = (i / 2) * width + j - j % 2;
//                y = input[yIndex] & 0xff;
//                u = input[nvOff + nvIndex] & 0xff;
//                v = input[nvOff + nvIndex + 1] & 0xff;
//
//                r = y + ((351 * (v - 128)) >> 8);
//                g = y - ((179 * (v - 128) + 86 * (u - 128)) >> 8);
//                b = y + ((443 * (u - 128)) >> 8);
//
//                r = ((r > 255) ? 255 : (r < 0) ? 0 : r);
//                g = ((g > 255) ? 255 : (g < 0) ? 0 : g);
//                b = ((b > 255) ? 255 : (b < 0) ? 0 : b);
//
//                output[yIndex * 3 + 0] = (byte) r;
//                output[yIndex * 3 + 1] = (byte) g;
//                output[yIndex * 3 + 2] = (byte) b;
//            }
//        }
//
//        return output;
    }

    protected boolean isColorFormatSupported(int colorFormat, MediaCodecInfo.CodecCapabilities caps) {
        for (int c : caps.colorFormats) {
            if (c == colorFormat) {
                return true;
            }
        }
        return false;
    }

    public Size getScreenSize() {
        return screenSize;
    }

    public interface VideoDecoderListener {

        void onDecodeFrame(byte[] result, long duration, long totalDuration);

        void onDecodeFinish();
    }
}
