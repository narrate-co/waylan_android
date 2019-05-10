package space.narrate.words.android.data.mw

import space.narrate.words.android.data.disk.mw.MwWordAndDefinitionGroups
import space.narrate.words.android.data.firestore.users.User

/**
 * A convenience class to include the [User] in addition to [MwWordAndDefinitionGroups]s. This is
 * helpful since Merriam-Webster entry should only be available to users with the proper
 * Merriam-Webster [PluginState].
 *
 * This makes it easy to observe [PermissiveWordsDefinitions] and react to either changes in
 * [MwWordAndDefinitionGroups] and [User] Merriam-Webster [PluginState].
 */
data class PermissiveWordsDefinitions(
        var user: User?,
        var entries: List<MwWordAndDefinitionGroups>
)

