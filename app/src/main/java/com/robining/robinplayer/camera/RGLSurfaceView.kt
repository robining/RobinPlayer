package com.robining.robinplayer.camera

import android.content.Context
import android.opengl.EGL14.EGL_CONTEXT_CLIENT_VERSION
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.Surface
import android.view.SurfaceHolder
import javax.microedition.khronos.egl.*

class RGLSurfaceView(private val surface: Surface?, private val eglContext: EGLContext?, context: Context, attrs: AttributeSet?) : GLSurfaceView(context, attrs) {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(null, null, context, attrs)

    private val mEGLContextClientVersion = 2
    private var currentRender: Renderer? = null

    init {
        setEGLContextClientVersion(mEGLContextClientVersion)
        if (eglContext != null) {
            setEGLContextFactory(object : GLSurfaceView.EGLContextFactory {
                override fun createContext(egl: EGL10?, display: EGLDisplay?, eglConfig: EGLConfig?): EGLContext {
                    val attribList = intArrayOf(EGL_CONTEXT_CLIENT_VERSION, mEGLContextClientVersion, EGL10.EGL_NONE)
                    println(">>>create egl context with:$eglContext ,is no context:${eglContext == EGL10.EGL_NO_CONTEXT}")
                    return egl?.eglCreateContext(display, eglConfig, eglContext,
                            attribList)!!
                }

                override fun destroyContext(egl: EGL10?, display: EGLDisplay?, context: EGLContext?) {
                    egl?.eglDestroyContext(display, context)
                }

            })
        }

        if (surface != null) {
            setEGLWindowSurfaceFactory(object : GLSurfaceView.EGLWindowSurfaceFactory {
                override fun createWindowSurface(egl: EGL10?, display: EGLDisplay?, config: EGLConfig?, nativeWindow: Any?): EGLSurface {
                    println(">>>create window surface with:$surface")
                    return egl?.eglCreateWindowSurface(display, config, surface, null)!!
                }

                override fun destroySurface(egl: EGL10?, display: EGLDisplay?, surface: EGLSurface?) {
                    egl?.eglDestroySurface(display, surface)
                }

            })
        }
    }

    override fun setRenderer(renderer: Renderer?) {
        super.setRenderer(renderer)
        this.currentRender = renderer
    }

    fun getCurrentRender(): GLSurfaceView.Renderer? {
        return currentRender
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        super.surfaceDestroyed(holder)
        if (currentRender != null && currentRender is DestroyableGLRender) {
            (currentRender as DestroyableGLRender).onSurfaceDestroyed()
        }
    }
}