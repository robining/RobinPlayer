package com.robining.robinplayer

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.view.View
import java.io.File

class MainActivity : AppCompatActivity() {
    lateinit var player : IPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        player = RobinPlayer()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE),1)
            }
        }
    }

    fun play(view : View){
        player.init(File(Environment.getExternalStorageDirectory(),"1.mp3"))
        player.play()
    }

    fun pause(view : View){
        player.pause()
    }

    fun resume(view : View){
        player.resume()
    }

    fun stop(view : View){
        player.stop()
    }

}
