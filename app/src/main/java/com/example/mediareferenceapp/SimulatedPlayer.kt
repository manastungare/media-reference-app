package com.example.mediareferenceapp

import android.media.AudioTrack
import android.os.Handler
import com.example.mediareferenceapp.State.PAUSED
import com.example.mediareferenceapp.State.PLAYING

/**
 * Very simple playback simulator. Starts in a paused state and you can trigger playback with [play]. Methods that
 * trigger state changes like [play] and [pause] no-op when already in that state.
 */
class SimulatedPlayer(private val logger: Logger) {
  private val audioTrack = AudioTrack.Builder().build()

  var currentPosition = 0L
  var playbackState = PAUSED

  private var lastTimestamp = 0L
  private val listeners = mutableListOf<PlaybackListener>()
  private val handler = Handler()

  private val simulationUpdateRunnable = Runnable {
    updatePlaybackSimulation()
    scheduleSimulationUpdateRunnable()
  }

  private fun scheduleSimulationUpdateRunnable() {
    handler.postDelayed(simulationUpdateRunnable, SIMULATION_INTERVAL_MS)
  }

  private fun cancelSimulationUpdateRunnable() {
    updatePlaybackSimulation()  // One last update.
    handler.removeCallbacks(simulationUpdateRunnable)
  }

  private fun updatePlaybackSimulation() {
    if (playbackState != PLAYING) {
      logger.log(TAG, "playbackState != PLAYING")
      return
    }

    currentPosition += System.currentTimeMillis() - lastTimestamp
    listeners.forEach { it.onProgressUpdated(currentPosition) }
  }

  fun addPlaybackListener(listener: PlaybackListener) = listeners.add(listener)

  fun pause() {
    if (playbackState == PAUSED) {
      logger.log(TAG, "play: playbackState == PAUSED")
      return
    }

    playbackState = PAUSED
    audioTrack.pause()
    listeners.forEach { it.onStateChanged(playbackState) }
    cancelSimulationUpdateRunnable()
  }

  fun play() {
    if (playbackState == PLAYING) {
      logger.log(TAG, "play: playbackState == PLAYING")
      return
    }

    scheduleSimulationUpdateRunnable()
    playbackState = PLAYING
    audioTrack.play()
    lastTimestamp = System.currentTimeMillis()
    listeners.forEach { it.onStateChanged(playbackState) }
  }

  fun seekTo(newPosition: Long) {
    currentPosition = newPosition
    lastTimestamp = System.currentTimeMillis()
    listeners.forEach { it.onProgressUpdated(currentPosition) }
  }

  fun release() {
    audioTrack.release()
    listeners.clear()
  }

  interface PlaybackListener {
    fun onStateChanged(newState: State)
    fun onProgressUpdated(currentPosition: Long)
  }

  companion object {
    private const val TAG = "SimulatedPlayer"

    private const val SIMULATION_INTERVAL_MS = 100L
  }
}

enum class State { PAUSED, PLAYING }
