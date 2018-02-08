package com.example.opengldemo;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.opengldemo.basic.BasicActivity;
import com.example.opengldemo.camera.CameraActivity;
import com.example.opengldemo.camera2.Camera2Activity;
import com.example.opengldemo.camera3.Camera3Activity;
import com.example.opengldemo.light.SpotLightActivity;
import com.example.opengldemo.light.SpotLightActivity2;
import com.example.opengldemo.ndk.NativeEGLActivity;
import com.example.opengldemo.save.SaveActivity;
import com.example.opengldemo.test.stencil.StencilActivity;
import com.example.opengldemo.texture.TextureActivity;
import com.example.opengldemo.uniformblock.UniformBlockActivity;
import com.example.opengldemo.util.BaseActivity;

public class MainActivity extends AppCompatActivity {

    private static final Class<? extends BaseActivity>[] ACTIVITIES = new Class[]{
            BasicActivity.class,
            NativeEGLActivity.class,
            SpotLightActivity.class,
            SpotLightActivity2.class,
            TextureActivity.class,
            UniformBlockActivity.class,
            StencilActivity.class,
            SaveActivity.class,
            CameraActivity.class,
            Camera2Activity.class,
            Camera3Activity.class
    };

    private static final String[] ACTIVITIE_DESC = new String[]{
            "Basic Demo\n  OpenGL基础",
            "NDK Demo\n使用NDK的方式使用 OpenGL",
            "Spot Light Demo\n使用顶点着色器渲染光照",
            "Spot Light Demo 2\n使用片段着色器渲染光照",
            "Texture Demo\n使用纹理贴图",
            "Uniform Block Demo\n使用Uniform Block",
            "Stencil testing Demo\n模版测试",
            "Save Image Demo\n利用Opengl保存图片,包括 FBO",
            "Camera Preview Demo\n使用Camera渲染预览显示",
            "Camera Demo\n通过Shader实现YUV转换RBG",
            "Camera2 Demo\n使用camera2, 通过Shader实现YUV转换RBG"
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
