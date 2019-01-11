package com.robining.robinplayer

import android.view.SurfaceView
import com.robining.robinplayer.exceptions.PlayerException
import java.io.File

class RobinPlayer : IPlayer {

    init {
        System.loadLibrary("RobinPlayer")
    }

    override fun init(file: File) {
        var result =  nativeInit(file.absolutePath)
        if(result < 0){
            throw PlayerException("open failed")
        }
    }

    override fun init(url: String) {
        nativeInit(url)
    }

    override fun setSurface(surfaceView: SurfaceView?) {
        throw Exception("This operation not supported at this time")
    }

    override fun play() {
        nativePlay()
    }

    override fun pause() {
        nativePause()
    }

    override fun resume() {
        nativeResume()
    }

    override fun stop() {
        nativeStop()
    }

    override fun destroy() {
        nativeDestroy()
    }

    private external fun nativeInit(url: String) : Int

    private external fun nativePlay()

    private external fun nativePause()

    private external fun nativeResume()

    private external fun nativeStop()

    private external fun nativeDestroy()
}