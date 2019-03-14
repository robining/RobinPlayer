package com.robining.robinplayer.camera

import android.app.Activity
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.view.Surface
import android.view.SurfaceView
import android.graphics.ImageFormat


object CameraUtil {
    var camera: Camera? = null
    var degree = 0

    fun openCamera(cameraId: Int, surfaceTexture: SurfaceTexture?, activity: Activity?): Camera.Size {
        camera = Camera.open(cameraId)
        if (surfaceTexture != null) {
            camera!!.setPreviewTexture(surfaceTexture)
        }
        val parameters = camera!!.parameters

        parameters.flashMode = "off"
        parameters.previewFormat = ImageFormat.NV21

        parameters.setPictureSize(
                parameters.supportedPictureSizes[0].width,
                parameters.supportedPictureSizes[0].height
        )
        parameters.setPreviewSize(
                parameters.supportedPreviewSizes[0].width,
                parameters.supportedPreviewSizes[0].height
        )

        camera!!.parameters = parameters

        if (activity != null) {
            degree = getDegree(cameraId, activity)
            camera!!.setDisplayOrientation(degree)
        }

        camera!!.startPreview()

        return parameters.previewSize
    }

    fun getDisplayOrientation(): Int {
        return degree
    }

    fun onSurfaceChanged(cameraId: Int, activity: Activity?, width: Int, height: Int) {
        if (activity != null) {
            camera!!.stopPreview()
            degree = getDegree(cameraId, activity)
            camera!!.setDisplayOrientation(degree)
            camera!!.startPreview()
        }
    }

    private fun getDegree(cameraId: Int, activity: Activity): Int {
        val info = android.hardware.Camera.CameraInfo()
        android.hardware.Camera.getCameraInfo(cameraId, info)
        val rotation = activity.windowManager.defaultDisplay
                .rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
            else -> {
            }
        }

        var result: Int
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            // compensate the mirror
            result = (360 - result) % 360
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360
        }

        return result
    }


    fun desotry() {
        camera?.stopPreview()
        camera?.release()
    }
}