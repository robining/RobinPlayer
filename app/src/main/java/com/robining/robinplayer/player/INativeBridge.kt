package com.robining.robinplayer.player

interface INativeBridge {
    fun onPlayStateChanged(oldState: Int, newState: Int)
    fun onReceivedError(code: Int, message: String)
    fun onReceivedWarn(code: Int, message: String)
    fun onReceivedTotalDuration(duration: Double)
    fun onProgressChanged(progress: Double)
    fun onPreloadProgressChanged(progress: Double)
    fun onPlayAudioFrame(length: Int, bytes: ByteArray)
    fun onPlayVideoFrame(width: Int, height: Int, y: ByteArray, u: ByteArray, v: ByteArray)
    fun initDecodeByMediaCodec(codecName: String, width: Int, height: Int,csd0 : ByteArray,csd1 : ByteArray): Boolean
    fun decodeVideoByMediaCodec(length: Int, bytes: ByteArray)
    fun useMediaCodecDecodeVideoMode()
    fun useYUVDecodeVideoMode()
}