precision mediump float;
varying vec2 vTextureCoord;
uniform sampler2D uTextureSampler;
//out vec4 FragColor;
void main()
{
    gl_FragColor = texture2D(uTextureSampler, vTextureCoord);
}