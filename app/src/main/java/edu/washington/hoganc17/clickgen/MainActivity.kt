package edu.washington.hoganc17.clickgen

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.musicg.wave.Wave
import edu.washington.hoganc17.clickgen.fragment.PlayerFragment
import edu.washington.hoganc17.clickgen.fragment.UploadFragment
import edu.washington.hoganc17.clickgen.model.AudioManager
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

    override fun onFileUploaded(songStream: InputStream, clickStream: InputStream) {
        Log.i("HULK", "Activity")

        val w1 = Wave(songStream)
        val songSampleAmps: ShortArray = w1.sampleAmplitudes
        songStream.close()

        val w2 = Wave(clickStream)
        val clickSampleAmps: ShortArray = w2.sampleAmplitudes
        clickStream.close()

        val shorty = ShortArray(songSampleAmps.size)
        var c = 0
        for (i in clickSampleAmps.indices) {
            shorty[c] = clickSampleAmps[i]
            c += 2
        }

        Log.i("AGONY", songSampleAmps.size.toString())
        Log.i("AGONY", shorty.size.toString())
        val mixedTracks = audioManager.mixAmplitudesSixteenBit(songSampleAmps, shorty)

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