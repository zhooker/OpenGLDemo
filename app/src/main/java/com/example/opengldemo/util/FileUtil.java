package com.example.opengldemo.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by zhuangsj on 18-2-7.
 */

public class FileUtil {

    public static boolean saveBitmap2File(Bitmap bitmap, String path) {

        if (bitmap == null || TextUtils.isEmpty(path))
            return false;

        File file = new File("/storage/emulated/0/1");
        if(!file.exists()) {
            file.mkdir();
        }


        File f = new File(file.getAbsolutePath() + "/"+ path);
        FileOutputStream fOut = null;
        boolean result = true;
        try {
            f.createNewFile();
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            result = false;
            e.printStackTrace();
        } catch (IOException e) {
            result = false;
            e.printStackTrace();
        } finally {
            if (!result && fOut != null)
                try {
                    fOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        if (result) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
            try {
                fOut.flush();
            } catch (IOException e) {
                result = false;
                e.printStackTrace();
            } finally {
                try {
                    fOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static boolean saveBitmap2File(byte[] aJpegData, String getFilePath) {
        if (aJpegData == null || TextUtils.isEmpty(getFilePath))
            return false;

        File file = new File("/storage/emulated/0/1");
        if(!file.exists()) {
            file.mkdir();
        }


        boolean result = true;
        OutputStream outputStream = null;
        try {
            File getFile = new File(file.getAbsolutePath() + "/"+ getFilePath);
            outputStream = new FileOutputStream(getFile);
            outputStream.write(aJpegData);
        } catch (Exception ex) {
            result = false;
            L.d("saveBitmap2File error " + ex.getMessage());
        } finally {
            try {
                if(outputStream != null)
                    outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    public static String getRealFilePath(final Context context, final Uri uri ) {
        if ( null == uri ) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if ( scheme == null )
            data = uri.getPath();
        else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
            data = uri.getPath();
        } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
            Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
            if ( null != cursor ) {
                if ( cursor.moveToFirst() ) {
                    int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
                    if ( index > -1 ) {
                        data = cursor.getString( index );
                    }
                }
                cursor.close();
            }
        }
        return data;
    }
}
