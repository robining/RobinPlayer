package com.robining.robinplayer

interface INativeBridge {
    fun onPlayStateChanged(oldState: Int, newState: Int)
    fun onReceivedError(code: Int, message: String)
    fun onReceivedWarn(code: Int, message: String)
    fun onReceivedTotalDuration(duration: Double)
    fun onProgressChanged(progress: Double)
    fun onPreloadProgressChanged(progress: Double)
    fun onPlayAudioFrame(length: Int, bytes: ByteArray)
    fun onPlayVideoFrame(width: Int, height: Int, y: ByteArray, u: ByteArray, v: ByteArray)
    fun isSupportDecodeByMediaCodec(format: String) : Boolean
    fun decodeVideoByMediaCodec(length: Int,bytes: ByteArray)
}