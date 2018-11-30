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
    const val ORIENTATION_LOCK = "orientation_lock_pref"

}

