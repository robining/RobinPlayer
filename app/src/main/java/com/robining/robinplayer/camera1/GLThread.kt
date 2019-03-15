package com.robining.robinplayer.camera1

import android.view.Surface
import java.lang.ref.WeakReference
import javax.microedition.khronos.egl.EGLContext

class GLThread(
        private val glRenderRef: WeakReference<RGLRender>,
        private val surface: Surface?,
        private val eglContext: EGLContext?
) :
    Thread() {
    private var isDestroyed = false
    private var isChanged = false
    private var width: Int = 0
    private var height: Int = 0
    private var eglContextHelper = EglContextHelper()
    private val lock = Object()
    private var isStarted = false
    private var renderMode: RenderMode = RenderMode.RENDERMODE_CONTINUOUSLY

    override fun run() {
        super.run()
        if (glRenderRef.get() != null && surface != null) {
            eglContextHelper.init(surface, eglContext)
            glRenderRef.get()!!.onSurfaceCreated(eglContextHelper.currentEglContext!!)
        } else {
            return
        }

        while (!isDestroyed) {
            val glRender = glRenderRef.get()
            if (glRender == null) {
                isDestroyed = true
                break
            }

            if (isChanged) {
                isChanged = false
                glRender.onSurfaceChanged(width, height)
            }

            if (renderMode == RenderMode.RENDERMODE_WHEN_DIRTY) {
                try {
                    synchronized(lock) {
                        println(">>>start wait lock notify")
                        lock.wait()
                        println(">>>lock notify")
                    }
                } catch (ex: InterruptedException) {
                }
            } else {
                try {
                    Thread.sleep(1000 / 60)
                } catch (ex: InterruptedException) {
                }
            }


            glRender.onDrawFrame()
            eglContextHelper.swapBuffers()

            isStarted = true
        }

        eglContextHelper.destoryEGL()
        glRenderRef.get()?.onSurfaceDestroyed()
    }

    fun onChanged(width: Int, height: Int) {
        isChanged = true
        this.width = width
        this.height = height
    }

    fun exit() {
        isDestroyed = true
    }

    fun requestRender() {
        synchronized(lock) {
            println(">>>notify all lock")
            lock.notifyAll()
        }
    }

    fun setRenderMode(renderMode: RenderMode) {
        this.renderMode = renderMode
    }
}