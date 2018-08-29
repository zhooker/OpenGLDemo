package com.example.opengldemo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.opengldemo.base.BaseActivity;
import com.example.opengldemo.basic.BasicActivity;
import com.example.opengldemo.blend.BlendActivity;
import com.example.opengldemo.camera.CameraActivity;
import com.example.opengldemo.camera2.Camera2Activity;
import com.example.opengldemo.filter.CameraFilterActivity;
import com.example.opengldemo.light.SpotLightActivity;
import com.example.opengldemo.light.SpotLightActivity2;
import com.example.opengldemo.ndk.NativeEGLActivity;
import com.example.opengldemo.save.SaveActivity;
import com.example.opengldemo.test.stencil.StencilActivity;
import com.example.opengldemo.texture.TextureActivity;
import com.example.opengldemo.uniformblock.UniformBlockActivity;
import com.example.opengldemo.util.L;
import com.example.opengldemo.vao.VAOActivity;

public class MainActivity extends AppCompatActivity {

    protected static final int REQUEST_CAMERA = 0;

    private static final Class<? extends BaseActivity>[] ACTIVITIES = new Class[]{
            BasicActivity.class,
            NativeEGLActivity.class,
            SpotLightActivity.class,
            SpotLightActivity2.class,
            TextureActivity.class,
            BlendActivity.class,
            VAOActivity.class,
            UniformBlockActivity.class,
            StencilActivity.class,
            SaveActivity.class,
            CameraFilterActivity.class,
            CameraActivity.class,
            Camera2Activity.class
    };

    private static final String[] ACTIVITIE_DESC = new String[]{
            "Basic Demo\n  OpenGL基础",
            "NDK Demo\n使用NDK的方式使用 OpenGL",
            "Spot Light Demo\n使用顶点着色器渲染光照",
            "Spot Light Demo 2\n使用片段着色器渲染光照",
            "Texture Demo\n使用纹理贴图",
            "Blend Demo\n混合示例",
            "VAO Demo\n使用 VAO、VBO、EBO的例子",
            "Uniform Block Demo\n使用Uniform Block",
            "Stencil testing Demo\n模版测试",
            "Save Image Demo\n利用Opengl保存图片,包括 FBO",
            "Camera Filter Demo\n使用Camera实现简单的滤镜效果",
            "Camera Demo\n使用camera实现预览",
            "Camera2 Demo\n使用camera2实现预览"
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

        requestCameraPermission();
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

    protected void requestCameraPermission() {
        String[] requests = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        for (String request : requests) {
            if (ContextCompat.checkSelfPermission(this, request) != PackageManager.PERMISSION_GRANTED) {
                L.d(request + "权限未被授予，需要申请！");
                ActivityCompat.requestPermissions(this, requests, REQUEST_CAMERA);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    L.d("相机权限申请失败，退出程序！");
                    finish();
                    return;
                }
            }
        }
    }
}
