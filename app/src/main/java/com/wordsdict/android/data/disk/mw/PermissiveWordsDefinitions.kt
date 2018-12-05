package com.wordsdict.android.data.disk.mw

import com.wordsdict.android.data.firestore.users.User

/**
 * A convenience class to include the [User] in addition to [WordAndDefinitions]s. This is
 * helpful since Merriam-Webster entries should only be available to users with the proper
 * Merriam-Webster [PluginState].
 *
 * This makes it easy to observe [PermissiveWordsDefinitions] and react to either changes in
 * [WordAndDefinitions] and [User] Merriam-Webster [PluginState].
 */
data class PermissiveWordsDefinitions(
        var user: User?,
        var entries: List<WordAndDefinitions>
)

