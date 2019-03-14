package com.robining.robinplayer.camera

import android.opengl.GLSurfaceView

interface DestroyableGLRender : GLSurfaceView.Renderer {
    fun onSurfaceDestroyed()
}