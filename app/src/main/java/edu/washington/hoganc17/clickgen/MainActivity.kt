package edu.washington.hoganc17.clickgen

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
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

class MainActivity : AppCompatActivity(), OnUploadListener, AdapterView.OnItemSelectedListener {

    private val audioManager = AudioManager()
    private var clickFrequency = 880.0f
    private var clickDuration = 0.5f

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

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val itemContent = parent?.getItemAtPosition(position).toString()
        val numericalVal = itemContent.split(" ")[0]
        Log.i("BRIT", numericalVal)
        if(parent?.id == R.id.spinnerDuration) {
            clickDuration = numericalVal.toFloat()
        } else if (parent?.id == R.id.spinnerFrequency) {
            clickFrequency = numericalVal.toFloat()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }
}
