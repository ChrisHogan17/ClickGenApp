package edu.washington.hoganc17.clickgen

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.musicg.wave.Wave
import edu.washington.hoganc17.clickgen.fragment.PlayerFragment
import edu.washington.hoganc17.clickgen.fragment.UploadFragment
import edu.washington.hoganc17.clickgen.model.AudioManager
import edu.washington.hoganc17.clickgen.model.AudioTrio
import edu.washington.hoganc17.clickgen.model.OnUploadListener
import java.io.InputStream


// Code for this media player was created with help from the example at:
// https://www.javatpoint.com/kotlin-android-media-player

class MainActivity : AppCompatActivity(), OnUploadListener {

    private val audioManager = AudioManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val uploadFragment = UploadFragment()

        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragContainer, uploadFragment, UploadFragment.TAG)
            .commit()

        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount > 1) {
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            } else {
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
            }
        }
    }

    // Process for generating the click track on the application
    override fun onFileUploaded(trio: AudioTrio) {
        val times = trio.beatsArray.toIntArray()
        val sampleRate = trio.sr.toFloat()
        val songInputStream = trio.inputStream

        val w1 = Wave(songInputStream)
        val songSampleAmps: ShortArray = w1.sampleAmplitudes
        songInputStream.close()

        // times and sr are given by the server. click_freq and click_dur will be set by the user
        val clickFreq = 880.0f
        val clickDur = 0.5f
        val length = songSampleAmps.size

        val clickSampleAmps = audioManager.generateClicktrack(times, sampleRate, clickFreq, clickDur, length)

        val doubledClickAmps = ShortArray(songSampleAmps.size)
        var c = 0
        for (i in clickSampleAmps.indices) {
            doubledClickAmps[c] = clickSampleAmps[i]
            c += 2
        }

        val mixedTracks = audioManager.mixAmplitudesSixteenBit(songSampleAmps, doubledClickAmps)

        val bundle = Bundle()
        bundle.putByteArray(PlayerFragment.OUT_BYTES, mixedTracks)

        val playerFragment = PlayerFragment()
        playerFragment.arguments = bundle

        supportFragmentManager
                .beginTransaction()
                .add(R.id.fragContainer, playerFragment, PlayerFragment.TAG)
                .addToBackStack(PlayerFragment.TAG)
                .commit()
    }


    // Process for when the server generates the click track
    override fun onFileUploaded(songStream: InputStream, clickStream: InputStream) {

        val w1 = Wave(songStream)
        val songSampleAmps: ShortArray = w1.sampleAmplitudes
        songStream.close()

        val w2 = Wave(clickStream)
        val clickSampleAmps: ShortArray = w2.sampleAmplitudes
        clickStream.close()

        val doubledClickAmps = ShortArray(songSampleAmps.size)
        var c = 0
        for (i in clickSampleAmps.indices) {
            doubledClickAmps[c] = clickSampleAmps[i]
            c += 2
        }

        val mixedTracks = audioManager.mixAmplitudesSixteenBit(songSampleAmps, doubledClickAmps)

        val bundle = Bundle()
        bundle.putByteArray(PlayerFragment.OUT_BYTES, mixedTracks)

        val playerFragment = PlayerFragment()
        playerFragment.arguments = bundle

        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragContainer, playerFragment, PlayerFragment.TAG)
            .addToBackStack(PlayerFragment.TAG)
            .commit()
    }
}