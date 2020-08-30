package edu.washington.hoganc17.clickgen

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import edu.washington.hoganc17.clickgen.fragment.PlayerFragment
import edu.washington.hoganc17.clickgen.fragment.UploadFragment
import edu.washington.hoganc17.clickgen.model.AudioTrio
import edu.washington.hoganc17.clickgen.model.OnUploadListener


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

    override fun onFileUploaded(trio: AudioTrio) {
        val playerFragment = PlayerFragment()

        supportFragmentManager
                .beginTransaction()
                .add(R.id.fragContainer, playerFragment, PlayerFragment.TAG)
                .addToBackStack(PlayerFragment.TAG)
                .commit()
    }
}