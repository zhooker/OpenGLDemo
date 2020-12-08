precision mediump float;
varying vec2 vTextureCoord;
uniform sampler2D uTextureSamplerY;
uniform sampler2D uTextureSamplerUV;

const lowp mat3 M = mat3( 1, 1, 1, 0, -.18732, 1.8556, 1.57481, -.46813, 0 );

void main()
{
    lowp vec3 yuv;
    lowp vec3 rgb;
    yuv.x = texture2D(uTextureSamplerY, vTextureCoord).r;
    yuv.yz = texture2D(uTextureSamplerUV, vTextureCoord).ar - vec2(0.5, 0.5);
    rgb = M * yuv;
    gl_FragColor = vec4(rgb, 1.0);
}