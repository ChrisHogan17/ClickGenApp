package edu.washington.hoganc17.clickgen.fragment

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.musicg.wave.Wave
import edu.washington.hoganc17.clickgen.model.AudioManager
import edu.washington.hoganc17.clickgen.model.MyWaveHeader
import edu.washington.hoganc17.clickgen.R
import edu.washington.hoganc17.clickgen.model.AudioTrio
import kotlinx.android.synthetic.main.fragment_player.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class PlayerFragment: Fragment() {

    companion object {
        val TAG: String = PlayerFragment::class.java.simpleName
        val OUT_BYTES = "OUT_BYTES"
    }

    private lateinit var songPlayer: MediaPlayer
    private lateinit var runnable: Runnable
    private var handler: Handler = Handler()

    private var paused = false
    private var stopped = true
    private var playerReleased = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mixedTracks = arguments?.getByteArray(OUT_BYTES)

        mixedTracks?.let {track ->

            val pathName = context?.applicationContext?.filesDir?.path

            val f = File("$pathName/mixed_sounds.wav")
            if (f.exists()) {
                f.delete()
            }

            val fo = FileOutputStream(f)

            val mwh = MyWaveHeader(track.size)
            mwh.write(fo)

            fo.write(track)
            fo.flush()
            fo.close()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnPlay.setOnClickListener {
            if (stopped || paused) {
                playSong()
            } else {
                pauseSong()
            }
        }

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
        if (stopped) { // Start MediaPlayer
            songPlayer = MediaPlayer()
            //songPlayer = MediaPlayer.create(this, R.raw.another_one_click)

            val pathName = context?.applicationContext?.filesDir?.path

            songPlayer.setDataSource("$pathName/mixed_sounds.wav")
            songPlayer.prepare()
            songPlayer.start()

            playerReleased = false

            songPlayer.setVolume(.5f, .5f)
            seekBarSongVolume.progress = 50

            btnStop.isEnabled = true

            stopped = false

            tvTotalTime.text = formatMinSec(songPlayer.duration)
            tvCurrTime.text = formatMinSec(songPlayer.currentPosition)

        } else if (paused) {
            songPlayer.seekTo(songPlayer.currentPosition)
            songPlayer.start()

            paused = false
        }

        btnPlay.setImageResource(R.drawable.pause_button)

        initializeTimeSeekBar()

        songPlayer.setOnCompletionListener {
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
        if (this::songPlayer.isInitialized) {

            if (!playerReleased && songPlayer.isPlaying) {
                songPlayer.pause()
                paused = true

            }

            btnPlay.setImageResource(R.drawable.play_button)
        }
    }

    private fun stopPlayer() {
        if (songPlayer.isPlaying || paused) {
            paused = false
            stopped = true

            songPlayer.stop()
            songPlayer.reset()
            songPlayer.release()
            playerReleased = true

            handler.removeCallbacks(runnable)

            seekBarTime.progress = 0
            tvCurrTime.text = ""
            tvTotalTime.text = ""

            btnStop.isEnabled = false
            btnPlay.setImageResource(R.drawable.play_button)
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