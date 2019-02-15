package com.robining.robinplayer

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.view.View
import java.io.File

class MainActivity : AppCompatActivity(), IPlayer.IPlayerCallback {
    lateinit var player : IPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        player = RobinPlayer(this)
        player.setCallback(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE),1)
            }
        }
    }

    fun play(view : View){
//        player.init(File(Environment.getExternalStorageDirectory(),"1.mp3"))
//        player.init("http://mpge.5nd.com/2015/2015-11-26/69708/1.mp3")
        player.init("http://dl.stream.qqmusic.qq.com/M500004SoZzZ0Juiny.mp3?vkey=2165317D45895C1DF5BF0ED3AFADB7C22998730AEFD667E226E2D18E16E3C8790179173FDC6A92868E3CACD176B25C854875FA188395E0FB&guid=5150825362&fromtag=1")
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

    fun seekTo(view : View){
        player.seekTo(40)
    }

    override fun onPlayStateChanged(oldState: PLAYER_STATE, newState: PLAYER_STATE) {
        super.onPlayStateChanged(oldState, newState)
        when(newState){
            PLAYER_STATE.NOT_INIT -> {

            }
            else -> {

            }
        }
    }
}
