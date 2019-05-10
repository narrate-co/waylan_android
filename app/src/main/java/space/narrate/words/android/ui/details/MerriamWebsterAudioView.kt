package space.narrate.words.android.ui.details

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageButton
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import space.narrate.words.android.R
import space.narrate.words.android.data.disk.mw.MwWordAndDefinitionGroups
import space.narrate.words.android.data.firestore.users.User
import space.narrate.words.android.data.firestore.users.merriamWebsterState
import space.narrate.words.android.util.widget.ProgressUnderlineView

/**
 * A composite view which shows a play/stop button above a [ProgressUnderlineView].
 */
class MerriamWebsterAudioView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    interface Listener {
        fun onAudioPlayClicked(url: String)
        fun onAudioStopClicked()
        fun onAudioError(messageRes: Int)
    }

    var listener: Listener? = null

    private var audioState = AudioClipHelper.AudioState.STOPPED
    private var audioUrls: List<String> = listOf()

    private val playPauseButton: AppCompatImageButton
    private val progressUnderlineView: ProgressUnderlineView

    private var disabledMessageRes: Int = R.string.mw_audio_view_no_audio_available_error

    private val audioStateDispatchReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return

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

    init {
        orientation = LinearLayout.VERTICAL

        val view = View.inflate(context, R.layout.merriam_webster_audio_view_layout, this)
        playPauseButton = view.findViewById(R.id.play_pause_image_button)
        progressUnderlineView = view.findViewById(R.id.underline)

        playPauseButton.setOnClickListener { handlePlayStopButtonClicked(audioState) }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        LocalBroadcastManager.getInstance(context).registerReceiver(
            audioStateDispatchReceiver,
            IntentFilter(AudioClipHelper.BROADCAST_AUDIO_STATE_DISPATCH)
        )
    }
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        LocalBroadcastManager.getInstance(context).unregisterReceiver(audioStateDispatchReceiver)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        alpha = if (enabled) 1F else 0.38F
    }

    /**
     * Set the [MwWordAndDefinitionGroups]s which this view should play pronunciation for when
     * the play button is clicked.
     */
    fun setSource(entries: List<MwWordAndDefinitionGroups>, user: User?) {
        if (user?.merriamWebsterState?.isValid == false) {
            isEnabled = false
            disabledMessageRes = R.string.mw_audio_view_requires_mw_plugin_error
            return
        }
        val sounds = entries.mapNotNull { it.word?.sound }.flatten()
        // Disable audio if the user is not valid or there is no audio
        if (sounds.isEmpty() ) {
            isEnabled = false
            disabledMessageRes = R.string.mw_audio_view_no_audio_available_error
            return
        }

        isEnabled = true
        audioUrls = sounds.asSequence().map { it.wavs }
            .flatten()
            .map { it.removeSuffix(".wav") }
            .filter { it.isNotBlank() }
            .map { fileName ->
                val firstChar = fileName.toCharArray().firstOrNull() ?: "a"
                "$BASE_URL$SEPARATOR$firstChar$SEPARATOR$fileName$DEFAULT_AUDIO_EXTENSION"
            }.toList()
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
        }
    }

    private fun handlePlayStopButtonClicked(state: AudioClipHelper.AudioState) {
        if (!isEnabled) {
            listener?.onAudioError(disabledMessageRes)
            return
        }

        when (state) {
            AudioClipHelper.AudioState.LOADING ,
            AudioClipHelper.AudioState.PREPARED,
            AudioClipHelper.AudioState.PLAYING -> stop()
            AudioClipHelper.AudioState.STOPPED -> play()
        }
    }

    companion object {
        private const val SEPARATOR = "/"
        private const val BASE_URL = "https://media.merriam-webster.com/audio/prons/en/us/mp3"
        private const val DEFAULT_AUDIO_EXTENSION = ".mp3"
    }

}