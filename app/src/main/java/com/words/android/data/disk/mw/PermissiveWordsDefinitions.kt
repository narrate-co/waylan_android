package com.words.android.data.disk.mw

import com.words.android.data.firestore.users.PluginState
import com.words.android.data.firestore.users.User


data class PermissiveWordsDefinitions(
        val user: User?,
        val entries: List<WordAndDefinitions>
)

