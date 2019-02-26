package com.robining.robinplayer

import android.content.Context
import android.opengl.GLES20
import android.util.Log

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

object WlShaderUtil {


    fun readRawTxt(context: Context, rawId: Int): String {
        val inputStream = context.resources.openRawResource(rawId)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val sb = StringBuffer()
        try {
            var line: String? = reader.readLine()
            while (line != null) {
                sb.append(line).append("\n")
                line = reader.readLine()
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return sb.toString()
    }

    fun loadShader(shaderType: Int, source: String): Int {
        var shader = GLES20.glCreateShader(shaderType)
        if (shader != 0) {
            GLES20.glShaderSource(shader, source)
            GLES20.glCompileShader(shader)
            val compile = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compile, 0)
            if (compile[0] != GLES20.GL_TRUE) {
                Log.d("ywl5320", "shader compile error")
                GLES20.glDeleteShader(shader)
                shader = 0
            }
        }
        return shader
    }

    fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == 0) {
            return 0
        }
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        if (fragmentShader == 0) {
            return 0
        }
        var program = GLES20.glCreateProgram()
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader)
            GLES20.glAttachShader(program, fragmentShader)
            GLES20.glLinkProgram(program)
            val linsStatus = IntArray(1)
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linsStatus, 0)
            if (linsStatus[0] != GLES20.GL_TRUE) {
                Log.d("ywl5320", "link program error")
                GLES20.glDeleteProgram(program)
                program = 0
            }
        }
        return program

    }

}
