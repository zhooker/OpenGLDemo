package com.example.opengldemo.texture;

import android.content.Context;
import android.opengl.GLES30;

import com.example.opengldemo.R;
import com.example.opengldemo.light.LightRenderer;
import com.example.opengldemo.util.CubeUtil;
import com.example.opengldemo.util.TextureHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * This class implements our custom renderer. Note that the GL10 parameter passed in is unused for OpenGL ES 2.0
 * renderers -- the static class GLES30 is used instead.
 */
public class TextureRenderer extends LightRenderer {

    protected int textureId;
    protected int mTextureHandle;
    protected int mTextureCoordindate;
    protected final FloatBuffer mCubesTexture;

    public TextureRenderer(Context context) {
        super(context);

        final float[] texture = getTexturePosition();
        mCubesTexture = ByteBuffer.allocateDirect(texture.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubesTexture.put(texture).position(0);
    }

    protected String getVertexShader() {
        return "texture/vertex_texture.glsl";
    }

    protected String getFragmentShader() {
        return "texture/fragment_texture.glsl";
    }

    protected float[] getTexturePosition() {
        return CubeUtil.cubeTextureData;
    }

    protected int getTextureId() {
        return R.drawable.wall;
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        super.onSurfaceCreated(glUnused, config);
        // init textureId
        textureId = TextureHelper.loadTexture(mContext, getTextureId());
    }

    @Override
    protected void initHandlers() {
        super.initHandlers();
        mTextureCoordindate = 3;//GLES30.glGetAttribLocation(mPerVertexProgramHandle, "a_TextureCoordinates");
        mTextureHandle = GLES30.glGetUniformLocation(mPerVertexProgramHandle, "u_TextureUnit");
    }

    @Override
    protected void drawCube() {
        // Pass in the Texture information
        mCubesTexture.position(0);
        GLES30.glVertexAttribPointer(mTextureCoordindate, 2, GLES30.GL_FLOAT, false,
                0, mCubesTexture);
        GLES30.glEnableVertexAttribArray(mTextureCoordindate);


        // Set the active textureId unit to textureId unit 0.
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);
        GLES30.glUniform1i(mTextureHandle, 0);

        super.drawCube();
    }
}
