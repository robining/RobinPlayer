package com.robining.robinplayer.pusher

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat

class RobinAudioEncoder(mime: String, sampleRate: Int, channelCount: Int, bitPerSample: Int) : RobinBaseMediaEncoder() {
    private val mediaFormat: MediaFormat = MediaFormat.createAudioFormat(mime, sampleRate, channelCount)
    var encodeListener: EncodeListener? = null

    init {
//        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 96000)
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, sampleRate * channelCount * bitPerSample)
        //如果是编码AAC需要设置编码级别
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 4000)
        this.init(mediaFormat)
    }

    override fun onOutputFormatChanged(outputBufferIndex: Int) {
        val spsb = mediaEncoder!!.outputFormat.getByteBuffer("csd-0")
        var sps = ByteArray(0)
        if(spsb != null){
            sps = ByteArray(spsb.remaining())
            spsb.get(sps, 0, sps.size)
        }

        val ppsb = mediaEncoder!!.outputFormat.getByteBuffer("csd-1")
        var pps = ByteArray(0)
        if(ppsb != null){
            pps = ByteArray(ppsb.remaining())
            ppsb.get(pps, 0, pps.size)
        }

        encodeListener?.onFoundSpsAndPps(sps, pps)
    }

    override fun onEncodedData(bufferInfo: MediaCodec.BufferInfo, data: ByteArray) {
        encodeListener?.onEncodedData(data)
    }

    interface EncodeListener {
        fun onEncodedData(data: ByteArray)
        fun onFoundSpsAndPps(sps: ByteArray, pps: ByteArray)
    }
}