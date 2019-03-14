package com.robining.robinplayer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.robining.robinplayer.base.BaseActivity
import com.robining.robinplayer.base.IPermissionRequestCallback
import com.robining.robinplayer.camera.CameraActivity
import com.robining.robinplayer.opengl.EglActivity
import com.robining.robinplayer.player.PlayerActivity

class MainActivity : BaseActivity() {
    private val context : Context = this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun gotoPlayer(view : View){
        requestPermission(object : IPermissionRequestCallback {
            override fun onAllPermissionGranted() {
                startActivity(Intent(context, PlayerActivity::class.java))
            }
        },Manifest.permission.WRITE_EXTERNAL_STORAGE)

    }

    fun gotoCamera(view: View){
        requestPermission(object :IPermissionRequestCallback{
            override fun onAllPermissionGranted() {
                startActivity(CameraActivity::class.java)
            }
        },Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    fun gotoEgl(view: View){
        startActivity(EglActivity::class.java)
    }
}
