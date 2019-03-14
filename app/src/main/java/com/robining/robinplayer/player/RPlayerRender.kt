package com.robining.robinplayer.player

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.Surface
import com.robining.robinplayer.R

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class RPlayerRender(private val context: Context) : GLSurfaceView.Renderer {
    private val MODE_YUV = 1
    private val MODE_MEDIACODEC = 2

    private val TAG = "RPlayerRender"
    private val vertexData = floatArrayOf(

            -1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f)

    private val textureData = floatArrayOf(0f, 1f, 1f, 1f, 0f, 0f, 1f, 0f)

    private val vertexBuffer: FloatBuffer
    private val textureBuffer: FloatBuffer

    private var program_yuv: Int = 0
    private var avPosition_yuv: Int = 0
    private var afPosition_yuv: Int = 0
    private var sampler_y: Int = 0
    private var sampler_u: Int = 0
    private var sampler_v: Int = 0
    private var textureId_yuv: IntArray? = null
    private var width_yuv: Int = 0
    private var height_yuv: Int = 0
    private var y: ByteBuffer? = null
    private var u: ByteBuffer? = null
    private var v: ByteBuffer? = null

    private var program_mediacodec: Int = 0
    private var avPosition_mediacodec: Int = 0
    private var afPosition_mediacodec: Int = 0
    private var sampler_mediacodec: Int = 0
    private var textureId_mediacodec: Int = 0
    private var surfaceTexture: SurfaceTexture? = null

    private var maxWidth: Int = 0
    private var maxHeight: Int = 0
    private var mode = MODE_YUV
    var onSurfaceCreatedLisenter: OnSurfaceCreatedListener? = null

    init {
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData)
        vertexBuffer.position(0)

        textureBuffer = ByteBuffer.allocateDirect(textureData.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureData)
        textureBuffer.position(0)
    }

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        Log.d(TAG, "onSurfaceCreated")
        installYUVSupport()
        installMediaCodecSupport(null) //必须在onSurfaceCreated之后安装，否则不能渲染
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceChanged")
        maxWidth = width
        maxHeight = height
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(unused: GL10) {
        Log.d(TAG, "onDrawFrame")
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0f, 0f, 0f, 1.0f)

        if (mode == MODE_YUV) {
            renderYUV()
        } else if (mode == MODE_MEDIACODEC) {
            renderMediaCodec()
        }
    }

    private fun installYUVSupport() {
        val vertexSource = WlShaderUtil.readRawTxt(context, R.raw.vertex_shader)
        val fragmentSource = WlShaderUtil.readRawTxt(context, R.raw.fragment_yuv_shader)
        program_yuv = WlShaderUtil.createProgram(vertexSource, fragmentSource)

        avPosition_yuv = GLES20.glGetAttribLocation(program_yuv, "av_Position")
        afPosition_yuv = GLES20.glGetAttribLocation(program_yuv, "af_Position")

        sampler_y = GLES20.glGetUniformLocation(program_yuv, "sampler_y")
        sampler_u = GLES20.glGetUniformLocation(program_yuv, "sampler_u")
        sampler_v = GLES20.glGetUniformLocation(program_yuv, "sampler_v")

        textureId_yuv = IntArray(3)
        GLES20.glGenTextures(3, textureId_yuv, 0)

        for (i in 0..2) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId_yuv!![i])

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        }
    }

    private fun installMediaCodecSupport(textureRef: ArrayList<SurfaceTexture>?): Surface {
        val vertexSource = WlShaderUtil.readRawTxt(context, R.raw.vertex_shader)
        val fragmentSource = WlShaderUtil.readRawTxt(context, R.raw.fragment_mediacodec_shader)
        program_mediacodec = WlShaderUtil.createProgram(vertexSource, fragmentSource)

        avPosition_mediacodec = GLES20.glGetAttribLocation(program_mediacodec, "av_Position")
        afPosition_mediacodec = GLES20.glGetAttribLocation(program_mediacodec, "af_Position")

        sampler_mediacodec = GLES20.glGetUniformLocation(program_mediacodec, "sTexture")

        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        textureId_mediacodec = textureIds[0]

        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        val surfaceTexture = SurfaceTexture(textureId_mediacodec)
        this.surfaceTexture = surfaceTexture
        textureRef?.add(surfaceTexture)
        val surface = Surface(surfaceTexture)
        onSurfaceCreatedLisenter?.onSurfaceCreated(surface, surfaceTexture)
        return surface
    }

    fun setYUVRenderData(width: Int, height: Int, y: ByteArray, u: ByteArray, v: ByteArray) {
        this.width_yuv = width
        this.height_yuv = height
        this.y = ByteBuffer.wrap(y)
        this.u = ByteBuffer.wrap(u)
        this.v = ByteBuffer.wrap(v)
    }

    private fun renderMediaCodec() {
        if (surfaceTexture == null) {
            return
        }
        println("mediacodec render a frame")
        surfaceTexture!!.updateTexImage()
        GLES20.glUseProgram(program_mediacodec)

        GLES20.glEnableVertexAttribArray(avPosition_mediacodec)
        GLES20.glVertexAttribPointer(avPosition_mediacodec, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer)

        GLES20.glEnableVertexAttribArray(afPosition_mediacodec)
        GLES20.glVertexAttribPointer(afPosition_mediacodec, 2, GLES20.GL_FLOAT, false, 8, textureBuffer)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId_mediacodec)
        GLES20.glUniform1i(sampler_mediacodec, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }

    private fun renderYUV() {
//        var ratioW = 1f
//        var ratioH = 1f
//
//        if (width_yuv > maxWidth) {
//            ratioW = maxWidth.toFloat() / width_yuv.toFloat()
//        }
//        if (height_yuv > maxHeight) {
//            ratioH = maxHeight.toFloat() / height_yuv.toFloat()
//        }
//
//        val ratio = Math.min(ratioW, ratioH)
//
//        GLES20.glViewport(0, 0, (maxWidth * ratio).toInt(), (maxHeight * ratio).toInt())
//        GLES20.glViewport(0, 0, 1920, 1080)
        if (width_yuv > 0 && height_yuv > 0 && y != null && u != null && v != null) {
            GLES20.glUseProgram(program_yuv)

            GLES20.glEnableVertexAttribArray(avPosition_yuv)
            GLES20.glVertexAttribPointer(avPosition_yuv, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer)

            GLES20.glEnableVertexAttribArray(afPosition_yuv)
            GLES20.glVertexAttribPointer(afPosition_yuv, 2, GLES20.GL_FLOAT, false, 8, textureBuffer)

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId_yuv!![0])
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, width_yuv, height_yuv, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, y)

            GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId_yuv!![1])
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, width_yuv / 2, height_yuv / 2, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, u)

            GLES20.glActiveTexture(GLES20.GL_TEXTURE2)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId_yuv!![2])
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, width_yuv / 2, height_yuv / 2, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, v)

            GLES20.glUniform1i(sampler_y, 0)
            GLES20.glUniform1i(sampler_u, 1)
            GLES20.glUniform1i(sampler_v, 2)

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

            y!!.clear()
            u!!.clear()
            v!!.clear()
            y = null
            u = null
            v = null

        }
    }

    fun useYUVMode() {
        this.mode = MODE_YUV
    }

    fun useMediaCodecMode() {
        this.mode = MODE_MEDIACODEC
    }


    interface OnSurfaceCreatedListener {
        fun onSurfaceCreated(surface: Surface, surfaceTexture: SurfaceTexture)
    }
}
