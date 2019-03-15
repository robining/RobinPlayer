package com.robining.robinplayer.pusher

import android.graphics.SurfaceTexture
import android.media.AudioFormat
import android.media.MediaFormat
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.robining.robinplayer.R
import com.robining.robinplayer.camera1.CameraRender
import com.robining.robinplayer.camera1.RenderMode
import com.robining.robinplayer.camera1.SurfaceTextureRender
import kotlinx.android.synthetic.main.activity_pusher.*
import java.io.File
import java.io.FileOutputStream

class PusherActivity : AppCompatActivity() {
    var audioRecorder: RobinAudioRecorder? = null
    var audioEncoder: RobinAudioEncoder? = null
    var videoEncoder: RobinVideoEncoder? = null
    val file = File(Environment.getExternalStorageDirectory(), "aac/audioRecord.aac")
    var fileOutputStream: FileOutputStream? = null
    var rtmpPusher: RobinPusher? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pusher)
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }

        if (!file.exists()) {
            file.createNewFile()
        }

        audioRecorder = RobinAudioRecorder(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT)
        audioRecorder!!.recordListener = object : RobinAudioRecorder.RecordListener {
            override fun onReadData(data: ByteArray, len: Int) {
                println(">>>recorded $len byte data")
                audioEncoder?.pushData(data, len)
            }
        }
        audioEncoder = RobinAudioEncoder(MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 2, 16)
        audioEncoder!!.encodeListener = object : RobinAudioEncoder.EncodeListener {
            override fun onFoundSpsAndPps(sps: ByteArray, pps: ByteArray) {
//                println(">>>found sps:${sps.size}  pps:${pps.size}")
//                rtmpPusher?.pushSpsAndPps(sps,pps)
            }

            override fun onEncodedData(data: ByteArray) {
//                val finalData = ADTSUtil.arrayWithAdtsHeader(data,44100)
//                println(">>>encoded ${data.size} byte data,with adts ${finalData.size}")
//                fileOutputStream?.write(finalData)
//                println(">>>encoded audio ${data.size} byte data")
//                rtmpPusher?.pushAudio(data)
            }
        }

        videoEncoder = RobinVideoEncoder(MediaFormat.MIMETYPE_VIDEO_AVC, 720, 1280)
        videoEncoder!!.encodeListener = object : RobinVideoEncoder.EncodeListener {
            override fun onEncodedData(data: ByteArray, isKeyFrame: Boolean) {
                println(">>>encoded video ${data.size} byte data")
                rtmpPusher?.pushVideo(data, isKeyFrame)
            }

            override fun onFoundSpsAndPps(sps: ByteArray, pps: ByteArray) {
                println(">>>found sps:${sps.size}  pps:${pps.size}")
                rtmpPusher?.pushSpsAndPps(sps, pps)
            }
        }

        glSurfaceView.setRenderMode(RenderMode.RENDERMODE_WHEN_DIRTY)
        glSurfaceView.setRenders(arrayOf(CameraRender(this, object :
                SurfaceTextureRender.SurfaceTextureListener {
            override fun onSurfaceTexureCreated(surfaceTexture: SurfaceTexture) {
                glSurfaceView.requestRender()
            }
        })))
        glSurfaceView.getExtraSurfaces().add(videoEncoder!!.surface)

        rtmpPusher = RobinPusher()
    }

    fun startRecord(view: View) {
        fileOutputStream = FileOutputStream(file)
        rtmpPusher?.connect("rtmp://132.232.32.188:1936/live/room")
//        audioEncoder?.start()
        videoEncoder?.start()
        audioRecorder?.start()
    }

    fun stopRecord(view: View) {
        audioRecorder?.stop()
//        audioEncoder?.stop()
        videoEncoder?.stop()
        fileOutputStream?.close()
        fileOutputStream = null
    }
}
