package com.example.mediareferenceapp

import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.FragmentActivity

/** Loads [MediaFragment]. */
class MainActivity : FragmentActivity(), Logger {

  private lateinit var mediaFragment: MediaFragment

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    mediaFragment = supportFragmentManager.findFragmentById(R.id.media_fragment) as MediaFragment
  }

  override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
    log(TAG, "onKeyDown ${getKeyName(keyCode)}")
    return super.onKeyDown(keyCode, event)
  }

  override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
    log(TAG, "onKeyUp ${getKeyName(keyCode)}")
    return super.onKeyUp(keyCode, event)
  }

  override fun log(tag: String, message: String) = mediaFragment.log(TAG, message)

  companion object {
    private const val TAG = "MainActivity"
  }
}
