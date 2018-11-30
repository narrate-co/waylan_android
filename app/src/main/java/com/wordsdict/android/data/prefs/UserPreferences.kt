package com.wordsdict.android.data.prefs

/**
 * Preferences that depend on a valid uid
 *
 * These have to be able to wait until AuthActivity has finished to be
 * get or set
 */
object UserPreferences {
    //onboarding prefs
    const val HAS_SEEN_RECENTS_BANNER = "has_seen_recents_banner"
    const val HAS_SEEN_TRENDING_BANNER = "has_seen_trending_banner"
    const val HAS_SEEN_FAVORITES_BANNER = "has_seen_favorites_banner"

    //orientation prefs
    const val PORTRAIT_TO_LANDSCAPE_ORIENTATION_CHANGE_COUNT = "portrait_to_landscape_rotation_count"
    const val LANDSCAPE_TO_PORTRAIT_ORIENTATION_CHANGE_COUNT = "landscape_to_portrait_rotation_count"
    const val IS_ABLE_TO_SUGGEST_ORIENTATION_UNLOCK = "is_able_to_suggest_orientation_unlock"


    // developer prefs
    const val USE_TEST_SKUS = "use_test_skus"
}

