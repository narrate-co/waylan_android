package com.words.android.data.firestore.users

import com.words.android.util.daysElapsed


val User.remainingTrialDays: Long
    get() {
        val totalTrialLength = if (isAnonymous) 7L else 30L
        val daysElapsed = merriamWebsterFreeTrialCreated.daysElapsed
        return Math.max(0, totalTrialLength - daysElapsed)
    }