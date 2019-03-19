package com.robining.robinplayer.pusher

import android.media.MediaCodec
import android.media.MediaFormat
import java.lang.ref.WeakReference

abstract class RobinBaseEncoder {
    protected var mediaEncoder: MediaCodec? = null
    private var encodeThread: EncodeThread? = null
    private var mediaFormat: MediaFormat? = null

    fun init(mediaFormat: MediaFormat) {
        this.mediaFormat = mediaFormat
        mediaEncoder = MediaCodec.createEncoderByType(mediaFormat.getString(MediaFormat.KEY_MIME))
        mediaEncoder!!.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    }

    fun start() {
        stop()
        mediaEncoder!!.start()
        encodeThread = EncodeThread(WeakReference(this))
        encodeThread!!.start()
    }

    fun pushData(data: ByteArray, length: Int) {
        if (!mediaFormat!!.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
            throw IllegalStateException("please set KEY_MAX_INPUT_SIZE for mediaFormat")
        }
        if(encodeThread == null || !encodeThread!!.isRunning){
            //还没有开始
            return
        }
        val maxInputSize = mediaFormat!!.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)

        var offset = 0
        var remaining = length
        while (remaining > 0) {
            val index = mediaEncoder!!.dequeueInputBuffer(10)
            if (index >= 0) {
                var inputLength = remaining
                if (remaining > maxInputSize) {
                    inputLength = maxInputSize
                }
                val inputBuffer = mediaEncoder!!.inputBuffers[index]
                inputBuffer.clear()
                inputBuffer.put(data, offset, inputLength)
                mediaEncoder!!.queueInputBuffer(index, 0, inputLength, 0, 0)

                remaining -= inputLength
                offset += inputLength
            }
        }
    }

    fun stop() {
        if (encodeThread != null) {
            encodeThread!!.exitWithJoin()
            mediaEncoder!!.stop()
        }
    }

    open fun onLoopStart() {}
    open fun onLoopStop() {}
    abstract fun onEncodedData(bufferInfo: MediaCodec.BufferInfo, data: ByteArray)
    abstract fun onOutputFormatChanged(outputBufferIndex: Int)

    class EncodeThread(private val encoderRef: WeakReference<RobinBaseEncoder>) : LoopThread() {
        private val mediaBufferInfo = MediaCodec.BufferInfo()
        override fun loopRun(): Boolean {
            val encoder = encoderRef.get() ?: return false
            val index = encoder.mediaEncoder!!.dequeueOutputBuffer(mediaBufferInfo, 10)
            if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                encoder.onOutputFormatChanged(index)
            } else if (index >= 0) {
                val outBuffer = encoder.mediaEncoder!!.outputBuffers[index]
                outBuffer.position(mediaBufferInfo.offset)
                outBuffer.limit(mediaBufferInfo.offset + mediaBufferInfo.size)

                val data = ByteArray(outBuffer.remaining())
                outBuffer.get(data, 0, data.size)
                encoder.onEncodedData(mediaBufferInfo, data)

                outBuffer.position(mediaBufferInfo.offset)
                encoder.mediaEncoder!!.releaseOutputBuffer(index, false)
            }

            return true
        }

        override fun onLoopStart() {
            super.onLoopStart()
            encoderRef.get()?.onLoopStart()
        }

        override fun onLoopEnd() {
            super.onLoopEnd()
            encoderRef.get()?.onLoopStop()
        }
    }
}