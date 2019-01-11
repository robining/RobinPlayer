package com.robining.robinplayer

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
