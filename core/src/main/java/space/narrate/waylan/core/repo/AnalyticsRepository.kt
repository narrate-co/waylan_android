package space.narrate.waylan.core.repo

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import space.narrate.waylan.core.billing.BillingEvent
import space.narrate.waylan.core.data.firestore.AuthenticationStore
import space.narrate.waylan.core.data.firestore.users.AddOn
import space.narrate.waylan.core.data.firestore.users.AddOnAction
import space.narrate.waylan.core.data.firestore.users.UserAddOnActionUseCase
import space.narrate.waylan.core.repo.AnalyticsRepository.Companion.EVENT_NAVIGATE_BACK
import space.narrate.waylan.core.repo.AnalyticsRepository.Companion.EVENT_SEARCH_WORD

/**
 * An abstraction of FirebaseAnalytics to help log custom events
 *
 * @property EVENT_NAVIGATE_BACK An event to track how the user navigates back within the app.
 *  Words has 3 options ([NavigationMethod]) for back navigation - the navigation bar back
 *  button, the toolbar navigation icon and drag to dismiss. This event hopes to track the
 *  discoverability/usability of drag to dismiss
 * @property EVENT_SEARCH_WORD An event to track when a user searches a word. Things logged are
 *  the textRes they input, the word they clicked on and where it was populated from
 *  ([WordSource]). This hopes to discover how accurate/valuable SymSpell and [SuggestSource]
 *  is and how suggestions might be improved.
 */
class AnalyticsRepository(
        private val firebaseAnalytics: FirebaseAnalytics,
        authenticationStore: AuthenticationStore
) {

    // All custom events
    companion object {
        private const val EVENT_SEARCH_WORD = "search_word"
        private const val EVENT_NAVIGATE_BACK = "navigate_back"
        private const val EVENT_SIGN_UP = "sign_up"

        private const val ADD_ON_EVENT_FREE_TRIAL = "free_trial"
        private const val ADD_ON_EVENT_ADD = "add"
        private const val ADD_ON_EVENT_RENEW = "renew"
        private const val ADD_ON_EVENT_INTERRUPTED_CANCELED = "interrupted-cancelled"
    }

    init {
        authenticationStore.uid.observeForever {
            firebaseAnalytics.setUserId(it)
        }
    }

    fun logSearchWordEvent(input: String, selection: String, source: String) {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.VALUE, input)
            putString(FirebaseAnalytics.Param.SEARCH_TERM, selection)
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, source)
        }
        firebaseAnalytics.logEvent(EVENT_SEARCH_WORD, params)
    }

    fun logNavigationIconEvent(from: String) {
        logNavigateBackEvent(from, "NAV_ICON")
    }

    fun logDragDismissEvent(from: String) {
        logNavigateBackEvent(from, "DRAG_DISMISS")
    }

    fun logSignUpEvent() {
        firebaseAnalytics.logEvent(EVENT_SIGN_UP, Bundle())
    }

    fun logAddOnBillingEvent(event: BillingEvent) {
        logCommercePurchase(
            event.addOn.name,
            when (event) {
                is BillingEvent.Purchased -> ADD_ON_EVENT_ADD
                is BillingEvent.Canceled -> ADD_ON_EVENT_INTERRUPTED_CANCELED
            }
        )
    }

    fun logAddOnEvent(addOn: AddOn, action: AddOnAction) {
        logCommercePurchase(
            addOn.name,
            when (action) {
                AddOnAction.TRY_FOR_FREE -> ADD_ON_EVENT_FREE_TRIAL
                AddOnAction.ADD -> ADD_ON_EVENT_ADD
                AddOnAction.RENEW -> ADD_ON_EVENT_RENEW
            }
        )
    }

    private fun logCommercePurchase(type: String, option: String) {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, type)
            putString(FirebaseAnalytics.Param.CHECKOUT_OPTION, option)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.ECOMMERCE_PURCHASE, params)
    }

    private fun logNavigateBackEvent(from: String, type: String) {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.VALUE, from)
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, type)
        }
        firebaseAnalytics.logEvent(EVENT_NAVIGATE_BACK, params)
    }

}