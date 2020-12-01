#version 300 es
precision mediump float;
in vec4 v_Color;
out vec4 FragColor;
uniform sampler2D u_TextureUnit;
in vec2 v_TextureCoordinates;
void main()
{
   FragColor = texture(u_TextureUnit, v_TextureCoordinates);
}