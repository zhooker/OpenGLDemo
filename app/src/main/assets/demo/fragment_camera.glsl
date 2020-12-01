#version 300 es
#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES u_TextureUnit;
in vec2 v_TextureCoordinates;
out vec4 FragColor;
void main()
{
   FragColor = texture(u_TextureUnit, v_TextureCoordinates);
}