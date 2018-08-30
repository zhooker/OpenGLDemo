#version 300 es
uniform mat4 u_MVPMatrix;
uniform mat4 u_MVMatrix;

layout (location = 0) in vec4 a_Position;
layout (location = 1) in vec4 a_Color;
layout (location = 2) in vec3 a_Normal;
layout (location = 3) in vec2 a_TextureCoordinates;
out vec2 v_TextureCoordinates;
out vec3 v_Position;
out vec4 v_Color;
out vec3 v_Normal;
void main()
{
   v_Position = vec3(u_MVMatrix * a_Position);
   v_Color = a_Color;
   v_Normal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));
   gl_Position = u_MVPMatrix * a_Position;
   v_TextureCoordinates = a_TextureCoordinates;
}