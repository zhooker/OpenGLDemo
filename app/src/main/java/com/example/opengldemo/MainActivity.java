package com.example.opengldemo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.opengldemo.base.BaseActivity;
import com.example.opengldemo.basic.BasicActivity;
import com.example.opengldemo.blend.BlendActivity;
import com.example.opengldemo.camera.CameraActivity;
import com.example.opengldemo.camera2.Camera2Activity;
import com.example.opengldemo.demo.CameraPreviewActivity;
import com.example.opengldemo.demo.CubeActivity;
import com.example.opengldemo.demo.SimpleSquareActivity;
import com.example.opengldemo.demo.ProjectionActivity;
import com.example.opengldemo.demo.SimpleActivity;
import com.example.opengldemo.filter.CameraFilterActivity;
import com.example.opengldemo.light.SpotLightActivity;
import com.example.opengldemo.light.SpotLightActivity2;
import com.example.opengldemo.ndk.NativeEGLActivity;
import com.example.opengldemo.player.PlayerActivity;
import com.example.opengldemo.save.SaveActivity;
import com.example.opengldemo.solar.system.SolarActivity;
import com.example.opengldemo.test.stencil.StencilActivity;
import com.example.opengldemo.texture.TextureActivity;
import com.example.opengldemo.uniformblock.UniformBlockActivity;
import com.example.opengldemo.util.L;
import com.example.opengldemo.vao.VAOActivity;

public class MainActivity extends BaseActivity {

    protected static final int REQUEST_CAMERA = 0;

    private static final Pair<Class<? extends BaseActivity>, String>[] ACTIVITIES = new Pair[]{
            new Pair(SimpleActivity.class, "SimpleActivity"),
            new Pair(SimpleSquareActivity.class, "SimpleSquareActivity"),
            new Pair(ProjectionActivity.class, "ProjectionActivity"),
            new Pair(CubeActivity.class, "CubeActivity"),
            new Pair(com.example.opengldemo.demo.TextureActivity.class, "TextureActivity"),
            new Pair(CameraPreviewActivity.class, "CameraPreviewActivity"),
            new Pair(BasicActivity.class, "OpenGL基础"),
            new Pair(NativeEGLActivity.class, "使用NDK的方式使用 OpenGL"),
            new Pair(SpotLightActivity.class, "使用顶点着色器渲染光照"),
            new Pair(SpotLightActivity2.class, "使用片段着色器渲染光照"),
            new Pair(TextureActivity.class, "使用纹理贴图"),
            new Pair(SolarActivity.class, "太阳系模型"),
            new Pair(BlendActivity.class, "混合示例"),
            new Pair(StencilActivity.class, "模版测试"),
            new Pair(VAOActivity.class, "使用 VAO、VBO、EBO的例子"),
            new Pair(UniformBlockActivity.class, "使用Uniform Block"),
            new Pair(SaveActivity.class, "利用Opengl保存图片,包括 FBO"),
            new Pair(CameraFilterActivity.class, "使用Camera实现简单的滤镜效果"),
            new Pair(CameraActivity.class, "使用camera实现预览"),
            new Pair(Camera2Activity.class, "使用camera2实现预览"),
            new Pair(PlayerActivity.class, "IJKPlayer测试"),
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("OpenGL ES 3.0 Demo");
        initData();
        requestCameraPermission();
    }

    public void onTest(View v) {

    }

    private void initData() {
        LinearLayout container = (LinearLayout) findViewById(R.id.btn_container);
        for (int i = 0; i < ACTIVITIES.length; i++) {
            Button btn = createButton();
            final Class<? extends Activity> clazz = ACTIVITIES[i].first;
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BaseActivity.goToActivity(MainActivity.this, clazz);
                }
            });
            btn.setText(String.format("%1$2d", i + 1) + "、" + clazz.getSimpleName().replace("Activity", "") + " Demo\n\t\t\t\t" + ACTIVITIES[i].second);
            container.addView(btn);
        }
    }

    private Button createButton() {
        Button btn = new Button(this);
        btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        btn.setAllCaps(false);
        btn.setGravity(Gravity.LEFT);
        btn.setLineSpacing(1.2f, 1.2f);
        btn.setPadding(20, 30, 20, 30);
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
