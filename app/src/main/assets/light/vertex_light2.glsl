#version 300 es
uniform mat4 u_MVPMatrix;
uniform mat4 u_MVMatrix;

in vec4 a_Position;
in vec4 a_Color;
in vec3 a_Normal;

out vec3 v_Position;
out vec4 v_Color;
out vec3 v_Normal;
void main()
{
   v_Position = vec3(u_MVMatrix * a_Position);
   v_Color = a_Color;
   v_Normal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));
   gl_Position = u_MVPMatrix * a_Position;
}