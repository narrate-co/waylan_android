package space.narrate.waylan.core.data.firestore.users

import space.narrate.waylan.core.R

/**
 * Actions which move [AddOn]s between [AddOnState]s.
 *
 * These actions translate into manipulations on a [UserAddOn] object.
 */
enum class AddOnAction(val title: Int) {
    TRY_FOR_FREE(R.string.add_on_action_try_for_free_title),
    ADD(R.string.add_on_action_add_title),
    RENEW(R.string.add_on_action_renew_title)
}