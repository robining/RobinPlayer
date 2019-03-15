package com.robining.robinplayer.camera

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.opengl.GLSurfaceView
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import com.robining.robinplayer.R
import com.robining.robinplayer.camera1.CameraRender
import com.robining.robinplayer.camera1.RGLSurfaceView
import com.robining.robinplayer.camera1.RenderMode
import com.robining.robinplayer.camera1.SurfaceTextureRender
import kotlinx.android.synthetic.main.activity_camera2.*
import kotlin.concurrent.thread

class Camera2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera2)
        flContent.post {
            val glSurfaceView = RGLSurfaceView(this)
            val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            flContent.addView(glSurfaceView, layoutParams)
            val render = CameraRender(this, object : SurfaceTextureRender.SurfaceTextureListener {
                override fun onSurfaceTexureCreated(surfaceTexture: SurfaceTexture) {
                    glSurfaceView.requestRender()
                }
            })
            glSurfaceView.setRenders(arrayOf(render))
            glSurfaceView.setRenderMode(RenderMode.RENDERMODE_WHEN_DIRTY)

            val mime = "video/avc"
            val width = flContent.width
            val height = flContent.height
            val videoFormat = MediaFormat.createVideoFormat(mime, width, height)
            videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 4)
            videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
            videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            val encodec = MediaCodec.createEncoderByType(mime)
            encodec!!.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val surface = encodec!!.createInputSurface()
            glSurfaceView.getExtraSurfaces().add(surface)
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
}
