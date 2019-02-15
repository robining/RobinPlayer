package com.robining.robinplayer

import android.content.Context
import android.util.Log
import android.view.SurfaceView
import android.widget.Toast
import com.robining.robinplayer.exceptions.PlayerException
import java.io.File

class RobinPlayer(val context: Context) : IPlayer, INativeBridge {

    var playerCallback: IPlayer.IPlayerCallback? = null

    override fun setCallback(callback: IPlayer.IPlayerCallback) {
        this.playerCallback = callback
    }

    override fun onPlayStateChanged(oldState: Int, newState: Int) {
        val oldPlayerState = PLAYER_STATE.values()[oldState]
        val newPlayerState = PLAYER_STATE.values()[newState]

        playerCallback?.onPlayStateChanged(oldPlayerState, newPlayerState)
        println(">>>------------------------state changed:$oldPlayerState to $newPlayerState")
    }

    init {
        System.loadLibrary("RobinPlayer")
    }

    override fun init(file: File) {
        nativeInit(this, file.absolutePath)
    }

    override fun init(url: String) {
        nativeInit(this, url)
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

    override fun seekTo(seconds: Int) {
        nativeSeekTo(seconds)
    }

    private external fun nativeInit(bridge: INativeBridge, url: String)

    private external fun nativePlay()

    private external fun nativePause()

    private external fun nativeResume()

    private external fun nativeStop()

    private external fun nativeDestroy()

    private external fun nativeSeekTo(seconds: Int)
}