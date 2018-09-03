package com.example.opengldemo;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.opengldemo.light.SpotLightActivity;
import com.example.opengldemo.light.SpotLightActivity2;
import com.example.opengldemo.ndk.NativeEGLActivity;
import com.example.opengldemo.test.TestActivity;
import com.example.opengldemo.texture.TextureActivity;
import com.example.opengldemo.util.BaseActivity;

public class MainActivity extends AppCompatActivity {

    private static final Class<? extends BaseActivity>[] ACTIVITIES = new Class[]{
            NativeEGLActivity.class,
            SpotLightActivity.class,
            SpotLightActivity2.class,
            TextureActivity.class,
            TestActivity.class
    };

    private static final String[] ACTIVITIE_DESC = new String[]{
            "NDK Demo\n使用NDK的方式使用 OpenGL",
            "Spot Light Demo\n使用顶点着色器渲染光照",
            "Spot Light Demo 2\n使用片段着色器渲染光照",
            "Texture Demo\n使用纹理贴图",
            "Test Demo\n 测试MappedByteBuffer "
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LinearLayout container = (LinearLayout) findViewById(R.id.btn_container);
        for (int i = 0; i < ACTIVITIES.length; i++) {
            Button btn = createButton();
            final Class<? extends Activity> clazz = ACTIVITIES[i];
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BaseActivity.goToActivity(MainActivity.this, clazz);
                }
            });
            btn.setText((i + 1) + "、" + ACTIVITIE_DESC[i]);
            container.addView(btn);
        }

        System.out.print("test");
    }

    private Button createButton() {
        Button btn = new Button(this);
        btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        btn.setAllCaps(false);
        btn.setLineSpacing(1.2f, 1.2f);
        btn.setPadding(5, 30, 5, 30);
        return btn;
    }
}
