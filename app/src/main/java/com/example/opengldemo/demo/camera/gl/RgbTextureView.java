package com.example.opengldemo.demo.camera.gl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.util.AttributeSet;
import android.view.TextureView;

/**
 * @author chenzhaojie
 * @date 2020/3/16
 */
public class RgbTextureView extends TextureView {

    private int mPreviewHeight;
    private int mPreviewWidth;
    private Matrix mMatrix;
    private ScriptIntrinsicYuvToRGB mScriptIntrinsicYuvToRGB;
    private Allocation mInAllocation, mOutAllocation;
    private Bitmap mBitmap;

    public RgbTextureView(Context context) {

        super(context);
    }

    public RgbTextureView(Context context, AttributeSet attrs) {

        super(context, attrs);
    }

    public void setPreviewSize(int width, int height) {

        this.mPreviewWidth = width;
        this.mPreviewHeight = height;
    }

    public void setMatrix(Matrix matrix) {

        this.mMatrix = matrix;
    }

    public void onFrame(byte[] data, int width, int height) {

        if (mMatrix == null) {
            return;
        }
        Canvas canvas = lockCanvas();
        if (canvas == null) {
            return;
        }
        // 渲染不可开子线程处理，否则会出现整机CPU占用率增加且画面延迟严重的情况
        Bitmap sceneBtm = getSceneBtm(data, width, height);
        try {
            canvas.save();
            canvas.setMatrix(mMatrix);
            canvas.drawBitmap(sceneBtm, 0, 0, null);
            canvas.restore();
        } finally {
            unlockCanvasAndPost(canvas);
        }
    }

    /**
     * 根据nv21数据生成bitmap
     */
    private Bitmap getSceneBtm(byte[] nv21Bytes, int width, int height) {//8ms左右

        if (nv21Bytes == null) {
            return null;
        }

        if (mInAllocation == null) {
            initRenderScript(width, height);
        }
        mInAllocation.copyFrom(nv21Bytes);
        mScriptIntrinsicYuvToRGB.setInput(mInAllocation);
        mScriptIntrinsicYuvToRGB.forEach(mOutAllocation);
        if (mBitmap == null || mBitmap.getWidth() != width || mBitmap.getHeight() != height) {
            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }
        mOutAllocation.copyTo(mBitmap);
        return mBitmap;
    }

    private void initRenderScript(int width, int height) {

        RenderScript mRenderScript = RenderScript.create(getContext());
        mScriptIntrinsicYuvToRGB = ScriptIntrinsicYuvToRGB.create(mRenderScript,
                Element.U8_4(mRenderScript));

        Type.Builder yuvType = new Type.Builder(mRenderScript, Element.U8(mRenderScript))
                .setX(width * height * 3 / 2);
        mInAllocation = Allocation.createTyped(mRenderScript,
                yuvType.create(),
                Allocation.USAGE_SCRIPT);

        Type.Builder rgbaType = new Type.Builder(mRenderScript, Element.RGBA_8888(mRenderScript))
                .setX(width).setY(height);
        mOutAllocation = Allocation.createTyped(mRenderScript,
                rgbaType.create(),
                Allocation.USAGE_SCRIPT);
    }
}
