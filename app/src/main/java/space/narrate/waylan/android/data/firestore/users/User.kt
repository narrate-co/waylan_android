package space.narrate.waylan.android.data.firestore.users

import java.util.*

/**
 * A Firestore document representation of [FirebaseUser] for easier access and customization.
 *
 * @property uid The uid of this user. This is also the document id and the uid of this user's
 *  corresponding [FirebaseUser.uid].
 * @property isAnonymous Whether the user has signed up or not.
 * @property merriamWebsterStarted The date which the user started either
 *  their [PluginState.FreeTrial] or [PluginState.Purchased].
 * @property merriamWebsterPurchaseToken The Google Play Billing purchase token for the
 *  Merriam-Webster plugin. If this is blank, the user's Merriam-Webster plugin state is
 *  either [PluginState.NONE] or [PluginState.PURCHASED].
 */
data class User(
        var uid: String = "",
        var isAnonymous: Boolean = true,
        var name: String = "",
        var email: String = "",
        var merriamWebsterStarted: Date = Date(),
        var merriamWebsterPurchaseToken: String = ""
)



