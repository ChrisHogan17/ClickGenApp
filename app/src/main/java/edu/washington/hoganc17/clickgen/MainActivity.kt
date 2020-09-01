package edu.washington.hoganc17.clickgen

import android.os.Bundle
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
    private var clickFrequency = 880.0f
    private var clickDuration = 0.5f
    private var shouldAllowBack = true

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val uploadFragment = UploadFragment()

        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragContainer, uploadFragment, UploadFragment.TAG)
            .commit()
    }

    // Process for generating the click track on the application
    override fun onFileUploaded(trio: AudioTrio) {
        val times = trio.beatsArray.toIntArray()
        val sampleRate = trio.sr.toFloat()
        val songInputStream = trio.inputStream

        val w1 = Wave(songInputStream)
        val songSampleAmps: ShortArray = w1.sampleAmplitudes
        songInputStream.close()

        // times and sr are given by the server
        val length = songSampleAmps.size

        val clickSampleAmps = audioManager.generateClicktrack(times, sampleRate, clickFrequency, clickDuration, length)

        val doubledClickAmps = ShortArray(songSampleAmps.size)
        var c = 0
        for (i in clickSampleAmps.indices) {
            doubledClickAmps[c] = clickSampleAmps[i]
            doubledClickAmps[c+1] = clickSampleAmps[i]
            c += 2
        }

        val mixedTracks = audioManager.mixAmplitudesSixteenBit(doubledClickAmps, doubledClickAmps)

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


    // Process for when the server generates the click track and we combine it with song on app
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
            doubledClickAmps[c+1] = clickSampleAmps[i]
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

    // Process for when the server returns the combined track
    override fun onFileUploaded(mixedStream: InputStream, title: String?) {
        val mixedBytes = ByteArray(mixedStream.available())
        mixedStream.read(mixedBytes)

        val bundle = Bundle()

        bundle.putByteArray(PlayerFragment.OUT_BYTES, mixedBytes)
        bundle.putString(PlayerFragment.OUT_TITLE, title)

        val playerFragment = PlayerFragment()
        playerFragment.arguments = bundle

        shouldAllowBack = false

        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragContainer, playerFragment, PlayerFragment.TAG)
            .addToBackStack(PlayerFragment.TAG)
            .commit()
    }

    override fun onBackPressed() {
        // If there is a playerFragment running, stop it gracefully before going back to avoid crashing
        val playerFragment = supportFragmentManager.findFragmentByTag(PlayerFragment.TAG) as? PlayerFragment

        playerFragment?.let { playFrag ->
            playFrag.stopPlayer()

            // If there is an upload fragment (which there should be) reset it to normal
            val uploadFragment = supportFragmentManager.findFragmentByTag(UploadFragment.TAG) as? UploadFragment
            uploadFragment?.setUploadUi()
        }

        super.onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }

}
