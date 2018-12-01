package com.wordsdict.android.service

import android.content.Context
import android.content.Intent

object AudioClipController {

    fun play(context: Context, url: String) {
        val intent = Intent(context, AudioClipService::class.java)
        intent.putExtra(AudioClipService.INTENT_KEY_COMMAND, "PLAY")
        intent.putExtra(AudioClipService.INTENT_KEY_URL, url)
        context.startService(intent)
    }

    fun stop(context: Context) {
        val intent = Intent(context, AudioClipService::class.java)
        intent.putExtra(AudioClipService.INTENT_KEY_COMMAND, "STOP")
        context.startService(intent)
    }
}

