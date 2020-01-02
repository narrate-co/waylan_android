package space.narrate.waylan.core.ui.common

import space.narrate.waylan.core.data.firestore.users.AddOn

interface AddOnListener {
    fun onAddOnDetailsClicked(addOn: AddOn)
    fun onAddOnDismissClicked(addOn: AddOn)
}