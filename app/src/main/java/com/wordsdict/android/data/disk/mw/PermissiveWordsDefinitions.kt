package com.wordsdict.android.data.disk.mw

import com.wordsdict.android.data.firestore.users.User


data class PermissiveWordsDefinitions(
        var user: User?,
        var entries: List<WordAndDefinitions>
)

