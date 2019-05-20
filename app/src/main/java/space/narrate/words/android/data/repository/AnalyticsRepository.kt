package space.narrate.words.android.data.repository

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

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
        private val firebaseAnalytics: FirebaseAnalytics
) {

    // All custom events
    companion object {
        private const val EVENT_SEARCH_WORD = "search_word"
        private const val EVENT_NAVIGATE_BACK = "navigate_back"
    }

    // Optionally set the current user's Id for additional info
    fun setUserId(uid: String?) {
        firebaseAnalytics.setUserId(uid)
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

    private fun logNavigateBackEvent(from: String, type: String) {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.VALUE, from)
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, type)
        }
        firebaseAnalytics.logEvent(EVENT_NAVIGATE_BACK, params)
    }

}