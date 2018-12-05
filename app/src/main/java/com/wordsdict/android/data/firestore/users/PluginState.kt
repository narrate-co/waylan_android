package com.wordsdict.android.data.firestore.users

import com.wordsdict.android.util.daysElapsed
import java.util.*

/**
 * A sealed class to hold the possible state of a User's "Plugins" group.
 *
 * Words has the concept of "plugins" - small modules of functionality that can be purchased
 * for a one-time fee and expire after a given period (currently 365 days). This sealed class uses
 * fields from [User] to construct one of the [PluginState]s. This makes working with ideas such
 * as validity, permission, expiration date, remaining validity, etc simpler for the client while
 * keeping the [User] Firestore object lightweight and maintainable with the fewest number
 * of plugin related fields (reducing the chance of improperly updating/syncing changes).
 */
sealed class PluginState(val started: Date, val duration: Long, val purchaseToken: String) {
    /**
     * An invalid state which should otherwise never be reached
     */
    class None : PluginState(Date(), -1L, "")

    /**
     * A plugin which has never been purchased but should be available for an initial period after
     * [started]
     */
    class FreeTrial(isAnonymous: Boolean, started: Date = Date()): PluginState(started, if (isAnonymous) 7L else 30L, "")

    /**
     * A plugin which has been purchased and has an associated Google Play Billing [purchaseToken]
     */
    class Purchased(started: Date = Date(), purchaseToken: String) : PluginState(started, 365L, purchaseToken)



    val isValid: Boolean = started.daysElapsed < duration

    val remainingDays: Long = Math.max(0, duration - started.daysElapsed)
}