package com.robining.robinplayer

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.SurfaceView
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class RobinPlayer(val context: Context) : IPlayer, INativeBridge {
    private val TAG = "RobinPlayer"
    private var playerCallback: IPlayer.IPlayerCallback? = null
    private val mainThreadHandler = Handler(Looper.getMainLooper())
    private var mediaEncoder: MediaCodec? = null
    private var recordAACsampleRate: Int = 44100
    private val recorderLock = "recorderLock"
    private var isInitedEncoder = false
    private var encodeOuputStream: OutputStream? = null

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
        if (mediaEncoder != null) {
            stopRecord()
        }
    }

    override fun destroy() {
        nativeDestroy()
    }

    override fun seekTo(seconds: Int) {
        nativeSeekTo(seconds)
    }

    override fun setAudioChannel(channel: AUDIO_CHANNEL) {
        nativeSetAudioChannel(channel.ordinal)
    }

    override fun startRecord(file: File) {
        synchronized(recorderLock) {
            Log.e(TAG, "init aac encoder start")
            val mimeType = "audio/mp4a-latm"
            val mediaFormat = MediaFormat.createAudioFormat(mimeType, recordAACsampleRate, 2)
            mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 96000)
            mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 5000)//如果一帧大小超出该数字会报错
            mediaEncoder = MediaCodec.createEncoderByType(mimeType)
            mediaEncoder!!.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            mediaEncoder!!.start()

            encodeOuputStream = FileOutputStream(file)
            isInitedEncoder = true
            Log.e(TAG, "init aac encoder end")
        }
    }

    override fun stopRecord() {
        synchronized(recorderLock) {
            isInitedEncoder = false
            Log.e(TAG, "destroy aac encoder start")
            mediaEncoder?.release()
            mediaEncoder = null
            encodeOuputStream?.close()
            Log.e(TAG, "destroy aac encoder start")
        }
    }

    private external fun nativeInit(bridge: INativeBridge, url: String)

    private external fun nativePlay()

    private external fun nativePause()

    private external fun nativeResume()

    private external fun nativeStop()

    private external fun nativeDestroy()

    private external fun nativeSeekTo(seconds: Int)

    private external fun nativeSetAudioChannel(channel: Int)

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

    override fun onPlayAudioFrame(length: Int, bytes: ByteArray) {
        synchronized(recorderLock) {
            if (mediaEncoder == null || !isInitedEncoder) {
                return
            }
            Log.e(TAG, "encoder frame start:$length")
            val inputBufferIndex: Int = mediaEncoder?.dequeueInputBuffer(0) ?: return

            if (inputBufferIndex >= 0) {
                val inputBuffer = mediaEncoder?.inputBuffers!![inputBufferIndex]
                inputBuffer.clear()
                inputBuffer.put(bytes)
                mediaEncoder?.queueInputBuffer(inputBufferIndex, 0, length, 0, 0)
            }

            val mediaCodecBufferInfo = MediaCodec.BufferInfo()
            var outBufferIndex = mediaEncoder?.dequeueOutputBuffer(mediaCodecBufferInfo, 0)
            while (outBufferIndex != null && outBufferIndex >= 0) {
                val outBuffer = mediaEncoder?.outputBuffers!![outBufferIndex]
                outBuffer.position(mediaCodecBufferInfo.offset)
                outBuffer.limit(mediaCodecBufferInfo.size)
                val sizePerFrame = 7 + mediaCodecBufferInfo.size
                val outByteBuffer = ByteArray(sizePerFrame) //因为adts占用7个字节
                addADtsHeader(outByteBuffer, sizePerFrame, mediaEncoder?.outputFormat?.getInteger(MediaFormat.KEY_SAMPLE_RATE)!!)
                outBuffer.get(outByteBuffer, 7, mediaCodecBufferInfo.size)
                Log.e(TAG, "encoded a frame to stream")
                encodeOuputStream?.write(outByteBuffer, 0, sizePerFrame)
                //还原位置
                outBuffer.position(mediaCodecBufferInfo.offset)
                mediaEncoder?.releaseOutputBuffer(outBufferIndex, false)
                outBufferIndex = mediaEncoder?.dequeueOutputBuffer(mediaCodecBufferInfo, 0)
            }
            Log.e(TAG, "encoder frame stopped")
        }
    }

    private fun addADtsHeader(packet: ByteArray, packetLen: Int, samplerate: Int) {
        val profile = 2 // AAC LC
        val chanCfg = 2 // CPE
        val adtsSmapleRate = getADTSsamplerate(samplerate)

        packet[0] = 0xFF.toByte() // 0xFFF(12bit) 这里只取了8位，所以还差4位放到下一个里面
        packet[1] = 0xF9.toByte() // 第一个t位放F
        packet[2] = ((profile - 1 shl 6) + (adtsSmapleRate shl 2) + (chanCfg shr 2)).toByte()
        packet[3] = ((chanCfg and 3 shl 6) + (packetLen shr 11)).toByte()
        packet[4] = (packetLen and 0x7FF shr 3).toByte()
        packet[5] = ((packetLen and 7 shl 5) + 0x1F).toByte()
        packet[6] = 0xFC.toByte()
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


    init {
        System.loadLibrary("RobinPlayer")
    }
}