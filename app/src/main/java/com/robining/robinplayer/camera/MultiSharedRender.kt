package com.robining.robinplayer.camera

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.view.Surface
import java.lang.IllegalStateException
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.opengles.GL10

open class MultiSharedRender(private val context: Context, renders: Array<GLSurfaceView.Renderer>, private val sharedSurfaces: Array<Surface>) : MultiRender(renders) {
    private val sharedRGLSurfaceView = Array<RGLSurfaceView?>(sharedSurfaces.size) { null }
    private var fboId: Int? = null
    private var fboTextureId: Int? = null
    private var fboRender: GLSurfaceView.Renderer? = null

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        super.onSurfaceCreated(gl, config)
        fboTextureId = GlesUtil.createTexture(null)
        fboId = GlesUtil.createFboBuffer(fboTextureId!!)
        fboRender = createFboRender(context, fboTextureId!!)
        fboRender?.onSurfaceCreated(gl, config)

        val egl = EGLContext.getEGL() as EGL10
        val eglContext = egl.eglGetCurrentContext()
        if(eglContext == EGL10.EGL_NO_CONTEXT){
            throw IllegalStateException("cannot got current egl context")
        }
        for ((index, surface) in sharedSurfaces.withIndex()) {
            sharedRGLSurfaceView[index] = RGLSurfaceView(surface, eglContext, context, null)
            val render = WithTextureRender(context, fboTextureId!!)
            render.tagName = "SharedRender$index"
            sharedRGLSurfaceView[index]!!.setRenderer(render)
            sharedRGLSurfaceView[index]!!.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
            sharedRGLSurfaceView[index]!!.surfaceCreated(null)
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        super.onSurfaceChanged(gl, width, height)
        fboRender?.onSurfaceChanged(gl, width, height)
        for (surfaceView in sharedRGLSurfaceView) {
            surfaceView?.surfaceChanged(null, 0, width, height)
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId!!)
        super.onDrawFrame(gl)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)

        fboRender?.onDrawFrame(gl)
        for (surfaceView in sharedRGLSurfaceView) {
            surfaceView?.requestRender()
        }
    }

    override fun onSurfaceDestroyed() {
        for (surfaceView in sharedRGLSurfaceView) {
            surfaceView?.surfaceDestroyed(null)
        }
    }

    open fun createFboRender(context: Context, fboTextureId: Int): GLSurfaceView.Renderer {
        val render = WithTextureRender(context, fboTextureId)
        render.tagName = "FboRender"
        return render
    }
}