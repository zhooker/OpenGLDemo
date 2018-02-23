precision mediump float;
varying vec2 vTextureCoord;
uniform sampler2D uTextureSampler;
uniform sampler2D uTextureSampler1;
uniform sampler2D uTextureSampler2;
//out vec4 FragColor;
const mat3 yuv2rgb = mat3(
    1, 0, 1.402,
    1, -0.34414, -0.71414,
    1, 1.1772, 0
);

void main()
{
//    vec3 yuv = vec3(
//        texture2D(uTextureSampler, vTextureCoord).g,
//        texture2D(uTextureSampler1, vTextureCoord).g,
//        texture2D(uTextureSampler2, vTextureCoord).g
//    );
//    vec3 rgb =  yuv2rgb*yuv;
//    gl_FragColor = vec4(rgb, 1);
    gl_FragColor = texture2D(uTextureSampler,vTextureCoord);
}