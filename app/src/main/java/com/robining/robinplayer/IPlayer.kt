package com.robining.robinplayer

import android.view.SurfaceView
import java.io.File

interface IPlayer {
    /**
     * 使用本地文件初始化
     */
    @Throws(Exception::class)
    fun init(file : File)

    /**
     * 使用网络路径初始化
     */
    @Throws(Exception::class)
    fun init(url : String)

    /**
     * 设置渲染接口
     */
    @Throws(Exception::class)
    fun setSurface(surfaceView : SurfaceView?)

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
    fun resume(){
        play()
    }

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
}