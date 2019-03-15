package com.robining.robinplayer.camera1

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.*
import android.util.Log
import com.robining.robinplayer.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.opengles.GL10

class PictureRender(private val context: Context) : GLSurfaceView.Renderer {

    private var vPositionId: Int? = null
    private var fPositionId: Int? = null
    private var samplerTextureId: Int? = null
    private var uMatrixId: Int? = null
    private var programId: Int? = null
    private var textureId: Int? = null
    private var imgTextureId: Int? = null //若使用fbo缓冲，那么只有imgTextureId里面有图，textureId是初始化的空的
    private var vertexVboId: Int? = null
    private var textureVertexVboId: Int? = null
    private var textureFboId: Int? = null
    private var isCreatedSuccess = false

    private val vertexArray = floatArrayOf(
        -1f, -1f,
        1f, -1f,
        -1f, 1f,
        1f, 1f
    )

    //fbo使用
//        private val textureVertexArray = floatArrayOf(
//            0f, 0f,
//            1f, 0f,
//            0f, 1f,
//            1f, 1f
//        )


    private val textureVertexArray = floatArrayOf(
        0f, 1f,
        1f, 1f,
        0f, 0f,
        1f, 0f
    )
    private val vertexBuffer: FloatBuffer
    private val fragmentBuffer: FloatBuffer

    private var matrix = FloatArray(16)
    private var bitmapWidth = 0
    private var bitmapHeight = 0

    init {
        vertexBuffer = ByteBuffer.allocateDirect(4 * vertexArray.size)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexArray);
        vertexBuffer.position(0)

        fragmentBuffer = ByteBuffer.allocateDirect(4 * textureVertexArray.size)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(textureVertexArray)
        fragmentBuffer.position(0)

    }

    private fun drawToFrameBuffer() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, textureFboId!!) //fbo不会使用
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f)

        GLES20.glUseProgram(programId!!)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, imgTextureId!!)
        GLES20.glUniform1i(samplerTextureId!!, 0)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexVboId!!)
        GLES20.glEnableVertexAttribArray(vPositionId!!)
        GLES20.glVertexAttribPointer(vPositionId!!, 2, GLES20.GL_FLOAT, false, 8, 0)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, textureVertexVboId!!)
        GLES20.glEnableVertexAttribArray(fPositionId!!)
        GLES20.glVertexAttribPointer(fPositionId!!, 2, GLES20.GL_FLOAT, false, 8, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
    }

    override fun onDrawFrame(gl: GL10?) {
        if (!isCreatedSuccess) {
            return
        }

        val startTimestamp = System.currentTimeMillis()
//            drawToFrameBuffer()
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f)

        GLES20.glUseProgram(programId!!)
        GLES20.glUniformMatrix4fv(uMatrixId!!, 1, false, matrix, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, imgTextureId!!)
        GLES20.glUniform1i(samplerTextureId!!, 1)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexVboId!!)
        GLES20.glEnableVertexAttribArray(vPositionId!!)
        GLES20.glVertexAttribPointer(vPositionId!!, 2, GLES20.GL_FLOAT, false, 8, 0)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, textureVertexVboId!!)
        GLES20.glEnableVertexAttribArray(fPositionId!!)
        GLES20.glVertexAttribPointer(fPositionId!!, 2, GLES20.GL_FLOAT, false, 8, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        Log.e("PictureRender", "drawed a frame costed:${System.currentTimeMillis() - startTimestamp}")
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        Log.e("MainActivity", "viewport size:$width * $height")

        initMartix(matrix, bitmapWidth.toFloat(), bitmapHeight.toFloat(), width.toFloat(), height.toFloat())
//        Matrix.orthoM(matrix, 0, -1f, 1f, -1f, 1f, -1f, 1f)
//            Matrix.rotateM(matrix,0,90f,0f,0f,1f)
        println(">>>${matrix.toList()}")
    }

    override fun onSurfaceCreated(gl: GL10?, config: javax.microedition.khronos.egl.EGLConfig?) {
        val vertexShaderId = GlesUtil.createVertexShader(context, R.raw.gl_vertex_matrix_shader)
        val fragmentShaderId = GlesUtil.createFragmentShader(context, R.raw.gl_samper2d_fragment_shader)
        programId = GlesUtil.createProgram(vertexShaderId, fragmentShaderId)

        vPositionId = GLES20.glGetAttribLocation(programId!!, "vPosition")
        fPositionId = GLES20.glGetAttribLocation(programId!!, "fPosition")
        samplerTextureId = GLES20.glGetUniformLocation(programId!!, "samplerTexture")
        uMatrixId = GLES20.glGetUniformLocation(programId!!, "uMatrix")

        vertexVboId = createVboBuffer(vertexArray, vertexBuffer)
        textureVertexVboId = createVboBuffer(textureVertexArray, fragmentBuffer)

        Log.e("", "test...................$context  ....${context.resources} ....${R.mipmap.ic_launcher}")
        val bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.b)
        bitmapWidth = bitmap.width
        bitmapHeight = bitmap.height

        Log.e("MainActivity", "bitmap size:$bitmapWidth * $bitmapHeight")
        imgTextureId = createTexture(bitmap)

        textureId = createTexture(null)
        textureFboId = createFboBuffer(textureId!!)
        isCreatedSuccess = true
    }

    private fun initMartix(
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


    private fun createVboBuffer(data: FloatArray, buffer: FloatBuffer): Int {
        val bufferIds = IntArray(1)
        GLES20.glGenBuffers(1, bufferIds, 0)
        val bufferId = bufferIds[0]
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferId)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, data.size * 4, null, GLES20.GL_STATIC_DRAW)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, data.size * 4, buffer)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        return bufferId
    }

    private fun createFboBuffer(textureId: Int): Int {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 1080, 1920, 0,
            GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null
        )
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

        val bufferIds = IntArray(1)
        GLES20.glGenBuffers(1, bufferIds, 0)
        val bufferId = bufferIds[0]
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, bufferId)
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER,
            GLES20.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D,
            textureId,
            0
        )
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw IllegalStateException("create fbo failed")
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        return bufferId
    }

    private fun createTexture(bitmap: Bitmap?): Int {
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        val textureId = textureIds[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        if (bitmap != null) {
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
            bitmap.recycle()
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

        return textureId
    }
}