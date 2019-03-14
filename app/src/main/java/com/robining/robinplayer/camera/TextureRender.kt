package com.robining.robinplayer.camera

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import com.robining.robinplayer.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

abstract class TextureRender(private val context: Context) : DestroyableGLRender {
    private var programId: Int? = null
    private var vertexAttrId: Int? = null
    private var textureVertexAttrId: Int? = null
    private var samplerTextureUniformId: Int? = null
    private var surfaceTexture: SurfaceTexture? = null

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

        val vertexShaderId = GlesUtil.createVertexShader(context, R.raw.vertex_shader)
        val fragmentShaderId = GlesUtil.createFragmentShader(context, R.raw.fragment_texture2d_shader)
        programId = GlesUtil.createProgram(vertexShaderId, fragmentShaderId)

        vertexAttrId = GLES20.glGetAttribLocation(programId!!, "av_Position")
        textureVertexAttrId = GLES20.glGetAttribLocation(programId!!, "af_Position")
        samplerTextureUniformId = GLES20.glGetUniformLocation(programId!!, "sTexture")

        textureId = GlesUtil.createTexture(null)
        surfaceTexture = SurfaceTexture(textureId!!)

        onSurfaceTextureCreated(textureId!!, surfaceTexture!!)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glUseProgram(programId!!)
        surfaceTexture?.updateTexImage()

        GLES20.glEnableVertexAttribArray(vertexAttrId!!)
        GLES20.glVertexAttribPointer(vertexAttrId!!, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer)

        GLES20.glEnableVertexAttribArray(textureVertexAttrId!!)
        GLES20.glVertexAttribPointer(textureVertexAttrId!!, 2, GLES20.GL_FLOAT, false, 8, textureVertexBuffer)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId!!)
        GLES20.glUniform1i(samplerTextureUniformId!!, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

    override fun onSurfaceDestroyed() {
        surfaceTexture?.release()
        vertexBuffer?.clear()
        textureVertexBuffer?.clear()
    }

    abstract fun onSurfaceTextureCreated(textureId: Int, surfaceTexture: SurfaceTexture)
}