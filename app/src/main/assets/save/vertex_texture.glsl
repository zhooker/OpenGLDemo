uniform mat4 u_VPMatrix;
uniform mat4 u_ModelMatrix;
attribute vec4 a_Position;
attribute vec2 aTextureCoordinate;
varying vec2 vTextureCoord;
void main()
{
    vTextureCoord = aTextureCoordinate;
 	gl_Position = u_VPMatrix * u_ModelMatrix * a_Position;
}