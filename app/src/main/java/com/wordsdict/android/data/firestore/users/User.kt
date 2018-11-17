package com.wordsdict.android.data.firestore.users

import java.util.*

data class User(
        var uid: String = "",
        var isAnonymous: Boolean = true,
        var name: String = "",
        var email: String = "",
        var isMerriamWebsterSubscriber: Boolean = false,
        var merriamWebsterStarted: Date = Date()
)



