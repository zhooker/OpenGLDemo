package com.example.opengldemo.light;

import android.content.Context;


public class LightRenderer2 extends LightRenderer {

    public LightRenderer2(Context context) {
        super(context);
    }

    @Override
    protected String getVertexShader() {
        return "light/vertex_light2.glsl";
    }

    @Override
    protected String getFragmentShader() {
        return "light/fragment_light2.glsl";
    }
}
