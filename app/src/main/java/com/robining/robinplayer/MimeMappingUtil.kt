package com.robining.robinplayer

import android.media.MediaCodecList

object MimeMappingUtil {
    val mapping = HashMap<String, String>()

    init {
        mapping["h264"] = "video/avc"
    }

    fun getMimeTypeByCodecName(codecName: String): String? {
        return mapping[codecName]
    }

    fun isSupportByMediaCodec(codecName: String): Boolean {
        val mimeType = getMimeTypeByCodecName(codecName) ?: return false
        val codecCount = MediaCodecList.getCodecCount()
        for (i in 0 until codecCount) {
            val codecInfo = MediaCodecList.getCodecInfoAt(i)
            if (!codecInfo.isEncoder) {
                continue
            }
            for (type in codecInfo.supportedTypes) {
                if (type == mimeType) {
                    return true
                }
            }
        }

        return false
    }
}