package com.robining.robinplayer

import android.Manifest
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity(), IPlayer.IPlayerCallback, SeekBar.OnSeekBarChangeListener {

    lateinit var player: IPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        seekBar.isEnabled = false
        seekBar.setOnSeekBarChangeListener(this)

        player = RobinPlayer(this)
        player.setCallback(this)

        player.bindSurfaceView(surfaceView)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }
        }
    }

    fun play(view: View) {
//        player.init(File(Environment.getExternalStorageDirectory(), "1.mp3"))
//        player.init(File(Environment.getExternalStorageDirectory(), "VID_20190211_170131.mp4"))
        player.init(File(Environment.getExternalStorageDirectory(),"DCIM/Camera/VID_20190226_172327.mp4"))
//        player.init("http://mpge.5nd.com/2015/2015-11-26/69708/1.mp3")
//        player.init("http://dl.stream.qqmusic.qq.com/M500004SoZzZ0Juiny.mp3?vkey=2165317D45895C1DF5BF0ED3AFADB7C22998730AEFD667E226E2D18E16E3C8790179173FDC6A92868E3CACD176B25C854875FA188395E0FB&guid=5150825362&fromtag=1")
        player.play()
    }

    fun pause(view: View) {
        player.pause()
    }

    fun resume(view: View) {
        player.resume()
    }

    fun stop(view: View) {
        player.stop()
    }

    fun seekTo(view: View) {
        player.seekTo(100)
    }

    fun setLeftAudioChannel(view: View) {
        player.setAudioChannel(AUDIO_CHANNEL.LEFT)
    }

    fun setRightAudioChannel(view: View) {
        player.setAudioChannel(AUDIO_CHANNEL.RIGHT)
    }

    fun setNoneAudioChannel(view: View) {
        player.setAudioChannel(AUDIO_CHANNEL.NONE)
    }

    fun startRecord(view: View) {
        val file = File(Environment.getExternalStorageDirectory(), "aac/tttt.aac")
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        if (!file.exists()) {
            file.createNewFile()
        }
        player.startRecord(file)
    }

    fun stopRecord(view: View) {
        player.stopRecord()
    }

    override fun onPlayStateChanged(oldState: PLAYER_STATE, newState: PLAYER_STATE) {
        super.onPlayStateChanged(oldState, newState)
        when (newState) {
            PLAYER_STATE.NOT_INIT -> {
                seekBar.isEnabled = false
            }
            PLAYER_STATE.INITED -> {
                seekBar.isEnabled = true
            }
            else -> {

            }
        }
    }

    override fun onReceivedTotalDuration(duration: Double) {
        super.onReceivedTotalDuration(duration)
        seekBar.max = duration.toInt()
        updateDurationText()
    }

    private fun updateDurationText() {
        val duration = seekBar.max
        val progress = seekBar.progress

        val dm = duration / 60
        val ds = duration % 60
        val pm = progress / 60
        val ps = progress % 60
        tvDuration.text = "$pm:$ps / $dm:$ds"
    }

    override fun onProgressChanged(progress: Double) {
        super.onProgressChanged(progress)
        seekBar.progress = progress.toInt()
        updateDurationText()
    }

    override fun onPreloadProgressChanged(progress: Double) {
        super.onPreloadProgressChanged(progress)
        seekBar.secondaryProgress = progress.toInt()
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            player.seekTo(progress)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
}
