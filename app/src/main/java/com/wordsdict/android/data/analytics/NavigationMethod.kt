package com.wordsdict.android.data.analytics


/**
 * Ways which a user can navigate back in Words
 *
 * This is tracked via [AnalyticsRepository.EVENT_NAVIGATE_BACK]
 */
enum class NavigationMethod {
    NAV_ICON, BACK_BUTTON, DRAG_DISMISS
}