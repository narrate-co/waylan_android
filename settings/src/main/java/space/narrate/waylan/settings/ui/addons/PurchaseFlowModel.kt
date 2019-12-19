package space.narrate.waylan.settings.ui.addons

import space.narrate.waylan.core.data.firestore.users.AddOn
import space.narrate.waylan.core.data.firestore.users.AddOnAction

/**
 * An object used to post events to [BillingManager].
 */
data class PurchaseFlowModel(
    val addOn: AddOn,
    val addOnAction: AddOnAction
)