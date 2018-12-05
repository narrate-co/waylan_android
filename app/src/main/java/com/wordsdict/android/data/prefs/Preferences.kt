package com.wordsdict.android.data.prefs

/**
 * Preferences that should be set outside of [UserScope].
 *
 * For example, Dark mode needs to be available earlier than we can instantiate
 * UserPreferenceStore with a valid uid. When the app is launched, we need to immediately set
 * [AppCompatDelegates.setDefaultNightMde] to avoid flashing day or night mode changes.
 *
 * While these Preferences are read outside of [UserScope], they should not be written to
 * directly. For consistency, prefer using [UserPreferenceStore], which surfaces
 * limited [PreferenceStore] functionality, to write to these values. It should not be necessary
 * to write to [Preferences] outside of [UserScope].
 */
object Preferences {
    const val NIGHT_MODE = "uses_night_mode"
    const val ORIENTATION_LOCK = "orientation_lock_pref"

}

