package com.robining.robinplayer.pusher

object ADTSUtil {
    private val ATDS_SIZE = 7
    fun getPacketSizeWithADTS(originalPacketLength: Int): Int {
        return originalPacketLength + ATDS_SIZE
    }

    fun addADtsHeader(packet: ByteArray, samplerate: Int) {
        val profile = 2 // AAC LC
        val chanCfg = 2 // CPE
        val adtsSmapleRate = getADTSsamplerate(samplerate)
        val packetLen = packet.size

        packet[0] = 0xFF.toByte() // 0xFFF(12bit) 这里只取了8位，所以还差4位放到下一个里面
        packet[1] = 0xF9.toByte() // 第一个t位放F
        packet[2] = ((profile - 1 shl 6) + (adtsSmapleRate shl 2) + (chanCfg shr 2)).toByte()
        packet[3] = ((chanCfg and 3 shl 6) + (packetLen shr 11)).toByte()
        packet[4] = (packetLen and 0x7FF shr 3).toByte()
        packet[5] = ((packetLen and 7 shl 5) + 0x1F).toByte()
        packet[6] = 0xFC.toByte()
    }

    fun arrayWithAdtsHeader(originalPacket: ByteArray, sampleRate: Int): ByteArray {
        val newArray = ByteArray(getPacketSizeWithADTS(originalPacket.size))
        addADtsHeader(newArray, sampleRate)
        originalPacket.copyInto(newArray, ATDS_SIZE, 0, originalPacket.size)
        return newArray
    }

    private fun getADTSsamplerate(samplerate: Int): Int {
        var rate = 4
        when (samplerate) {
            96000 -> rate = 0
            88200 -> rate = 1
            64000 -> rate = 2
            48000 -> rate = 3
            44100 -> rate = 4
            32000 -> rate = 5
            24000 -> rate = 6
            22050 -> rate = 7
            16000 -> rate = 8
            12000 -> rate = 9
            11025 -> rate = 10
            8000 -> rate = 11
            7350 -> rate = 12
        }
        return rate
    }
}