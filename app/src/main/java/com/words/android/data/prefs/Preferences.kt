package com.words.android.data.prefs

/**
 * Preferences that should be set outside of the user scope.
 * Dark mode needs to be initialized faster than we can instantiate
 * UserPreferenceRepository with a valid uid.
 *
 * These are globally set for all users
 */
object Preferences {
    const val NIGHT_MODE = "uses_night_mode"
}
