package com.robining.robinplayer.camera

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.TextureView
import com.robining.robinplayer.R
import com.robining.robinplayer.base.BaseActivity
import kotlinx.android.synthetic.main.activity_camera.*
import kotlin.concurrent.thread


class CameraActivity : BaseActivity() {
    private var encodec: MediaCodec? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        val mime = "video/avc"
        val width = 1080
        val height = 2248
        val videoFormat = MediaFormat.createVideoFormat(mime, width, height)
        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 4)
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        encodec = MediaCodec.createEncoderByType(mime)
        encodec!!.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

        val surface = encodec!!.createInputSurface()
        println(">>>encodec surface:$surface")
        glSurfaceView.setRenderer(MultiSharedRender(this, arrayOf(CameraRender(this, SurfaceTexture.OnFrameAvailableListener { glSurfaceView.requestRender() })), arrayOf(surface)))
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        encodec!!.start()

        thread {
            val mediaInfo = MediaCodec.BufferInfo()
            while (true) {
                var outbufferIndex = encodec!!.dequeueOutputBuffer(mediaInfo, 10)
                while (outbufferIndex >= 0) {
                    val buffer = encodec!!.outputBuffers[outbufferIndex]
                    buffer.position(mediaInfo.offset)
                    buffer.limit(mediaInfo.size + mediaInfo.offset)
                    Log.e("MainActivity", ">>>encoded ${mediaInfo.size} data")
                    encodec!!.releaseOutputBuffer(outbufferIndex, false)
                    outbufferIndex = encodec!!.dequeueOutputBuffer(mediaInfo, 10)
                }
            }
        }
    }
}
