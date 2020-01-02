package space.narrate.waylan.core.billing

import space.narrate.waylan.core.data.firestore.users.AddOn

/**
 * An event which can be listened to by UI screens to show user feedback from events
 * which happen in [BillingManager].
 */
sealed class BillingEvent(val addOn: AddOn) {
    class Purchased(addOn: AddOn) : BillingEvent(addOn)
    class Canceled(addOn: AddOn) : BillingEvent(addOn)
}