#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 v_texPosition;
uniform samplerExternalOES sTexture;

void main() {
    gl_FragColor=texture2D(sTexture, v_texPosition);
}