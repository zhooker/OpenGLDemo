package com.example.opengldemo.camera2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;

/**
 * @author zhuangsj
 * @created 2018/8/27
 */
public class Presenter {

    interface IView {
        void onProcess(Bitmap bitmap);
    }

    private final IView iView;
    private boolean isProcess = false;
    private int count = 0;


    public Presenter(IView iView) {
        this.iView = iView;
    }

    public void process(Image image) {
        if (isProcess) {
            return;
        }

        if (count < 5) {
            count++;
            return;
        }

        isProcess = true;

        processImage(image);

        isProcess = false;
        count = 0;
    }


    public void processImage(Image image) {
        byte[] jpegData = ImageUtil.imageToByteArray(image);
        Bitmap bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length);
        iView.onProcess(bitmap);
    }
}
