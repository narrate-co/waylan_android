package com.words.android.service

import android.app.IntentService
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.words.android.BuildConfig
import com.words.android.Config
import java.io.IOException
import java.util.*
import kotlin.concurrent.schedule


class AudioClipService: Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {




    companion object {
        const val INTENT_KEY_COMMAND = "KEY_COMMAND"
        const val INTENT_KEY_URL = "KEY_URL"

        const val BROADCAST_AUDIO_STATE_DISPATCH = "audio_state_dispatch_broadcast"
        const val BROADCAST_AUDIO_STATE_EXTRA_STATE = "audio_state_extra_state"
        const val BROADCAST_AUDIO_STATE_EXTRA_URL = "audio_state_extra_url"
        const val BROADCAST_AUDIO_STATE_EXTRA_MESSAGE = "audio_state_extra_message"

        // Milliseconds to wait for audio to load
        const val MEDIA_PREPARE_TIMEOUT = 8000L // 8 seconds

        const val TAG = "AudioClipService"
    }

    enum class Command {
        PLAY, STOP, NONE
    }

    enum class AudioStateDispatch {
        LOADING, STOPPED, ERROR, PREPARED, PLAYING
    }


    private var mediaPlayer: MediaPlayer? = null
    private val audioManager: AudioManager by lazy { getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    private var currentUrl: String? = null

    private var isPreparing = false
    private var playWhenLoaded = true

    private var networkIsAvailable = true //TODO use a network monitor

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val command = Command.valueOf(intent?.getStringExtra(INTENT_KEY_COMMAND) ?: Command.NONE.name)
        when (command) {
            Command.PLAY -> play(intent?.getStringExtra(INTENT_KEY_URL))
            Command.STOP, Command.NONE -> {
                stop()
                destroy()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun play(url: String?) {
        playWhenLoaded = true

        //if there's something playing, stop everything, continue
        if (currentUrl != null) stop()

        //if the given url is null, dispatch an error, destroy
        if (url == null || url.isEmpty()) {
            stop()
            dispatchError(url, "No available pronunciation")
            destroy()
            return
        }

        timer?.cancel()

        //handle no network by stopping and dispatching an error, destory
        if (!networkIsAvailable) {
            stop()
            dispatchError(url, "No network available")
            destroy()
            return
        }

        mediaPlayer?.release()
        mediaPlayer = null
        audioManager.abandonAudioFocus(this)
        mediaPlayer = MediaPlayer()
        currentUrl = url
        mediaPlayer?.setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
        try {
            dispatchLoading(url)
            println("$TAG::setting up url: $url")
            mediaPlayer?.setDataSource(url)
            mediaPlayer?.setOnPreparedListener(this)
            mediaPlayer?.setOnCompletionListener(this)
            mediaPlayer?.setOnErrorListener(this)
            mediaPlayer?.prepareAsync()

            // let the media player time out if debugging to catch errors
            if (!BuildConfig.DEBUG || !Config.DEBUG_VERBOSE) {
                timer = Timer("media_prepare_timer", false).schedule(MEDIA_PREPARE_TIMEOUT) {
                    dispatchError(currentUrl, "Unable to play audio")
                    stop()
                    destroy()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            dispatchError(url, "No pronunciation available")
            stop()
            destroy()
        }

    }

    var timer: TimerTask? = null

    private fun stop() {
        timer?.cancel()
        audioManager.abandonAudioFocus(this)
        mediaPlayer?.release()
        mediaPlayer = null
        dispatchStopped(currentUrl)
        currentUrl = null
    }

    private fun destroy() {
        stopSelf()
    }

    override fun onPrepared(player: MediaPlayer?) {
        timer?.cancel()
        val result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            dispatchPrepared(currentUrl)
            if (playWhenLoaded) {
                dispatchPlaying(currentUrl)
                mediaPlayer?.start()
            }
        } else {
            dispatchError(currentUrl, "Unable to play pronunciation")
            stop()
            destroy()
        }
    }

    override fun onCompletion(p0: MediaPlayer?) {
        stop()
        destroy()
    }

    override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        dispatchError(currentUrl, "An error occurred playing pronunciation")
        stop()
        destroy()
        return true
    }


    override fun onAudioFocusChange(p0: Int) { }


    private fun dispatch(dispatch: AudioStateDispatch, url: String?, message: String? = null) {
        val intent = Intent(BROADCAST_AUDIO_STATE_DISPATCH)
        intent.putExtra(BROADCAST_AUDIO_STATE_EXTRA_STATE, dispatch)
        intent.putExtra(BROADCAST_AUDIO_STATE_EXTRA_URL, url ?: "")
        intent.putExtra(BROADCAST_AUDIO_STATE_EXTRA_MESSAGE, message ?: "")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun dispatchLoading(url: String?) {
        isPreparing = true
        dispatch(AudioStateDispatch.LOADING, url)
    }

    private fun dispatchStopped(url: String?) {
        isPreparing = false
        dispatch(AudioStateDispatch.STOPPED, url)
    }

    private fun dispatchError(url: String?, message: String) {
        dispatch(AudioStateDispatch.ERROR, url, message)
    }

    private fun dispatchPrepared(url: String?) {
        isPreparing = false
        dispatch(AudioStateDispatch.PREPARED, url)
    }

    private fun dispatchPlaying(url: String?) {
        isPreparing = false
        dispatch(AudioStateDispatch.PLAYING, url)
    }

}

