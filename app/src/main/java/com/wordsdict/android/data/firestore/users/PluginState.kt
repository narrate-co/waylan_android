package com.wordsdict.android.data.firestore.users

import com.wordsdict.android.util.daysElapsed
import java.util.*

sealed class PluginState(val started: Date, val duration: Long) {
    class None : PluginState(Date(), -1L)
    class FreeTrial(isAnonymous: Boolean, started: Date = Date()): PluginState(started, if (isAnonymous) 7L else 30L)
    class Purchased(started: Date = Date()) : PluginState(started, 365L)

    val isValid: Boolean = started.daysElapsed < duration

    val remainingDays: Long = Math.max(0, duration - started.daysElapsed)
}