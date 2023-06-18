package com.example.audiorecorderparkinsonapp

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.appbar.MaterialToolbar
import java.text.DecimalFormat
import java.text.NumberFormat

class AudioPlayerActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvFileName: TextView

    private lateinit var tvTrackProgress: TextView
    private lateinit var tvTrackDuration: TextView

    private lateinit var btnPlay: ImageButton
    private lateinit var btnBackward: ImageButton
    private lateinit var btnForward: ImageButton
    private lateinit var seekBar: SeekBar

    private lateinit var runnable: Runnable
    private lateinit var handler: Handler
    private var delay = 1000L

    private var jumpValue = 1000


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        var filePath = intent.getStringExtra("filepath")
        var fileName = intent.getStringExtra("filename")

        toolbar = findViewById(R.id.toolbar)
        tvFileName = findViewById(R.id.tvFileName)

        tvTrackDuration = findViewById(R.id.tvTrackDuration)
        tvTrackProgress = findViewById(R.id.tvTrackProgress)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        tvFileName.text = fileName

        mediaPlayer = MediaPlayer()
        mediaPlayer.apply {
            setDataSource(filePath)
            prepare()
        }

        tvTrackDuration.text = dateFormat(mediaPlayer.duration)
        btnBackward = findViewById(R.id.btnBackwards)
        btnForward = findViewById(R.id.btnForward)
        btnPlay = findViewById(R.id.btnPlay)
        seekBar = findViewById(R.id.seekBar)

        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            seekBar.progress = mediaPlayer.currentPosition
            tvTrackProgress.text = dateFormat(mediaPlayer.currentPosition)
            handler.postDelayed(runnable, delay)
        }

        btnPlay.setOnClickListener {
            playPausePlayer()
        }

        playPausePlayer()
        seekBar.max = mediaPlayer.duration

        mediaPlayer.setOnCompletionListener {
            btnPlay.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_play_circle, theme)
            handler.removeCallbacks(runnable)
        }

        btnForward.setOnClickListener {
            mediaPlayer.seekTo(mediaPlayer.currentPosition + jumpValue)
            seekBar.progress += jumpValue
        }

        btnBackward.setOnClickListener {
            mediaPlayer.seekTo(mediaPlayer.currentPosition - jumpValue)
            seekBar.progress -= jumpValue
        }

        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2) {
                    mediaPlayer.seekTo(p1)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                TODO("Not yet implemented")
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun playPausePlayer() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
            btnPlay.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_pause, theme)
            handler.postDelayed(runnable, delay)
        } else {
            mediaPlayer.pause()
            btnPlay.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_play_circle, theme)
            handler.removeCallbacks(runnable)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mediaPlayer.stop()
        mediaPlayer.release()
        handler.removeCallbacks(runnable)
    }

    private fun dateFormat(duration: Int): String {
        var d = duration / 1000
        var s = d % 60
        var m = (d/60 % 60)
        var h = ((d - m* 60)/360).toInt()

        val f: NumberFormat = DecimalFormat("00")
        var str = "$m:${f.format(s)}"
        if (h > 0) {
            str = "$h:$str"
        }
        return str
    }
}