package com.example.opengldemo.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zhuangsj on 18-2-6.
 */

public class AssetsUtils {
    public static String read(Context context, String fileName) {
        String result = null;
        try {
            InputStream is = context.getResources().getAssets().open("" + fileName);
            int length = is.available();
            byte[] buffer = new byte[length];
            is.read(buffer);
            result = new String(buffer, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static Bitmap getBitmap(Context context, String fileName) {
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        try
        {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return image;
    }
}
