package space.narrate.waylan.core.data.firestore.users

import java.util.*

/**
 * An [AddOn] associated with a [User]. This object controls all aspects of any [AddOn]. The fields
 * here determine the [AddOnState] of this AddOn.
 */
data class UserAddOn(
    var id: String = "",
    var validDurationDays: Long = 7L,
    var hasStartedFreeTrial: Boolean = false,
    var started: Date = Date(),
    var purchaseToken: String = "",
    var isAwareOfExpiration: Boolean = false
)