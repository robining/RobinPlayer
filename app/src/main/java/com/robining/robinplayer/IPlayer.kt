package com.robining.robinplayer

import android.opengl.GLSurfaceView
import android.view.SurfaceView
import java.io.File

interface IPlayer {
    /**
     * 使用本地文件初始化
     */
    @Throws(Exception::class)
    fun init(file: File)

    /**
     * 使用网络路径初始化
     */
    @Throws(Exception::class)
    fun init(url: String)

    /**
     * 设置渲染接口
     */
    @Throws(Exception::class)
    fun setSurface(surfaceView: SurfaceView?)

    /**
     * 开始播放
     */
    @Throws(Exception::class)
    fun play()

    /**
     * 暂停播放
     */
    @Throws(Exception::class)
    fun pause()

    /**
     * 恢复播放
     */
    @Throws(Exception::class)
    fun resume()

    /**
     * 停止播放
     */
    @Throws(Exception::class)
    fun stop()

    /**
     * 释放资源
     */
    @Throws(Exception::class)
    fun destroy()

    /**
     * 跳转至
     * @param seconds 指定位置(s)
     */
    fun seekTo(seconds: Int)

    /**
     * 设置音频声道
     */
    fun setAudioChannel(channel: AUDIO_CHANNEL)

    fun setCallback(callback: IPlayerCallback)

    fun startRecord(file: File)

    fun stopRecord()

    fun bindSurfaceView(glSurfaceView: GLSurfaceView)

    interface IPlayerCallback {
        fun onPlayStateChanged(oldState: PLAYER_STATE, newState: PLAYER_STATE) {}
        fun onReceivedTotalDuration(duration: Double){}
        fun onProgressChanged(progress: Double){}
        fun onPreloadProgressChanged(progress: Double){}
    }
}