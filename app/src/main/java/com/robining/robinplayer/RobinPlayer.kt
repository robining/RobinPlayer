package com.robining.robinplayer

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.SurfaceView
import android.widget.Toast
import java.io.File

class RobinPlayer(val context: Context) : IPlayer, INativeBridge {
    private val TAG = "RobinPlayer"
    private var playerCallback: IPlayer.IPlayerCallback? = null
    private val mainThreadHandler = Handler(Looper.getMainLooper())

    override fun setCallback(callback: IPlayer.IPlayerCallback) {
        this.playerCallback = callback
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

    override fun onPlayStateChanged(oldState: Int, newState: Int) {
        val oldPlayerState = PLAYER_STATE.values()[oldState]
        val newPlayerState = PLAYER_STATE.values()[newState]

        mainThreadHandler.post {
            playerCallback?.onPlayStateChanged(oldPlayerState, newPlayerState)
            Log.i(TAG, ">>>play state changed:$oldPlayerState to $newPlayerState")
        }
    }

    override fun onReceivedWarn(code: Int, message: String) {
        mainThreadHandler.post {
            Log.w(TAG, "code:$code  message:$message")
        }
    }

    override fun onReceivedError(code: Int, message: String) {
        mainThreadHandler.post {
            Log.e(TAG, "code:$code  message:$message")
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onReceivedTotalDuration(duration: Double) {
        mainThreadHandler.post {
            playerCallback?.onReceivedTotalDuration(duration)
        }
    }

    override fun onProgressChanged(progress: Double) {
        mainThreadHandler.post {
            Log.e(TAG, "play progress:$progress")
            playerCallback?.onProgressChanged(progress)
        }
    }

    override fun onPreloadProgressChanged(progress: Double) {
        mainThreadHandler.post {
            Log.e(TAG, "preload progress:$progress")
            playerCallback?.onPreloadProgressChanged(progress)
        }
    }


    init {
        System.loadLibrary("RobinPlayer")
    }
}