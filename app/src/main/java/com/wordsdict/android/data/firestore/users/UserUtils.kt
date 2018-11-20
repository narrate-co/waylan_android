package com.wordsdict.android.data.firestore.users

import java.util.*

private val User.totalStateDuration: Long
    get() = when {
        isMerriamWebsterSubscriber -> 365L
        isAnonymous -> 7L
        else -> 30L
    }

val User.merriamWebsterState: PluginState
    get() {
        return when {
            isMerriamWebsterSubscriber -> PluginState.Purchased(merriamWebsterStarted, merriamWebsterPurchaseToken)
            else -> PluginState.FreeTrial(isAnonymous, merriamWebsterStarted)
        }
    }

val User.oneDayPastExpiration: Date
    get() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -(totalStateDuration + 1).toInt())
        return cal.time
    }