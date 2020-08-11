package edu.washington.hoganc17.clickgen

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


// Code for this media player was created with help from the example at:
// https://www.javatpoint.com/kotlin-android-media-player

class MainActivity : AppCompatActivity() {

    private lateinit var player: MediaPlayer
    private lateinit var runnable: Runnable
    private var handler: Handler = Handler()
    private var pause: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnPlay.setOnClickListener {
            playSong()
        }

        btnPause.setOnClickListener {
            pauseSong()
        }

        btnStop.setOnClickListener {
            stopPlayer()
        }

        seekBarTime.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (b) {
                    player.seekTo(i * 1000)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })

    }

    private fun playSong() {
        if (pause) {
            player.seekTo(player.currentPosition)
            player.start()
            pause = false
        } else {
            player = MediaPlayer.create(this, R.raw.robots)
            player.start()
            tvRemainingTime.text = formatMinSec(player.duration)
            tvCurrTime.text = formatMinSec(player.currentPosition)
        }

        initializeSeekBar()

        player.setOnCompletionListener {
            // UI Changes
        }
    }

    private fun initializeSeekBar() {
        seekBarTime.max = player.duration / 1000

        runnable = Runnable {
            seekBarTime.progress = player.currentPosition / 1000
            tvCurrTime.text = formatMinSec(player.currentPosition)

            handler.postDelayed(runnable, 1000)
        }
        handler.postDelayed(runnable, 1000)
    }

    private fun pauseSong() {
        if (player.isPlaying) {
            player.pause()
            pause = true
        }
    }

    private fun stopPlayer() {
        if (player.isPlaying || pause) {
            pause = false
            player.stop()
            player.reset()
            player.release()
            handler.removeCallbacks(runnable)

            seekBarTime.progress = 0
            tvCurrTime.text = ""
            tvRemainingTime.text = ""

        }
    }

    private fun formatMinSec(millis: Int): String {
        val min = millis / 1000 / 60
        val sec = millis / 1000 % 60
        var duration = "$min:"
        if (sec < 10) duration += "0"
        duration += sec
        return duration
    }
}