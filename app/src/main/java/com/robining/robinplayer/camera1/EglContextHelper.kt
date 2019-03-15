package com.robining.robinplayer.camera1

import android.view.Surface
import android.opengl.EGL14
import android.opengl.GLES20
import android.util.Log
import javax.microedition.khronos.egl.*


class EglContextHelper {
    var egl: EGL10? = null
    var currentEglContext: EGLContext? = null
    var currentSurface: EGLSurface? = null
    var currentDisplay: EGLDisplay? = null
    var currentGlConfig: EGLConfig? = null

    fun init(surface: Surface, eglContext: EGLContext?) {
        //get egl instance
        egl = EGLContext.getEGL() as EGL10

        //get default display
        currentDisplay = egl!!.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
        if (currentDisplay == EGL10.EGL_NO_DISPLAY) {
            throw IllegalStateException("cannot get default display")
        }

        //init default display
        val version = IntArray(2)
        if (!egl!!.eglInitialize(currentDisplay, version)) {
            throw IllegalStateException("cannot init default display")
        }

        //set display attr
        val attrbutes = intArrayOf(
            EGL10.EGL_RED_SIZE,
            8,
            EGL10.EGL_GREEN_SIZE,
            8,
            EGL10.EGL_BLUE_SIZE,
            8,
            EGL10.EGL_ALPHA_SIZE,
            8,
            EGL10.EGL_DEPTH_SIZE,
            8,
            EGL10.EGL_STENCIL_SIZE,
            8,
            EGL10.EGL_RENDERABLE_TYPE,
            4,
            EGL10.EGL_NONE
        )
        val num_configs = IntArray(1)
        if (!egl!!.eglChooseConfig(currentDisplay, attrbutes, null, 1, num_configs)) {
            throw IllegalStateException("cannot set display attr")
        }

        val numConfig = num_configs[0]
        if (numConfig <= 0) {
            throw IllegalStateException("no config match")
        }

        //reset display attr by system config
        val eglConfigs = arrayOfNulls<EGLConfig>(numConfig)
        if (!egl!!.eglChooseConfig(currentDisplay, attrbutes, eglConfigs, numConfig, num_configs)) {
            throw IllegalStateException("cannot set display attr with system config")
        }
        currentGlConfig = eglConfigs[0]

        val attrib_list = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE)
        if (eglContext == null) {
            currentEglContext =
                egl!!.eglCreateContext(currentDisplay, currentGlConfig, EGL10.EGL_NO_CONTEXT, attrib_list)
        } else {
            currentEglContext = egl!!.eglCreateContext(currentDisplay, currentGlConfig, eglContext, attrib_list)
        }

        Log.e(".","create egl context:$eglContext  to $currentEglContext")

        currentSurface = egl!!.eglCreateWindowSurface(currentDisplay, currentGlConfig, surface, null)

        if (!egl!!.eglMakeCurrent(currentDisplay, currentSurface, currentSurface, currentEglContext)) {
            throw IllegalStateException("egl make failed")
        }
    }

    fun swapBuffers() {
        egl?.eglSwapBuffers(currentDisplay, currentSurface)
    }

    fun destoryEGL() {
        egl?.eglMakeCurrent(currentDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT)
        egl?.eglDestroySurface(currentDisplay, currentSurface)
        currentSurface = null
        egl?.eglDestroyContext(currentDisplay, currentEglContext)
        currentEglContext = null
        egl?.eglTerminate(currentDisplay)
        currentDisplay = null
        egl = null
    }
}