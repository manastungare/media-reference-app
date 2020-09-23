package com.example.mediareferenceapp

import android.content.Intent
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.mediareferenceapp.State.PAUSED
import com.example.mediareferenceapp.State.PLAYING
import java.lang.System.currentTimeMillis

/** Fragment that houses a [SimulatedPlayer] and ties it to [MediaSession] */
class MediaFragment : Fragment(), SimulatedPlayer.PlaybackListener, Logger {
  private val player = SimulatedPlayer(this as Logger)

  private lateinit var textViewLog: TextView
  private lateinit var textViewPlaybackState: TextView
  private lateinit var mediaSession: MediaSessionCompat

  private var playbackStateBuilder = PlaybackStateCompat.Builder()

  private val mediaMetadata = MediaMetadataCompat.Builder().apply {
    putText(MediaMetadataCompat.METADATA_KEY_TITLE, "Nice Title")
    putLong(MediaMetadataCompat.METADATA_KEY_DURATION, Long.MAX_VALUE)
  }.build()

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    log(TAG, "onActivityCreated")
    super.onActivityCreated(savedInstanceState)
    player.addPlaybackListener(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    log(TAG, "onCreateView")
    val rootView = inflater.inflate(R.layout.fragment_media, container, false)
    textViewLog = rootView.findViewById(R.id.textview_log)
    textViewPlaybackState = rootView.findViewById(R.id.textview_playback_state)

    rootView.findViewById<Button>(R.id.button_play).setOnClickListener { player.play() }
    rootView.findViewById<Button>(R.id.button_pause).setOnClickListener { player.pause() }
    rootView.findViewById<Button>(R.id.button_clear_log).setOnClickListener { textViewLog.text = "" }

    return rootView
  }

  override fun onStart() {
    super.onStart()
    log(TAG, "onStart")
    initializeMediaSession()
  }

  override fun onResume() {
    log(TAG, "onResume")
    super.onResume()
  }

  override fun onPause() {
    log(TAG, "onPause")
    super.onPause()
  }

  override fun onStop() {
    super.onStop()
    log(TAG, "onStop")
    player.pause()
    finishMediaSession()
  }

  override fun onDestroy() {
    log(TAG, "onDestroy")
    super.onDestroy()
    player.release()
  }

  private fun initializeMediaSession() {
    // Playback state
    playbackStateBuilder.apply {
      setState(STATE_PAUSED, 0L, 1F)
      setActions(ACTIONS_WHILE_PAUSED)
    }

    mediaSession = MediaSessionCompat(requireContext(), TAG).apply {
      // setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS)
      setCallback(object : MediaSessionCompat.Callback() {
        override fun onPause() {
          log("MediaSession", "onPause")
          player.pause()
        }

        override fun onPlay() {
          log("MediaSession", "onPlay")
          player.play()
        }

        override fun onStop() {
          log("MediaSession", "onStop")
          player.pause()
        }

        override fun onSeekTo(pos: Long) {
          log("MediaSession", "onSeekTo $pos")
          player.seekTo(pos)
        }

        override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
          if (mediaButtonEvent == null) {
            log("MediaSession", "onMediaButtonEvent: mediaButtonEvent == null")
            return false
          }
          val keyEvent = mediaButtonEvent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
          if (keyEvent == null) {
            log("MediaSession", "onMediaButtonEvent: keyEvent == null")
            return super.onMediaButtonEvent(mediaButtonEvent)
          }
          log("MediaSession", "onMediaButtonEvent: ${getKeyName(keyEvent.keyCode)}")
          return super.onMediaButtonEvent(mediaButtonEvent)
        }

        override fun onCommand(command: String?, extras: Bundle?, cb: ResultReceiver?) {
          log("MediaSession", "onCommand: $command")
          super.onCommand(command, extras, cb)
        }
      })
      setPlaybackState(playbackStateBuilder.build())
      setMetadata(mediaMetadata)
      isActive = true
    }
    MediaControllerCompat.setMediaController(requireActivity(), mediaSession.controller)
  }

  private fun finishMediaSession() {
    mediaSession.isActive = false
    mediaSession.release()
  }

  override fun onStateChanged(newState: State) {
    when (newState) {
      PLAYING -> {
        playbackStateBuilder.apply {
          setState(STATE_PLAYING, player.currentPosition, 1F)
          setActions(ACTIONS_WHILE_PLAYING)
        }
      }
      PAUSED -> {
        playbackStateBuilder.apply {
          setState(STATE_PAUSED, player.currentPosition, 1F)
          setActions(ACTIONS_WHILE_PAUSED)
        }
      }
    }
    mediaSession.setPlaybackState(playbackStateBuilder.build())
    textViewPlaybackState.text = "onStateChanged $newState"
    log(TAG, "onStateChanged $newState")
  }

  override fun onProgressUpdated(currentPosition: Long) {
    playbackStateBuilder.setState(STATE_PLAYING, player.currentPosition, 1F)
    mediaSession.setPlaybackState(playbackStateBuilder.build())
    textViewPlaybackState.text = "Playback now at ${player.currentPosition}"
  }

  override fun log(tag: String, message: String) {
    val timestamp = currentTimeMillis().toIso8601Time()
    if (::textViewLog.isInitialized) {
      val existingText = textViewLog.text.toString()
      textViewLog.text = "[$timestamp] $TAG.$message\n$existingText"
    }
    Log.w(TAG, "${currentTimeMillis().toIso8601Time()} $message")
  }

  companion object {
    private const val TAG = "MediaFragment"

    private const val ACTIONS_WHILE_PAUSED = ACTION_PLAY_PAUSE or ACTION_PLAY or ACTION_STOP or ACTION_SEEK_TO

    private const val ACTIONS_WHILE_PLAYING = ACTION_PLAY_PAUSE or ACTION_PAUSE or ACTION_STOP or ACTION_SEEK_TO
  }
}

fun getKeyName(keyCode: Int) = when (keyCode) {
  KEYCODE_MEDIA_PLAY -> "PLAY"
  KEYCODE_MEDIA_PLAY_PAUSE -> "PLAY_PAUSE"
  KEYCODE_MEDIA_STOP -> "STOP"
  KEYCODE_MEDIA_PAUSE -> "PAUSE"
  KEYCODE_DPAD_LEFT -> "DPAD_LEFT"
  KEYCODE_DPAD_DOWN -> "DPAD_DOWN"
  KEYCODE_DPAD_RIGHT -> "DPAD_RIGHT"
  KEYCODE_DPAD_UP -> "DPAD_UP"
  else -> keyCode.toString()
}
