package com.robining.robinplayer.camera1

import android.app.Activity
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.opengl.Matrix
import javax.microedition.khronos.opengles.GL10

class CameraRender(private val activity: Activity, private val listener: SurfaceTextureRender.SurfaceTextureListener) :
    SurfaceTextureRender(activity),
        SurfaceTextureRender.SurfaceTextureListener {

    private var cameraPreviewSize: Camera.Size? = null
    override fun onSurfaceTexureCreated(surfaceTexture: SurfaceTexture) {
        surfaceTexture.setOnFrameAvailableListener {
            listener.onSurfaceTexureCreated(surfaceTexture)
        }


        cameraPreviewSize = CameraUtil.openCamera(0, surfaceTexture, activity)
    }

    init {
        setSurfaceTexureListener(this)
    }

    override fun onInitVertexMatrix(matrix: FloatArray, width: Int, height: Int) {
        super.onInitVertexMatrix(matrix, width, height)
//        if (cameraPreviewSize != null) {
//
//            val degree = CameraUtil.degree.toFloat()
//            if (degree == 90f || degree == 270f) {
//                initMartix(
//                    matrix,
//                    cameraPreviewSize!!.height.toFloat(),
//                    cameraPreviewSize!!.width.toFloat(),
//                    width.toFloat(),
//                    height.toFloat()
//                )
//
//            } else {
//                initMartix(
//                    matrix,
//                    cameraPreviewSize!!.width.toFloat(),
//                    cameraPreviewSize!!.height.toFloat(),
//                    width.toFloat(),
//                    height.toFloat()
//                )
//            }
//        }
    }

    override fun onInitTextureVertexMatrix(matrix: FloatArray, width: Int, height: Int) {
        super.onInitTextureVertexMatrix(matrix, width, height)
//        Matrix.rotateM(matrix, 0, CameraUtil.degree.toFloat(), 1f, 0f, 0f)
//        Matrix.rotateM(matrix, 0, CameraUtil.degree.toFloat(), 0f, 0f, 1f)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
//        CameraUtil.onSurfaceChanged(0, activity)
        super.onSurfaceChanged(gl, width, height)
    }
}