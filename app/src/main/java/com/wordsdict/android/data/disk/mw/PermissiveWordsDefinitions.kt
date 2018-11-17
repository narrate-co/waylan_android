package com.wordsdict.android.data.disk.mw

import com.wordsdict.android.data.firestore.users.User


data class PermissiveWordsDefinitions(
        val user: User?,
        val entries: List<WordAndDefinitions>
)

