package com.wordsdict.android.data.prefs

import android.content.pm.ActivityInfo
import com.wordsdict.android.R

/**
 * Preferences that should be set outside of the user scope.
 * Dark mode needs to be initialized faster than we can instantiate
 * UserPreferenceRepository with a valid uid.
 *
 * These are globally set for all users
 */
object Preferences {
    const val NIGHT_MODE = "uses_night_mode"
    const val ORIENTATION_LOCK = "orientation_lock"

    // debug prefs
    const val USE_TEST_SKUS = "use_test_skus"
}

enum class Orientation(val value: Int, val title: Int, val desc: Int) {
    UNSPECIFIED(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, R.string.settings_orientation_unspecified_title, R.string.settings_orientation_unspecified_desc),
    PORTRAIT(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, R.string.settings_orientation_portrait_title, R.string.settings_orientation_portrait_desc),
    LANDSCAPE(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, R.string.settings_orientation_landscape_title, R.string.settings_orientation_landscape_desc)
}