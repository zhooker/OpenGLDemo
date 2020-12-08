#version 300 es
precision mediump float;
in vec4 v_Color;
out vec4 FragColor;
//layout(location = 0) out vec4 frag_color;
void main()
{
   FragColor = v_Color;
}