#version 300 es
in vec4 a_Position;
in vec4 a_TextureCoordinates;
out vec2 v_TextureCoordinates;
uniform mat4 uTextureMatrix;
void main()
{
   v_TextureCoordinates = (uTextureMatrix * a_TextureCoordinates).xy;
   gl_Position = a_Position;
}