package space.narrate.waylan.android.data.prefs

/**
 * Preferences which should be specific to a valid, logged in user.
 */
object UserPreferences {
    // Onboarding prefs
    const val HAS_SEEN_RECENTS_BANNER = "has_seen_recents_banner"
    const val HAS_SEEN_TRENDING_BANNER = "has_seen_trending_banner"
    const val HAS_SEEN_FAVORITES_BANNER = "has_seen_favorites_banner"
    const val HAS_SEEN_DRAG_DISMISS_OVERLAY = "has_seen_drag_dismiss_overlay"
    const val HAS_SEEN_MERRIAM_WEBSTER_PERMISSION_PANE = "has_seen_merriam_webster_permission_pane"

    // Filter prefs
    const val RECENTS_LIST_FILTER = "recents_list_filter"
    const val TRENDING_LIST_FILTER = "trending_list_filter"
    const val FAVORITES_LIST_FILTER = "favorites_list_filter"

    // Orientation prefs
    const val PORTRAIT_TO_LANDSCAPE_ORIENTATION_CHANGE_COUNT = "portrait_to_landscape_rotation_count"
    const val LANDSCAPE_TO_PORTRAIT_ORIENTATION_CHANGE_COUNT = "landscape_to_portrait_rotation_count"
    const val IS_ABLE_TO_SUGGEST_ORIENTATION_UNLOCK = "is_able_to_suggest_orientation_unlock"

    // Developer prefs
    const val USE_TEST_SKUS = "use_test_skus"
}

