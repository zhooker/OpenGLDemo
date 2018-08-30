#version 300 es
precision mediump float;
uniform vec3 u_LightPos;

in vec3 v_Position;
in vec4 v_Color;
in vec3 v_Normal;
uniform sampler2D u_TextureUnit;
in vec2 v_TextureCoordinates;
out vec4 FragColor;
void main()
{
   float distance = length(u_LightPos - v_Position);
   vec3 lightVector = normalize(u_LightPos - v_Position);
   float diffuse = max(dot(v_Normal, lightVector), 0.1);
   diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance * distance)));
   FragColor = mix(texture(u_TextureUnit, v_TextureCoordinates), v_Color * diffuse,0.8);
}