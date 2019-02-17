package space.narrate.words.android.data.analytics


/**
 * Methods which a user is able to navigate back in Words
 *
 * User navigation behavior is tracked via [AnalyticsRepository.EVENT_NAVIGATE_BACK]
 */
enum class NavigationMethod {
    NAV_ICON, BACK_BUTTON, DRAG_DISMISS
}