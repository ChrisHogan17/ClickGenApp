package edu.washington.hoganc17.clickgen

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.musicg.wave.Wave
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


// Code for this media player was created with help from the example at:
// https://www.javatpoint.com/kotlin-android-media-player

class MainActivity : AppCompatActivity() {

    private lateinit var songPlayer: MediaPlayer
    private lateinit var runnable: Runnable
    private var handler: Handler = Handler()
    private var paused = false
    private var playerReleased = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val theDust = resources.openRawResource(R.raw.bites_dust_16)
        val theClick = resources.openRawResource(R.raw.bites_click_16)

        mixSound(theDust, theClick)

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
            songPlayer = MediaPlayer()
            //songPlayer = MediaPlayer.create(this, R.raw.another_one_click)
            songPlayer.setDataSource(applicationContext.filesDir.path + "/mixed_sounds.wav")
            songPlayer.prepare()
            songPlayer.start()

            playerReleased = false

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
        if (this::songPlayer.isInitialized) {

            if (!playerReleased && songPlayer.isPlaying) {
                songPlayer.pause()
                paused = true

                btnPause.isEnabled = false
                btnPlay.isEnabled = true
            }
        }
    }

    private fun stopPlayer() {
        if (songPlayer.isPlaying || paused) {
            paused = false

            songPlayer.stop()
            songPlayer.reset()
            songPlayer.release()
            playerReleased = true

            handler.removeCallbacks(runnable)

            seekBarTime.progress = 0
            tvCurrTime.text = ""
            tvTotalTime.text = ""

            btnPlay.isEnabled = true
            btnPause.isEnabled = false
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



    private fun mixSound(trackOne: InputStream, trackTwo: InputStream) {

        val w1 = Wave(trackOne)
        val music1: ShortArray = w1.sampleAmplitudes
        trackOne.close()

        val w2 = Wave(trackTwo)
        val music2: ShortArray = w2.sampleAmplitudes
        trackTwo.close()



        // 8 bit file + 8 bit file = 8 bit file
        /*
        // Code from: https://stackoverflow.com/questions/40929243/how-to-mix-two-wav-files-without-noise

        val output = ByteArray(if (music1.size > music2.size) music2.size else music1.size)

        for (i in output.indices) {
            val samplef1 = music1[i] / 128.0f * 1.2f // 2^7=128
            val samplef2 = music2[i] / 128.0f * 1.2f
            val mixed = (samplef1 + samplef2) / 2
            val outputSample = (mixed * 128.0f).toByte()
            output[i] = outputSample
        }
        */

        // Our Code
        // 16 bit + 16 bit = 16 bit file

        // Since each value in the sampleAmplitudes is 2 bytes
        // Make output byte array the length of the longest array * 2
        val output = ByteArray(if (music1.size > music2.size) music2.size * 2 else music1.size * 2)

        var outputIndex = 0
        for (i in music1.indices) {
            // Numbers borrowed from Stack Overflow Code
            // Needs fine tuning as we don't really understand why the values are what they are
            val sample1 = music1[i] / 128.0f * 1.2f
            val sample2 = music2[i] / 250.0f

            // Average the altitude of each sample into one value
            val mixed = (sample1 + sample2) / 2
            // Convert to binaryString
            var mixedString = Integer.toBinaryString(mixed.toInt())

            // Pad each value so they all have 16 bits
            mixedString = mixedString.padStart(16, '0')

            // Trim off overflow bits
            mixedString = mixedString.substring(mixedString.length - 16)

            // Separate 2 byte value into individual bytes
            val byte1 = mixedString.substring(0, 8)
            val byte2 = mixedString.substring(8, 16)

            // Add bytes sequentially to output byte array
            output[outputIndex] = Integer.parseInt(byte1, 2).toByte()
            output[outputIndex + 1] = Integer.parseInt(byte2, 2).toByte()
            outputIndex += 2
        }

        val f = File(applicationContext.filesDir.path + "/mixed_sounds.wav")
        if (f.exists()) {
            f.delete()
        }

        val fo = FileOutputStream(f)

        val wh = w2.waveHeader
        val mwh = MyWaveHeader(wh.audioFormat.toShort(),
                wh.channels.toShort(), wh.sampleRate,
                wh.bitsPerSample.toShort(), output.size)
        mwh.write(fo)

        fo.write(output)
        fo.flush()
        fo.close()
    }
}