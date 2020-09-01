package edu.washington.hoganc17.clickgen.fragment

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import edu.washington.hoganc17.clickgen.MainActivity
import edu.washington.hoganc17.clickgen.R
import edu.washington.hoganc17.clickgen.model.MyWaveHeader
import kotlinx.android.synthetic.main.fragment_player.*
import java.io.File
import java.io.FileOutputStream


class PlayerFragment: Fragment() {

    companion object {
        val TAG: String = PlayerFragment::class.java.simpleName
        const val OUT_BYTES = "OUT_BYTES"
        const val DIR_CHOICE_CODE = 1234
    }

    private lateinit var songPlayer: MediaPlayer
    private lateinit var runnable: Runnable
    private var handler: Handler = Handler()

    private var paused = false
    private var stopped = true
    private var playerReleased = true

    lateinit var clickTrack: ByteArray


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mixedTracks = arguments?.getByteArray(OUT_BYTES)

        mixedTracks?.let {track ->

            clickTrack = track

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

        btnSaveFile.setOnClickListener {
            saveFile()
        }

        btnUploadAgain.setOnClickListener {
            (activity as MainActivity).onBackPressed()
        }

        seekBarTime.setOnSeekBarChangeListener(

                object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                    ) {
                        if (fromUser && !stopped) {
                            songPlayer.seekTo(progress * 1000)
                        }
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

            val pathName = context?.applicationContext?.filesDir?.path

            songPlayer.setDataSource("$pathName/mixed_sounds.wav")
            songPlayer.prepare()
            songPlayer.start()

            playerReleased = false

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
            paused = false
            stopped = true

            songPlayer.stop()
            songPlayer.reset()
            songPlayer.release()
            playerReleased = true

            handler.removeCallbacks(runnable)

            seekBarTime.progress = 0
            tvCurrTime.text = "0:00"

            btnStop.isEnabled = false
            btnPlay.setImageResource(R.drawable.play_button)
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

    fun stopPlayer() {
        if(this::songPlayer.isInitialized) {
            if (songPlayer.isPlaying || paused) {
                paused = false
                stopped = true

                songPlayer.stop()
                songPlayer.reset()
                songPlayer.release()
                playerReleased = true

                handler.removeCallbacks(runnable)

                seekBarTime.progress = 0
                tvCurrTime.text = "0:00"

                btnStop.isEnabled = false
                btnPlay.setImageResource(R.drawable.play_button)
            }
        }
    }

    private fun saveFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = "audio/x-wav"
        intent.putExtra(Intent.EXTRA_TITLE, "MyClickTrack.wav")
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, DIR_CHOICE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == DIR_CHOICE_CODE && resultCode == Activity.RESULT_OK) {
            val uri = data?.data

            uri?.let {
                val fo = context?.contentResolver?.openOutputStream(uri)
                fo?.let {
                    val mwh = MyWaveHeader(clickTrack.size)
                    mwh.write(fo)


                    fo.write(clickTrack)
                    fo.flush()
                    fo.close()
                }
            }

            Toast.makeText(context, "Click track saved!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "There was a problem saving your file", Toast.LENGTH_SHORT).show()
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }
}