package com.robining.robinplayer.pusher

import android.media.AudioRecord
import java.lang.ref.WeakReference

class RobinAudioRecorder(audioSource: Int,sampleRateInHz: Int, channelConfig: Int, audioFormat: Int) {
    private val audioRecorder: AudioRecord
    private var currentRecordingThread: RecordingThread? = null
    private var bufferSizeInBytes: Int = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
    var recordListener: RecordListener? = null

    init {
        audioRecorder = AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes)
    }

    fun start() {
        stop()
        currentRecordingThread = RecordingThread(WeakReference(this))
        currentRecordingThread!!.start()
        audioRecorder.startRecording()
    }

    fun stop() {
        if (currentRecordingThread != null) {
            currentRecordingThread!!.exitWithJoin()
            audioRecorder.stop()
        }
    }


    class RecordingThread(private val recorderRef: WeakReference<RobinAudioRecorder>) : LoopThread() {
        override fun loopRun(): Boolean {
            val recorder = recorderRef.get() ?: return false
            val buffer = ByteArray(recorder.bufferSizeInBytes)
            val size = recorder.audioRecorder.read(buffer, 0, buffer.size)
            if (size > 0) {
                recorder.recordListener?.onReadData(buffer, size)
            }
            return true
        }
    }

    interface RecordListener {
        fun onReadData(data: ByteArray, len: Int)
    }
}