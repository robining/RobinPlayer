package com.robining.robinplayer.camera1

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.robining.robinplayer.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

open class SurfaceTextureRender(private val context: Context) : GLSurfaceView.Renderer {
    private var programId: Int? = null
    private var vertexAttrId: Int? = null
    private var textureVertexAttrId: Int? = null
    private var samplerTextureUniformId: Int? = null
    private var vMatrixId: Int? = null
    private var fMatrixId: Int? = null

    private val vertexPositions = floatArrayOf(
        -1f, -1f,
        1f, -1f,
        -1f, 1f,
        1f, 1f
    )

    private val textureVertexPositions = floatArrayOf(
        0f, 1f,
        1f, 1f,
        0f, 0f,
        1f, 0f
    )

    private var vertexBuffer: FloatBuffer? = null
    private var textureVertexBuffer: FloatBuffer? = null
    private var textureId: Int? = null
    private var testPictureTextureId: Int? = null

    private var surfaceTexture: SurfaceTexture? = null
    private var surfaceTextureListener: SurfaceTextureListener? = null
    private var surfaceWidth = 0f
    private var surfaceHeight = 0f
    private val vertexMatrix = FloatArray(16)
    private val textureVertexMatrix = FloatArray(16)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        vertexBuffer = ByteBuffer.allocateDirect(4 * vertexPositions.size)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexPositions)
        vertexBuffer!!.position(0)

        textureVertexBuffer = ByteBuffer.allocateDirect(4 * textureVertexPositions.size)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(textureVertexPositions)
        textureVertexBuffer!!.position(0)

        val vertexShaderId = GlesUtil.createVertexShader(context, R.raw.gl_vertex_matrix2_shader)
        val fragmentShaderId = GlesUtil.createFragmentShader(context, R.raw.gl_oes_fragment_shader)
        programId = GlesUtil.createProgram(vertexShaderId, fragmentShaderId)

        vertexAttrId = GLES20.glGetAttribLocation(programId!!, "vPosition")
        textureVertexAttrId = GLES20.glGetAttribLocation(programId!!, "fPosition")
        samplerTextureUniformId = GLES20.glGetUniformLocation(programId!!, "samplerTexture")
        vMatrixId = GLES20.glGetUniformLocation(programId!!, "vMatrix")
        fMatrixId = GLES20.glGetUniformLocation(programId!!, "fMatrix")

        textureId = GlesUtil.createOesTexture()
        surfaceTexture = SurfaceTexture(textureId!!)
        testPictureTextureId = GlesUtil.createTexture(BitmapFactory.decodeResource(context.resources, R.drawable.b))

        if (surfaceTextureListener != null) {
            surfaceTextureListener!!.onSurfaceTexureCreated(surfaceTexture!!)
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        surfaceWidth = width.toFloat()
        surfaceHeight = height.toFloat()

        onInitVertexMatrix(vertexMatrix, width, height)
        onInitTextureVertexMatrix(textureVertexMatrix, width, height)
    }

    open fun onInitVertexMatrix(matrix: FloatArray, width: Int, height: Int) {
        Matrix.orthoM(matrix, 0, -1f, 1f, -1f, 1f, -1f, 1f)
    }

    open fun onInitTextureVertexMatrix(matrix: FloatArray, width: Int, height: Int) {
        Matrix.orthoM(matrix, 0, -1f, 1f, -1f, 1f, -1f, 1f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glUseProgram(programId!!)
        GLES20.glUniformMatrix4fv(vMatrixId!!, 1, false, vertexMatrix, 0)
        GLES20.glUniformMatrix4fv(fMatrixId!!, 1, false, textureVertexMatrix, 0)
        surfaceTexture?.updateTexImage()

        GLES20.glEnableVertexAttribArray(vertexAttrId!!)
        GLES20.glVertexAttribPointer(vertexAttrId!!, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer)

        GLES20.glEnableVertexAttribArray(textureVertexAttrId!!)
        GLES20.glVertexAttribPointer(textureVertexAttrId!!, 2, GLES20.GL_FLOAT, false, 8, textureVertexBuffer)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId!!)
        GLES20.glUniform1i(samplerTextureUniformId!!, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
    }

    protected fun initMartix(
        matrix: FloatArray,
        targetWidth: Float,
        targetHeight: Float,
        viewWidth: Float,
        viewHeight: Float
    ) {
        var wRatio: Float
        var hRatio: Float

        wRatio = targetWidth / viewWidth
        hRatio = targetHeight / viewHeight
        if (wRatio > 1 && hRatio <= 1) {
            //宽度超出屏幕，高度没有
            hRatio /= wRatio
            wRatio = 1f
        } else if (wRatio <= 1 && hRatio > 1) {
            wRatio /= hRatio
            hRatio = 1f
        } else if (wRatio > 1 && hRatio > 1) {
            //都超过屏幕，以大的边进行所需
            val ratio = Math.max(wRatio, hRatio)
            wRatio /= ratio
            hRatio /= ratio
        }

        //转换成倍数
        wRatio = 1 / wRatio
        hRatio = 1 / hRatio
        Matrix.orthoM(matrix, 0, -wRatio, wRatio, -hRatio, hRatio, -1f, 1f)
    }

    fun setSurfaceTexureListener(listener: SurfaceTextureListener) {
        this.surfaceTextureListener = listener
    }

    interface SurfaceTextureListener {
        fun onSurfaceTexureCreated(surfaceTexture: SurfaceTexture)
    }
}
