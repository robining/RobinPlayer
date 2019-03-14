package com.robining.robinplayer.base

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

open class BaseActivity : AppCompatActivity() {
    private var permissionRequestIndex:Int = 0
    private val permissionRequestIndexMap = hashMapOf<Int,PermissionRequestCallback>()

    fun requestPermission(callback: IPermissionRequestCallback,vararg permissions : String){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val requestCode  = permissionRequestIndex ++
            requestPermissions(permissions,requestCode)
            permissionRequestIndexMap[requestCode] = PermissionRequestCallback(requestCode,callback)
        }else{
            callback.onAllPermissionGranted()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for(result in grantResults){
            if (result != PackageManager.PERMISSION_GRANTED){
                return
            }
        }

        permissionRequestIndexMap[requestCode]?.onAllPermissionGranted()
        permissionRequestIndexMap.remove(requestCode)
    }

    protected fun showMessage(message:CharSequence){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }

    protected fun startActivity(clazz : Class<*>){
        startActivity(Intent(this,clazz))
    }
}