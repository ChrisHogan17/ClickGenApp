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

    override fun onFileUploaded(songInputStream: InputStream, clickInputStream: InputStream) {
        Log.i("HULK", "Activity")


        val w1 = Wave(songInputStream)
        val songSampleAmps: ShortArray = w1.sampleAmplitudes
        songInputStream.close()

        val w2 = Wave(clickInputStream)
        val clickSampleAmps: ShortArray = w2.sampleAmplitudes
        clickInputStream.close()

        val mixedTracks = audioManager.mixAmplitudesSixteenBit(songSampleAmps, clickSampleAmps)

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