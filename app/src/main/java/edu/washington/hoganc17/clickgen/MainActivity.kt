package edu.washington.hoganc17.clickgen

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.musicg.wave.Wave
import edu.washington.hoganc17.clickgen.fragment.PlayerFragment
import edu.washington.hoganc17.clickgen.fragment.UploadFragment
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


// Code for this media player was created with help from the example at:
// https://www.javatpoint.com/kotlin-android-media-player

class MainActivity : AppCompatActivity(), OnUploadListener {

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

    override fun onFileUploaded() {
        val playerFragment = PlayerFragment()

        supportFragmentManager
                .beginTransaction()
                .add(R.id.fragContainer, playerFragment, PlayerFragment.TAG)
                .addToBackStack(PlayerFragment.TAG)
                .commit()
    }
}