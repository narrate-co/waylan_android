package com.words.android.data.firestore.users

import com.google.firebase.firestore.Exclude
import com.words.android.util.daysElapsed
import java.util.*

data class User(
        var uid: String = "",
        var isAnonymous: Boolean = true,
        var name: String = "",
        var email: String = "",
        var merriamWebsterState: PluginState = PluginState.FREE_TRIAL,
        var merriamWebsterFreeTrialCreated: Date = Date()
)



