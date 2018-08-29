#version 300 es
uniform mat4 u_MVPMatrix;
in vec4 a_Position;
in vec4 a_Color;
out vec4 v_Color;
void main()
{
   v_Color = a_Color;
   gl_Position = u_MVPMatrix * a_Position;
}