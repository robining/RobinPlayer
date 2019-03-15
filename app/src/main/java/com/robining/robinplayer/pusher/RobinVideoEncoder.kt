package com.robining.robinplayer.pusher

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaCodecInfo
import android.view.Surface


class RobinVideoEncoder(mimeType: String, width: Int, height: Int) : RobinBaseMediaEncoder() {
    var surface: Surface
    var encodeListener: EncodeListener? = null
    private var sps = ByteArray(0)
    private var pps = ByteArray(0)

    init {
        val mediaFormat = MediaFormat.createVideoFormat(mimeType, width, height)
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 4)
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)

        this.init(mediaFormat)

        this.surface = mediaEncoder!!.createInputSurface()
    }


    override fun onOutputFormatChanged(outputBufferIndex: Int) {
        val spsb = mediaEncoder!!.outputFormat.getByteBuffer("csd-0")
        var sps = ByteArray(0)
        if (spsb != null) {
            sps = ByteArray(spsb.remaining())
            spsb.get(sps, 0, sps.size)
        }

        val ppsb = mediaEncoder!!.outputFormat.getByteBuffer("csd-1")
        var pps = ByteArray(0)
        if (ppsb != null) {
            pps = ByteArray(ppsb.remaining())
            ppsb.get(pps, 0, pps.size)
        }

        this.sps = sps
        this.pps = pps
    }

    override fun onEncodedData(bufferInfo: MediaCodec.BufferInfo, data: ByteArray) {
        val isKeyFrame = bufferInfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME
        if (isKeyFrame) {
            encodeListener?.onFoundSpsAndPps(sps, pps)
        }
        encodeListener?.onEncodedData(data, isKeyFrame)
    }

    interface EncodeListener {
        fun onEncodedData(data: ByteArray, isKeyFrame: Boolean)
        fun onFoundSpsAndPps(sps: ByteArray, pps: ByteArray)
    }
}