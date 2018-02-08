attribute vec4 a_Position;
attribute vec4 a_Color;
varying vec4 vColor;
void main()
{
    vColor = a_Color;
    // 因为我用了cube的数据，把矩形画在了1.0的位置，加了深度测试后就不可见了
 	gl_Position = vec4(a_Position.x,a_Position.y,a_Position.z-1.0,a_Position.w);
}