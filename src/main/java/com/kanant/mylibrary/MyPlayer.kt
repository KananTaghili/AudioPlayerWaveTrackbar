package com.kanant.mylibrary

import android.media.MediaPlayer
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class MyPlayer(
    private val viewLifecycleOwner: LifecycleOwner
) {
    private var mediaPlayer: MediaPlayer? = null
    private var job: Job? = null
    private var uri: String? = null

    private lateinit var onStartPlaying: () -> Unit
    private lateinit var onStopPlaying: () -> Unit
    private lateinit var onResume: (Float) -> Unit

    var paused = false
        private set

    fun injectMedia(
        audioUri: String?,
        onStartPlaying: () -> Unit,
        onStopPlaying: () -> Unit,
        onResume: (Float) -> Unit
    ) {
        stopPlaying()
        this.onStartPlaying = onStartPlaying
        this.onStopPlaying = onStopPlaying
        this.onResume = onResume
        this.uri = audioUri
        mediaPlayer?.reset()
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer()
        if (audioUri != null && File(audioUri).exists()) {
            mediaPlayer?.let {
                it.setDataSource(audioUri)
                it.prepare()
                it.setOnCompletionListener {
                    onStopPlaying.invoke()
                    onResume.invoke(0F)
                    job?.cancel()
                }
            }
        }
    }

    fun startPlayingFrom(progress: Float) {
        if (uri != null && File(uri!!).exists()) {
            job = viewLifecycleOwner.lifecycleScope.launch {
                mediaPlayer?.let {
                    onStartPlaying.invoke()
                    paused = false

                    val currentDuration = it.duration * progress / 100
                    it.seekTo(currentDuration.toInt())
                    it.start()

                    while (true) {
                        val progressPercentage = it.currentPosition.toFloat() / it.duration * 100
                        onResume.invoke(progressPercentage)
                        delay(50)
                    }
                }
            }
        }
    }

    fun stopPlaying(paused: Boolean = false) {
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            this.paused = paused
            onStopPlaying.invoke()
            mediaPlayer!!.pause()
            job?.cancel()
        }
    }
}