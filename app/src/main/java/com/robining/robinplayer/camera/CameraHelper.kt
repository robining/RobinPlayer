package com.robining.robinplayer.camera

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import kotlinx.android.synthetic.main.activity_camera.*

class CameraHelper private constructor() {
    private val TAG = "CameraHelper"
    companion object {
        val instance = CameraHelper()
    }


    private var camera : Camera? = null

    fun initCamera(context:Context,texture: SurfaceTexture){
        camera = Camera.open(1)
        camera!!.setPreviewTexture(texture)
        val parameter = camera!!.parameters
        parameter.previewFormat = ImageFormat.NV21
        camera!!.parameters = parameter

        val screenWidth = context.resources.displayMetrics.widthPixels
        val screenHeight = context.resources.displayMetrics.heightPixels
        onSizeChanged(screenWidth,screenHeight)
        camera!!.startPreview()
    }

    fun onSizeChanged(width: Int,height: Int){
        if(camera == null){
            return
        }

        val parameter = camera!!.parameters

        val pictureSize = getFitSize(parameter.supportedPictureSizes,width,height)
        Log.d(TAG,"old picture size:${parameter.pictureSize.width},${parameter.pictureSize.height}")
        parameter.setPictureSize(pictureSize.width,pictureSize.height)
        Log.d(TAG,"new picture size:${parameter.pictureSize.width},${parameter.pictureSize.height}")


        Log.d(TAG,"old preview size:${parameter.previewSize.width},${parameter.previewSize.height}")
        val previewSize = getFitSize(parameter.supportedPreviewSizes,width,height)
        parameter.setPreviewSize(previewSize.width,previewSize.height)
        Log.d(TAG,"new preview size:${parameter.previewSize.width},${parameter.previewSize.height}")

        camera!!.parameters = parameter
    }

    fun destory(){
        camera?.stopPreview()
        camera?.release()
        camera = null
    }

    private fun getFitSize(sizes: List<Camera.Size>,width:Int,height:Int): Camera.Size {
//        var finalWidth = width
//        var finalHeight = height
//        if (width < height) {
//            finalWidth = height
//            finalHeight = width
//        }
//
//        for (size in sizes) {
//            if (1.0f * size.width / size.height == 1.0f * finalWidth / finalHeight) {
//                return size
//            }
//        }
        return sizes[0]
    }
}