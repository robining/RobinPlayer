package com.robining.robinplayer.camera

import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

open class MultiRender(private val renders: Array<GLSurfaceView.Renderer>) : DestroyableGLRender {
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        for (render in renders) {
            render.onSurfaceCreated(gl, config)
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        for (render in renders) {
            render.onSurfaceChanged(gl, width, height)
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        for (render in renders) {
            render.onDrawFrame(gl)
        }
    }

    override fun onSurfaceDestroyed() {
        for (render in renders) {
            if (render is DestroyableGLRender) {
                render.onSurfaceDestroyed()
            }
        }
    }
}