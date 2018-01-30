package com.example.opengldemo.light;

/**
 * This class implements our custom renderer. Note that the GL10 parameter passed in is unused for OpenGL ES 2.0
 * renderers -- the static class GLES30 is used instead.
 */
public class SpotLightRenderer2 extends SpotLightRenderer {
    protected String getVertexShader()
    {
        // Define our per-pixel lighting shader.
        final String perPixelVertexShader =
                          "#version 300 es                \n"
                        + "uniform mat4 u_MVPMatrix;      \n"
                        + "uniform mat4 u_MVMatrix;       \n"

                        + "in vec4 a_Position;            \n"
                        + "in vec4 a_Color;               \n"
                        + "in vec3 a_Normal;              \n"

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
                        + "   FragColor = v_Color * diffuse;                                     \n"
                        + "}                                                                     \n";

        return perPixelFragmentShader;
    }
}
