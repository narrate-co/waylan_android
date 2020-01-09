package space.narrate.waylan.core.data.firestore.users

import java.util.*

/**
 * A Firestore document representation of [FirebaseUser] for easier access and customization.
 *
 * @property uid The uid of this user. This is also the document id and the uid of this user's
 *  corresponding [FirebaseUser.uid].
 * @property isAnonymous Whether the user has signed up or not.
 */
data class User(
    var uid: String = "",
    var isAnonymous: Boolean = true,
    var name: String = "",
    var email: String = "",

    // TODO: Remove once all users are migrated.
    @Deprecated("User level add-on fields are deprecated. Use UserAddOns instead")
    var merriamWebsterStarted: Date = Date(), // deprecated
    @Deprecated("User level add-on fields are deprecated. Use UserAddOns instead")
    var merriamWebsterPurchaseToken: String = ""
)



