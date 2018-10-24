package com.words.android.data.prefs

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class PreferenceRepository(
        private val applicationContext: Context
) {


    private val defaultPrefs: SharedPreferences by lazy {
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }

    var usesDarkMode: Boolean by PreferenceDelegate(
            defaultPrefs,
            Preferences.USES_DARK_MODE,
            false
    )

}

