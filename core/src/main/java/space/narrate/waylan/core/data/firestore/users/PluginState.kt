package space.narrate.waylan.core.data.firestore.users

import space.narrate.waylan.core.R
import space.narrate.waylan.core.data.firestore.users.PluginState.Action.*
import space.narrate.waylan.core.util.daysElapsed
import java.util.*
import kotlin.math.max

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
sealed class PluginState(
    val started: Date,
    val duration: Long,
    val purchaseToken: String,
    val actions: List<Action>
) {

    /**
     * Actions available which are able to move a plugin state to a different plugin state.
     *
     * For example, [TRY_FOR_FREE] should move a the [None] state for a given add-on into the
     * [FreeTrial] state.
     */
    enum class Action(val title: Int) {
        TRY_FOR_FREE(R.string.plugin_action_try_for_free_title),
        ADD(R.string.plugin_action_add_title)
    }

    /**
     * An invalid state which should otherwise never be reached
     */
    class None : PluginState(Date(), -1L, "", listOf(TRY_FOR_FREE, ADD))

    /**
     * A plugin which has never been purchased but should be available for an initial period after
     * [started]
     */
    class FreeTrial(
        isAnonymous: Boolean,
        started: Date = Date()
    ): PluginState(started, if (isAnonymous) 7L else 30L, "", listOf(ADD))

    /**
     * A plugin which has been purchased and has an associated Google Play Billing [purchaseToken]
     */
    class Purchased(
        started: Date = Date(),
        purchaseToken: String
    ) : PluginState(started, 365L, purchaseToken, emptyList())

    val isValid: Boolean = started.daysElapsed < duration

    val remainingDays: Long = max(0, duration - started.daysElapsed)
}