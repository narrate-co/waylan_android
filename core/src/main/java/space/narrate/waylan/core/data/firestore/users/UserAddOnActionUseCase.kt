package space.narrate.waylan.core.data.firestore.users

import java.util.*

sealed class UserAddOnActionUseCase {

    object TryForFree : UserAddOnActionUseCase() {
        override fun perform(user: User, addOn: UserAddOn): UserAddOn = addOn.apply {
            started = Date()
            hasStartedFreeTrial = true
            validDurationDays = if (user.isAnonymous) {
                AddOnConfig.TRIAL_DURATION_ANON_DAYS
            } else {
                AddOnConfig.TRIAL_DURATION_DAYS
            }
            isAwareOfExpiration = false
        }
    }

    // Adding and Renewing are exactly the same flow.
    data class Add(
        val newPurchaseToken: String
    ) : UserAddOnActionUseCase() {
        override fun perform(user: User, addOn: UserAddOn): UserAddOn = addOn.apply {
            started = Date()
            hasStartedFreeTrial = true
            validDurationDays = AddOnConfig.PURCHASE_DURATION_DAYS
            purchaseToken = newPurchaseToken
            isAwareOfExpiration = false
        }
    }

    class Manual(
        val with: UserAddOn.() -> Unit
    ) : UserAddOnActionUseCase() {
        override fun perform(user: User, addOn: UserAddOn): UserAddOn = addOn.apply {
            with()
        }
    }

    abstract fun perform(user: User, addOn: UserAddOn): UserAddOn
}