package edu.washington.hoganc17.clickgen

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


// Code for this media player was created with help from the example at:
// https://www.javatpoint.com/kotlin-android-media-player

class MainActivity : AppCompatActivity() {

    private lateinit var songPlayer: MediaPlayer
    private lateinit var runnable: Runnable
    private var handler: Handler = Handler()
    private var paused: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnPlay.setOnClickListener {
            playSong()
        }

        btnPause.setOnClickListener {
            pauseSong()
        }
        btnPause.isEnabled = false

        btnStop.setOnClickListener {
            stopPlayer()
        }
        btnStop.isEnabled = false

        seekBarTime.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        songPlayer.seekTo(progress * 1000)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            }
        )

        seekBarSongVolume.setOnSeekBarChangeListener(
            object: SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val volumeNum = progress / 100f
                    songPlayer.setVolume(volumeNum, volumeNum)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            }
        )
    }

    private fun playSong() {
        if (paused) {
            songPlayer.seekTo(songPlayer.currentPosition)
            songPlayer.start()

            btnPlay.isEnabled = false
            btnPause.isEnabled = true
            paused = false

        } else {
            songPlayer = MediaPlayer.create(this, R.raw.another_one_click)
            songPlayer.start()

            songPlayer.setVolume(.5f, .5f)
            seekBarSongVolume.progress = 50

            btnPlay.isEnabled = false
            btnPause.isEnabled = true
            btnStop.isEnabled = true

            tvTotalTime.text = formatMinSec(songPlayer.duration)
            tvCurrTime.text = formatMinSec(songPlayer.currentPosition)
        }

        initializeTimeSeekBar()

        songPlayer.setOnCompletionListener {
            btnPlay.isEnabled = true
            btnPause.isEnabled = false
            btnStop.isEnabled = false
        }
    }

    private fun initializeTimeSeekBar() {
        seekBarTime.max = songPlayer.duration / 1000

        runnable = Runnable {
            seekBarTime.progress = songPlayer.currentPosition / 1000
            tvCurrTime.text = formatMinSec(songPlayer.currentPosition)

            handler.postDelayed(runnable, 1000)
        }
        handler.postDelayed(runnable, 1000)
    }

    private fun pauseSong() {
        if (songPlayer.isPlaying) {
            songPlayer.pause()
            paused = true

            btnPause.isEnabled = false
            btnPlay.isEnabled = true
        }
    }

    private fun stopPlayer() {
        if (songPlayer.isPlaying || paused) {
            paused = false

            songPlayer.stop()
            songPlayer.reset()
            songPlayer.release()

            handler.removeCallbacks(runnable)

            seekBarTime.progress = 0
            tvCurrTime.text = ""
            tvTotalTime.text = ""

            btnStop.isEnabled = false
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

    override fun onPause() {
        super.onPause()
        pauseSong()
    }
}