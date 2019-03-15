attribute vec4 vPosition;
attribute vec2 fPosition;
varying vec2 ffPosition;
uniform mat4 uMatrix;

void main(){
    ffPosition = fPosition;
    gl_Position = vPosition*uMatrix;
}