precision highp float;
varying highp vec2 vTextureCoord;
uniform sampler2D uTextureSampler;
uniform sampler2D uTextureSampler0;
//out vec4 FragColor;
void main()
{
//    gl_FragColor = texture2D(uTextureSampler0,vTextureCoord);

         lowp vec4 originColor = texture2D(uTextureSampler, vTextureCoord);
         mediump float blueColor = originColor.b * 63.0;

         mediump vec2 quad1;
         quad1.y = floor(floor(blueColor) / 8.0);
         quad1.x = floor(blueColor) - (quad1.y * 8.0);

         mediump vec2 quad2;
         quad2.y = floor(ceil(blueColor) / 8.0);
         quad2.x = ceil(blueColor) - (quad2.y * 8.0);

         highp vec2 texPos1;
         texPos1.x = (quad1.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * originColor.r);
         texPos1.y = (quad1.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * originColor.g);

         highp vec2 texPos2;
         texPos2.x = (quad2.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * originColor.r);
         texPos2.y = (quad2.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * originColor.g);

         lowp vec4 newColor1 = texture2D(uTextureSampler0, texPos1);
         lowp vec4 newColor2 = texture2D(uTextureSampler0, texPos2);

         lowp vec4 newColor = mix(newColor1, newColor2, fract(blueColor));

         gl_FragColor = vec4(mix(originColor.rgb, newColor.rgb, 1.0).rgb, originColor.w);
}