package com.robining.robinplayer.camera

import android.graphics.ImageFormat
import android.hardware.Camera
import android.media.MediaCodec
import android.os.Bundle
import android.view.Surface
import com.robining.robinplayer.R
import com.robining.robinplayer.base.BaseActivity
import kotlinx.android.synthetic.main.activity_camera.*
import android.R.attr.configure
import android.media.MediaFormat
import android.media.MediaCodecInfo
import android.util.Log
import com.robining.robinplayer.player.MimeMappingUtil
import java.io.IOException


class CameraActivity : BaseActivity() {
    private val TAG = "CameraActivity"
    private var videoBufferinfo :MediaCodec.BufferInfo? = null
    private var videoFormat : MediaFormat? = null
    private var videoEncodec : MediaCodec? = null
    private var surface : Surface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        initVideoEncodec("video/avc",720,1280)
        glSurfaceView.init(surface)

    }

    private fun initVideoEncodec(mimeType: String, width: Int, height: Int) {
        try {
            videoBufferinfo = MediaCodec.BufferInfo()
            videoFormat = MediaFormat.createVideoFormat(mimeType, width, height)
            videoFormat!!.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            videoFormat!!.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 4)
            videoFormat!!.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
            videoFormat!!.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)

            videoEncodec = MediaCodec.createEncoderByType(mimeType)
            videoEncodec!!.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

            surface = videoEncodec!!.createInputSurface()

        } catch (e: IOException) {
            e.printStackTrace()
            videoEncodec = null
            videoFormat = null
            videoBufferinfo = null
            surface = null
        }

    }
}
