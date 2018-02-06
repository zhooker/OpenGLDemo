uniform mat4 uTextureMatrix;
uniform mat4 u_MVPMatrix;
attribute vec4 a_Position;
attribute vec4 aTextureCoordinate;
varying vec2 vTextureCoord;
void main()
{
    vTextureCoord = (uTextureMatrix * aTextureCoordinate).xy;
 	gl_Position = a_Position;
}