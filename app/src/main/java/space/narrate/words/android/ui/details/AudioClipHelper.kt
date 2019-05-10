package space.narrate.words.android.ui.details

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import space.narrate.words.android.BuildConfig
import space.narrate.words.android.R
import java.io.IOException
import java.util.*
import kotlin.concurrent.schedule

class AudioClipHelper(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) : MediaPlayer.OnPreparedListener,
    MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener,
    AudioManager.OnAudioFocusChangeListener, LifecycleObserver {

    companion object {
        // Key constants for audio state broadcasting
        // The name of the broadcast intent
        const val BROADCAST_AUDIO_STATE_DISPATCH = "audio_state_dispatch_broadcast"
        // The current [AudioStateBroadcast]
        const val BROADCAST_AUDIO_STATE_EXTRA_STATE = "audio_state_extra_state"
        // An optional url of the currently playing audio clip. This is a nullable extra.
        // This can be used to match a UI element with the clip playing here to ensure the correct
        // UI element is being updated.
        const val BROADCAST_AUDIO_STATE_EXTRA_URL = "audio_state_extra_url"
        // A optional message to report state details. This is a nullable extra. This is used to
        // dispatch internal errors to be shown by UI-side receivers.
        const val BROADCAST_AUDIO_STATE_EXTRA_MESSAGE = "audio_state_extra_message"


        // Milliseconds to wait for audio to load before throwing a timeout error
        const val MEDIA_PREPARE_TIMEOUT = 8000L // 8 seconds

        const val TAG = "AudioClipHelper"
    }

    /**
     * An enumeration of possible media states in which this Service can be. These are all the
     * possible values for [BROADCAST_AUDIO_STATE_EXTRA_STATE]. UI clients can receive AudioState
     * to properly reflect this service's state in the UI.
     */
    enum class AudioState {
        /** When a clip is being prepared and will be played once loaded */
        LOADING,
        /** When a clip is set but is not loading or planning to be prepared */
        STOPPED,
        /** When something has gone wrong with preparing and playing a clip */
        ERROR,
        /** When a clip has been prepared, right before it is played */
        PREPARED,
        /** When a clip is currently being played */
        PLAYING
    }


    private var mediaPlayer: MediaPlayer? = null
    private val audioManager: AudioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    private var currentUrl: String? = null

    private var isPreparing = false
    private var playWhenLoaded = true

    //TODO use a network monitor
    // A placeholder for a future implementation of a network monitor
    private var networkIsAvailable = true

    // A task to run after a media clip has started to be prepared and after
    // [MEDIA_PREPARE_TIMEOUT] has elapsed. The MediaPlayer's default timeout is too long and this
    // task keeps the user from waiting over [MEDIA_PREPARE_TIMEOUT] for a clip to load.
    var prepareTimeoutTimer: TimerTask? = null


    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    fun play(url: String?) {
        start(url)
    }

    fun stop() {
        end()
        destroy()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        stop()
    }

    private fun start(url: String?) {
        if (!lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            end()
            dispatchError(url, R.string.audio_clip_unable_to_play_generic)
            destroy()
            return
        }

        playWhenLoaded = true

        //if there's something playing, end everything and then continue
        if (currentUrl != null) end()

        //if the given url is null, dispatch an error and destroy
        if (url == null || url.isEmpty()) {
            end()
            dispatchError(url, R.string.audio_clip_no_pronunciations_available)
            destroy()
            return
        }

        prepareTimeoutTimer?.cancel()

        //handle no network by stopping and dispatching an error and destory
        if (!networkIsAvailable) {
            end()
            dispatchError(url, R.string.audio_clip_no_network_available)
            destroy()
            return
        }

        mediaPlayer?.release()
        mediaPlayer = null
        audioManager.abandonAudioFocus(this)
        mediaPlayer = MediaPlayer()
        currentUrl = url
        mediaPlayer?.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )

        try {
            dispatchLoading(url)
            mediaPlayer?.setDataSource(url)
            mediaPlayer?.setOnPreparedListener(this)
            mediaPlayer?.setOnCompletionListener(this)
            mediaPlayer?.setOnErrorListener(this)
            mediaPlayer?.prepareAsync()

            // let the media player timeout if debugging to catch errors
            if (!BuildConfig.DEBUG) {
                // Set our own custom timeout time and task
                prepareTimeoutTimer = Timer("media_prepare_timer", false)
                    .schedule(MEDIA_PREPARE_TIMEOUT) {
                        dispatchError(currentUrl, R.string.audio_clip_unable_to_start)
                        end()
                        destroy()
                    }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            dispatchError(url, R.string.audio_clip_no_pronunciations_available)
            end()
            destroy()
        }

    }

    private fun end() {
        prepareTimeoutTimer?.cancel()
        audioManager.abandonAudioFocus(this)
        mediaPlayer?.release()
        mediaPlayer = null
        dispatchStopped(currentUrl)
        currentUrl = null
    }

    private fun destroy() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onPrepared(player: MediaPlayer?) {
        prepareTimeoutTimer?.cancel()
        val result = audioManager.requestAudioFocus(
            this,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
        )
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            dispatchPrepared(currentUrl)
            if (playWhenLoaded) {
                dispatchPlaying(currentUrl)
                mediaPlayer?.start()
            }
        } else {
            dispatchError(currentUrl, R.string.audio_clip_unable_to_start)
            end()
            destroy()
        }
    }

    override fun onCompletion(p0: MediaPlayer?) {
        end()
        destroy()
    }

    override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        dispatchError(currentUrl, R.string.audio_clip_pay_error)
        end()
        destroy()
        return true
    }

    override fun onAudioFocusChange(p0: Int) {
    }

    /** Helper methods for dispatching specific AudioStates */

    private fun dispatchLoading(url: String?) {
        isPreparing = true
        dispatch(AudioState.LOADING, url)
    }

    private fun dispatchStopped(url: String?) {
        isPreparing = false
        dispatch(AudioState.STOPPED, url)
    }

    private fun dispatchError(url: String?, messageRes: Int) {
        dispatch(AudioState.ERROR, url, messageRes)
    }

    private fun dispatchPrepared(url: String?) {
        isPreparing = false
        dispatch(AudioState.PREPARED, url)
    }

    private fun dispatchPlaying(url: String?) {
        isPreparing = false
        dispatch(AudioState.PLAYING, url)
    }

    // Send AudioState events to be received by anyone who needs to update UI to
    // reflect this services state.
    private fun dispatch(dispatch: AudioState, url: String?, messageRes: Int? = null) {
        val intent = Intent(BROADCAST_AUDIO_STATE_DISPATCH)
        intent.putExtra(BROADCAST_AUDIO_STATE_EXTRA_STATE, dispatch)
        if (url != null) {
            intent.putExtra(BROADCAST_AUDIO_STATE_EXTRA_URL, url)
        }
        if (messageRes != null) {
            intent.putExtra(BROADCAST_AUDIO_STATE_EXTRA_MESSAGE, messageRes)
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}


