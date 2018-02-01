package com.example.opengldemo.texture;

import android.content.Context;
import android.opengl.GLES30;
import com.example.opengldemo.R;
import com.example.opengldemo.light.SpotLightRenderer;
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
public class TextureRenderer extends SpotLightRenderer {

    private Context mContext;

    private int textureId;
    private int mTextureHandle;
    private int mTextureCoordindate;
    private final FloatBuffer mCubesTexture;

    public TextureRenderer(Context context) {
        super();
        // init textureId
        mCubesTexture = ByteBuffer.allocateDirect(CubeUtil.cubeTextureData.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubesTexture.put(CubeUtil.cubeTextureData).position(0);

        this.mContext = context;
    }

    protected String getVertexShader()
    {
        // Define our per-pixel lighting shader.
        final String perPixelVertexShader =
                          "#version 300 es                \n"
                        + "uniform mat4 u_MVPMatrix;      \n"
                        + "uniform mat4 u_MVMatrix;       \n"

                        + "layout (location = 0) in vec4 a_Position;            \n"
                        + "layout (location = 1) in vec4 a_Color;               \n"
                        + "layout (location = 2) in vec3 a_Normal;              \n"
                        + "layout (location = 3) in vec2 a_TextureCoordinates;  \n"
                        + "out vec2 v_TextureCoordinates; \n"
                        + "out vec3 v_Position;           \n"
                        + "out vec4 v_Color;              \n"
                        + "out vec3 v_Normal;             \n"

                        // The entry point for our vertex shader.
                        + "void main()                                                \n"
                        + "{                                                          \n"
                        // Transform the vertex into eye space.
                        + "   v_Position = vec3(u_MVMatrix * a_Position);             \n"
                        // Pass through the color.
                        + "   v_Color = a_Color;                                      \n"
                        // Transform the normal's orientation into eye space.
                        + "   v_Normal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));      \n"
                        // gl_Position is a special variable used to store the final position.
                        // Multiply the vertex by the matrix to get the final point in normalized screen coordinates.
                        + "   gl_Position = u_MVPMatrix * a_Position;                 \n"
                                  + "   v_TextureCoordinates = a_TextureCoordinates;                            \n"
                        + "}                                                          \n";

        return perPixelVertexShader;
    }

    protected String getFragmentShader()
    {
        final String perPixelFragmentShader =
                          "#version 300 es                \n"
                        + "precision mediump float;       \n"
                        + "uniform vec3 u_LightPos;       \n"

                        + "in vec3 v_Position;		      \n"
                        + "in vec4 v_Color;               \n"
                        + "in vec3 v_Normal;              \n"
                                  + "uniform sampler2D u_TextureUnit;         \n"
                                  + "in vec2 v_TextureCoordinates;            \n"
                        + "out vec4 FragColor;            \n"
                        + "void main()                    \n"
                        + "{                              \n"
                        // Will be used for attenuation.
                        + "   float distance = length(u_LightPos - v_Position);                  \n"
                        // Get a lighting direction vector from the light to the vertex.
                        + "   vec3 lightVector = normalize(u_LightPos - v_Position);             \n"
                        // Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
                        // pointing in the same direction then it will get max illumination.
                        + "   float diffuse = max(dot(v_Normal, lightVector), 0.1);              \n"
                        // Add attenuation.
                        + "   diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance * distance)));  \n"
                        // Multiply the color by the diffuse illumination level to get final output color.
                        + "   FragColor = mix(texture(u_TextureUnit, v_TextureCoordinates),v_Color * diffuse,0.8); \n"
                        + "}                                                                     \n";

        return perPixelFragmentShader;
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        super.onSurfaceCreated(glUnused, config);
        // init textureId
        textureId = TextureHelper.loadTexture(mContext, R.drawable.wall);
    }

    @Override
    protected void initHandles() {
        super.initHandles();
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
