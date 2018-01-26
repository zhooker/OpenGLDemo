package com.example.opengldemo.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.WindowManager;
import java.lang.reflect.Method;

/**
 * Created by zhuangsj on 16-11-3.
 */

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setTitle(getClass().getSimpleName());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static void goToActivity(Context context, Class<? extends Activity> clazz) {
        Intent intent = new Intent();
        intent.setClass(context, clazz);
        context.startActivity(intent);
    }

    private boolean isTopOfTask() {
        try {
            Class clazz = getClass();
            while (!clazz.equals(Activity.class)) {
                clazz = clazz.getSuperclass();
            }

            Method method = clazz.getDeclaredMethod("isTopOfTask",new Class[] {});
            method.setAccessible(true);
            return (boolean) method.invoke(this, new Object[] {});
        } catch (Exception e) {
            e.printStackTrace();
            L.d("isTopOfTask","isTopOfTask  error = " + e.getMessage());
        }
        return false;
    }
}
