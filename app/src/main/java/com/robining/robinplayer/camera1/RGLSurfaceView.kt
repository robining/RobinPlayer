package com.robining.robinplayer.camera1

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.lang.ref.WeakReference
import javax.microedition.khronos.egl.EGLContext

class RGLSurfaceView(context: Context, attrs: AttributeSet?, defStyle: Int) : SurfaceView(context, attrs, defStyle),
    SurfaceHolder.Callback, RGLRender {

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private val TAG = "RGLSurfaceView"
    private var glThread: GLThread? = null
    private val extraSurfaces: ArrayList<Surface> = arrayListOf()
    private val extraSharedRenders: ArrayList<SharedRender> = arrayListOf()
    private var renders: Array<GLSurfaceView.Renderer>? = null
    private var renderMode: RenderMode = RenderMode.RENDERMODE_CONTINUOUSLY
    private var textureId: Int? = null
    private var fboId: Int? = null
    private var fboRender: FboRender? = null

    init {
        holder.addCallback(this)
    }

    fun getExtraSurfaces(): ArrayList<Surface> {
        return extraSurfaces
    }

    fun setRenders(renders: Array<GLSurfaceView.Renderer>) {
        if (glThread != null) {
            throw IllegalStateException("need call serRenders before surfaceCreated")
        }
        this.renders = renders
    }

    fun requestRender() {
        glThread?.requestRender()
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        glThread?.onChanged(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        glThread?.exit()
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        glThread = GLThread(WeakReference(this), holder!!.surface, null)
        glThread!!.setRenderMode(renderMode)
        glThread!!.start()
    }

    override fun onSurfaceDestroyed() {
        for (sharedRender in extraSharedRenders) {
            sharedRender.glThread!!.exit()
        }
    }

    override fun onDrawFrame() {
        //添加离屏渲染
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId!!)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        if (renders != null) {
            for (render in renders!!) {
                render.onDrawFrame(null)
            }
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)

        //绘制到本地窗口
        fboRender!!.onDrawFrame()

        //绘制到共享Render
        for (sharedRender in extraSharedRenders) {
            sharedRender.glThread!!.requestRender()
        }
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        fboRender!!.onSurfaceChanged(width,height)

        if (renders != null) {
            for (render in renders!!) {
                render.onSurfaceChanged(null, width, height)
            }
        }

        for (sharedRender in extraSharedRenders) {
            sharedRender.glThread!!.onChanged(width, height)
        }
    }

    override fun onSurfaceCreated(eglContext: EGLContext) {
        textureId = GlesUtil.createTexture(null)
        fboId = GlesUtil.createFboBuffer(textureId!!)

        fboRender = FboRender(context, textureId!!)
        fboRender!!.onSurfaceCreated(eglContext)

        extraSharedRenders.clear()
        for (surface in extraSurfaces) {
            val sharedRender = SharedRender(context, textureId!!)
            extraSharedRenders.add(sharedRender)
            sharedRender.initGLThread(surface, eglContext, RenderMode.RENDERMODE_WHEN_DIRTY)
        }

        if (renders != null) {
            for (render in renders!!) {
                render.onSurfaceCreated(null, null)
            }
        }
    }

    fun setRenderMode(renderMode: RenderMode) {
        this.renderMode = renderMode
        glThread?.setRenderMode(renderMode)
    }
}