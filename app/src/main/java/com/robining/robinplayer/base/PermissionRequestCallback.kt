package com.robining.robinplayer.base

class PermissionRequestCallback(val reuqestIndex : Int,val callback: IPermissionRequestCallback) : IPermissionRequestCallback{
    override fun onAllPermissionGranted() {
        callback.onAllPermissionGranted()
    }

}