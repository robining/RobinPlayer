package com.robining.robinplayer.pusher

class RobinPusher : IPusher {
    init {
        System.loadLibrary("RobinPusher")
    }

    override fun connect(url: String) {
        nativeConnectPusher(url)
    }

    override fun pushAudio(data: ByteArray) {
        nativePushAudio(data, data.size)
    }

    override fun pushVideo(data: ByteArray) {

    }


    override fun pushSpsAndPps(sps: ByteArray, pps: ByteArray) {
        nativePushSpsAndPps(sps, sps.size, pps, pps.size)
    }


    private external fun nativeConnectPusher(url: String)

    private external fun nativePushAudio(bytes: ByteArray, length: Int)

    private external fun nativePushSpsAndPps(sps: ByteArray, spsLength: Int, pps: ByteArray, ppsLength: Int)
}