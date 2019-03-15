package com.robining.robinplayer.pusher

interface IPusher {
    fun connect(url: String)

    fun pushAudio(data: ByteArray)

    fun pushVideo(data: ByteArray)

    fun pushSpsAndPps(sps: ByteArray, pps: ByteArray)
}