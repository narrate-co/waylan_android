package space.narrate.waylan.core.ui.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.View
import androidx.appcompat.widget.AppCompatImageButton
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import space.narrate.waylan.core.R
import space.narrate.waylan.core.data.firestore.users.UserAddOn
import space.narrate.waylan.core.data.firestore.users.isValid
import space.narrate.waylan.core.ui.common.AudioClipHelper

class DictionaryEntryAudioHelper(
  private val view: UnderlineActionView,
  private val listener: Listener?
) {

  interface Listener {
    fun onAudioPlayClicked(url: String?)
    fun onAudioStopClicked()
    fun onAudioError(messageRes: Int)
  }

  private var audioState = AudioClipHelper.AudioState.STOPPED
  private var audioUrls: List<String> = listOf()

  private val playPauseButton: AppCompatImageButton = view.findViewById(R.id.image_view)
  private val progressUnderlineView: ProgressUnderlineView = view.findViewById(R.id.underline)

  private var disabledMessageRes: Int = R.string.audio_no_audio_available_error

  private val audioStateDispatchReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      if (intent == null) return

      if (intent.hasExtra(AudioClipHelper.BROADCAST_AUDIO_STATE_EXTRA_URL)) {
        val url = intent.getStringExtra(AudioClipHelper.BROADCAST_AUDIO_STATE_EXTRA_URL);
        if (!audioUrls.contains(url)) {
          // If this event is not one pertaining to any of our audio urls, the broadcast belongs to
          // a different instance of DictionaryEntryAudioHelper and should be ignored.
          return
        }
      }

      audioState = intent.getSerializableExtra(
        AudioClipHelper.BROADCAST_AUDIO_STATE_EXTRA_STATE
      ) as AudioClipHelper.AudioState


      if (audioState == AudioClipHelper.AudioState.ERROR) {
        val messageRes = intent.getIntExtra(
          AudioClipHelper.BROADCAST_AUDIO_STATE_EXTRA_MESSAGE,
          0
        )
        error(messageRes)
      } else {
        handleAudioStateReceived(audioState)
      }
    }
  }

  private val onAttachedStateChangedListener = object : View.OnAttachStateChangeListener {
    override fun onViewAttachedToWindow(v: View) {
      LocalBroadcastManager.getInstance(v.context).registerReceiver(
        audioStateDispatchReceiver,
        IntentFilter(AudioClipHelper.BROADCAST_AUDIO_STATE_DISPATCH)
      )
    }

    override fun onViewDetachedFromWindow(v: View) {
      LocalBroadcastManager.getInstance(v.context).unregisterReceiver(audioStateDispatchReceiver)
      view.removeOnAttachStateChangeListener(this)
    }
  }

  init {
    view.addOnAttachStateChangeListener(onAttachedStateChangedListener)
    playPauseButton.setOnClickListener { handlePlayStopButtonClicked(audioState) }
  }

  fun setEnabled(enabled: Boolean) {
    view.isEnabled = enabled
    view.visibility = if (enabled) View.VISIBLE else View.GONE
    view.alpha = if (enabled) 1F else 0.38F
  }

  /**
   * Set the [MwWordAndDefinitionGroups]s which this view should play pronunciation for when
   * the play button is clicked.
   */
  fun setSources(urls: List<String>, userAddOn: UserAddOn?) {
    if (userAddOn?.isValid == false) {
      setEnabled(false)
      disabledMessageRes = R.string.audio_requires_plugin_purchase_error
      return
    }

    // Disable audio if the user is not valid or there is no audio
    if (urls.isEmpty() ) {
      setEnabled(false)
      disabledMessageRes = R.string.audio_no_audio_available_error
      return
    }

    setEnabled(true)
    playPauseButton.setImageResource(R.drawable.ic_round_play_arrow_24px)

    // TODO: Possibly clean the urls (stripping off file names?)
    this.audioUrls = urls
  }

  private fun play() {
    audioUrls.firstOrNull()?.let { url ->
      listener?.onAudioPlayClicked(url)
    }
  }

  private fun stop() {
    listener?.onAudioStopClicked()
  }

  private fun error(messageRes: Int) {
    listener?.onAudioError(messageRes)
  }

  private fun handleAudioStateReceived(state: AudioClipHelper.AudioState) {
    when (state) {
      AudioClipHelper.AudioState.LOADING -> {
        playPauseButton.setImageResource(R.drawable.ic_round_stop_24px)
        progressUnderlineView.startProgress()
      }
      AudioClipHelper.AudioState.PREPARED,
      AudioClipHelper.AudioState.PLAYING -> {
        playPauseButton.setImageResource(R.drawable.ic_round_stop_24px)
        progressUnderlineView.stopProgress()
      }
      AudioClipHelper.AudioState.STOPPED -> {
        playPauseButton.setImageResource(R.drawable.ic_round_play_arrow_24px)
        progressUnderlineView.stopProgress()
      }
      // TODO: Handle AudioState.Error
    }
  }

  private fun handlePlayStopButtonClicked(state: AudioClipHelper.AudioState) {
    if (!view.isEnabled) {
      listener?.onAudioError(disabledMessageRes)
      return
    }

    when (state) {
      AudioClipHelper.AudioState.LOADING,
      AudioClipHelper.AudioState.PREPARED,
      AudioClipHelper.AudioState.PLAYING -> stop()
      AudioClipHelper.AudioState.STOPPED -> play()
      // TODO: Handle AudioState.Error
    }
  }
}