package com.words.android.data.firestore.users

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.Exclude
import com.words.android.Config

data class User(
        var uid: String = "",
        var isAnonymous: Boolean = true,
        var name: String = "",
        var email: String = "",
        var merriamWebsterState: PluginState = PluginState.FREE_TRIAL
)

