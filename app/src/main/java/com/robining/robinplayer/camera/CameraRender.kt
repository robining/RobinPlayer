package com.robining.robinplayer.camera

import android.app.Activity
import android.graphics.SurfaceTexture
import javax.microedition.khronos.opengles.GL10

class CameraRender(private val activity: Activity, private val frameAvailableListener: SurfaceTexture.OnFrameAvailableListener?) : OesTextureRender(activity) {
    override fun onSurfaceTextureCreated(textureId: Int, surfaceTexture: SurfaceTexture) {
        surfaceTexture.setOnFrameAvailableListener(frameAvailableListener)
        CameraUtil.openCamera(0, surfaceTexture, activity)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        super.onSurfaceChanged(gl, width, height)
        CameraUtil.onSurfaceChanged(0, activity, width, height)
    }

    override fun onSurfaceDestroyed() {
        super.onSurfaceDestroyed()
        CameraUtil.desotry()
    }
}