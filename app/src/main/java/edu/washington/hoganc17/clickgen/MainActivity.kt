package edu.washington.hoganc17.clickgen

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var mp: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnPlay.setOnClickListener {
            if (mp == null) {
                mp = MediaPlayer.create(this, R.raw.robots)
            }
            mp?.start()
        }

        btnPause.setOnClickListener {
            mp?.pause()
        }

        btnStop.setOnClickListener {
            mp?.let {
                it.release()
                mp = null
            }
        }
    }
}