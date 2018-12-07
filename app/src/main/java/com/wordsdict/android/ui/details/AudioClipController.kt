package com.wordsdict.android.ui.details

import android.content.Context
import android.content.Intent

/**
 * A static object to "send and forget" url audio clips to be streamed by [AudioClipService]
 *
 */
object AudioClipController {

    /**
     * Play (stream) a [url] audio clip. If a clip is already playing, it will be stopped and this
     * clip will be started
     *
     * @param url the full url address of the clip to be streamed
     */
    fun play(context: Context, url: String) {
        val intent = Intent(context, AudioClipService::class.java)
        intent.putExtra(AudioClipService.INTENT_KEY_COMMAND, "PLAY")
        intent.putExtra(AudioClipService.INTENT_KEY_URL, url)
        context.startService(intent)
    }

    /**
     * Stop any currently playing (streaming) audio clips.
     */
    fun stop(context: Context) {
        val intent = Intent(context, AudioClipService::class.java)
        intent.putExtra(AudioClipService.INTENT_KEY_COMMAND, "STOP")
        context.startService(intent)
    }
}

