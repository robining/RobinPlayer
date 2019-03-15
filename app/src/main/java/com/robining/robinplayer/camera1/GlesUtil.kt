package com.robining.robinplayer.camera1

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLUtils
import android.support.annotation.RawRes
import java.nio.FloatBuffer

object GlesUtil {
    fun readStringFromRaw(context: Context, @RawRes idRes: Int): String {
        val inputStream = context.resources.openRawResource(idRes)
        val buffer = ByteArray(1024)
        var length = inputStream.read(buffer)
        val stringBuilder = StringBuilder()
        while (length != -1) {
            stringBuilder.append(String(buffer, 0, length))
            length = inputStream.read(buffer)
        }

        return stringBuilder.toString()
    }

    fun createVertexShader(context: Context, @RawRes idRes: Int): Int {
        val shaderStr = readStringFromRaw(context, idRes)
        return createShader(GLES20.GL_VERTEX_SHADER, shaderStr)
    }

    fun createFragmentShader(context: Context, @RawRes idRes: Int): Int {
        val shaderStr = readStringFromRaw(context, idRes)
        return createShader(GLES20.GL_FRAGMENT_SHADER, shaderStr)
    }

    fun createShader(shaderType: Int, shaderStr: String): Int {
        val shaderId = GLES20.glCreateShader(shaderType)
        GLES20.glShaderSource(shaderId, shaderStr)
        GLES20.glCompileShader(shaderId)

        val compileResult = IntArray(1)
        GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, compileResult, 0)
        if (compileResult[0] != GLES20.GL_TRUE) {
            throw IllegalStateException("create shader failed")
        }

        return shaderId
    }

    fun createProgram(vararg shaders: Int): Int {
        val programId = GLES20.glCreateProgram()
        for (shader in shaders) {
            GLES20.glAttachShader(programId, shader)
        }
        GLES20.glLinkProgram(programId)

        val linkResult = IntArray(1)
        GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, linkResult, 0)
        if (linkResult[0] != GLES20.GL_TRUE) {
            throw IllegalStateException("create program failed")
        }

        return programId
    }

    fun createVboBuffer(data: FloatArray, buffer: FloatBuffer): Int {
        val bufferIds = IntArray(1)
        GLES20.glGenBuffers(1, bufferIds, 0)
        val bufferId = bufferIds[0]
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferId)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, data.size * 4, null, GLES20.GL_STATIC_DRAW)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, data.size * 4, buffer)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        return bufferId
    }

    fun createFboBuffer(textureId: Int): Int {
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

    fun createTexture(bitmap: Bitmap?): Int {
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        val textureId = textureIds[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId!!)
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

    fun createOesTexture(): Int {
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        val textureId = textureIds[0]
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)

        return textureId
    }
}