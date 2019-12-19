package space.narrate.waylan.core.data.firestore.users

import android.content.Context
import space.narrate.waylan.core.R
import space.narrate.waylan.core.data.firestore.users.AddOnState.FREE_TRIAL_EXPIRED
import space.narrate.waylan.core.data.firestore.users.AddOnState.FREE_TRIAL_VALID
import space.narrate.waylan.core.data.firestore.users.AddOnState.NONE
import space.narrate.waylan.core.data.firestore.users.AddOnState.PURCHASED_EXPIRED
import space.narrate.waylan.core.data.firestore.users.AddOnState.PURCHASED_VALID
import space.narrate.waylan.core.util.daysElapsed
import kotlin.math.max

val UserAddOn.state: AddOnState
    get() {
        return when {
            purchaseToken.isNotBlank() && isValid -> PURCHASED_VALID
            purchaseToken.isNotBlank() && !isValid -> PURCHASED_EXPIRED
            hasStartedFreeTrial && isValid -> FREE_TRIAL_VALID
            hasStartedFreeTrial && !isValid -> FREE_TRIAL_EXPIRED
            else -> NONE
        }
    }

fun UserAddOn.statusTextLabel(context: Context): String {
    return when(state)  {
        PURCHASED_VALID ->
            // TODO: Add remaining days to expiration?
            context.getString(R.string.plugin_status_purchased_valid)
        PURCHASED_EXPIRED ->
            context.getString(R.string.plugin_status_purchased_expired)
        FREE_TRIAL_VALID ->
            context.getString(R.string.plugin_status_free_trial_valid_days_remaining, remainingDays)
        FREE_TRIAL_EXPIRED ->
            context.getString(R.string.plugin_status_free_trial_expired)
        else -> ""
    }
}

val UserAddOn.isValid: Boolean
    get() = started.daysElapsed < validDurationDays

val UserAddOn.remainingDays: Long
    get() = max(0L, validDurationDays - started.daysElapsed)
