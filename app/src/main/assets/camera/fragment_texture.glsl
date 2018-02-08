#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 vTextureCoord;
uniform samplerExternalOES uTextureSampler;
uniform sampler2D uTextureSampler0;
//out vec4 FragColor;
void main()
{
    vec3 edge = texture2D(uTextureSampler0, vTextureCoord).rgb;
    vec4 camera = texture2D(uTextureSampler,vTextureCoord);
    gl_FragColor = camera * vec4(edge, 1.0);
}