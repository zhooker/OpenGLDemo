uniform mat4 uTextureMatrix;
attribute vec4 a_Position;
attribute vec4 aTextureCoordinate;
varying vec2 vTextureCoord;
void main()
{
    vTextureCoord = (uTextureMatrix * aTextureCoordinate).xy;
 	gl_Position = a_Position;
}