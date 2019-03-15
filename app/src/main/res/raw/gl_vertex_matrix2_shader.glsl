attribute vec4 vPosition;
attribute vec2 fPosition;
varying vec2 ffPosition;
uniform mat4 vMatrix;
uniform mat4 fMatrix;

void main(){
    ffPosition = fPosition;
    gl_Position = vPosition*vMatrix*fMatrix;
}