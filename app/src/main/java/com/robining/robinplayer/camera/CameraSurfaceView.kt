package com.robining.robinplayer.camera

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CameraSurfaceView(context: Context,attr:AttributeSet) : GLSurfaceView(context,attr) {
    init {
//        setRenderer(object : Renderer{
//            override fun onDrawFrame(gl: GL10?) {
//
//            }
//
//            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
//            }
//
//            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
//            }
//
//        })
        setRenderer(null)

        renderMode = RENDERMODE_CONTINUOUSLY
    }
}