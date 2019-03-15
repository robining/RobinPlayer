precision mediump float;
varying vec2 ffPosition;
uniform sampler2D samplerTexture;

void main(){
    gl_FragColor=texture2D(samplerTexture,ffPosition);
}