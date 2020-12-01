#version 300 es
uniform mat4 u_MVPMatrix;
in vec4 a_Position;
in vec4 a_Color;
out vec4 v_Color;
in vec2 a_TextureCoordinates;
out vec2 v_TextureCoordinates;
void main()
{
   v_Color = a_Color;
   v_TextureCoordinates = a_TextureCoordinates;
   gl_Position = u_MVPMatrix * a_Position;
}