package space.narrate.waylan.core.ui.common

import space.narrate.waylan.core.data.firestore.users.AddOn

/**
 * A listener which can be attached to an Add-On feature to support common Add-On related
 * user tasks.
 */
interface AddOnListener {
    fun onAddOnDetailsClicked(addOn: AddOn)
    fun onAddOnDismissClicked(addOn: AddOn)
}