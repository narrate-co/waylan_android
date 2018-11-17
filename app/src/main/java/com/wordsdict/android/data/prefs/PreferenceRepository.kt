package com.wordsdict.android.data.prefs

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData

class PreferenceRepository(
        private val applicationContext: Context
) {


    private val defaultPrefs: SharedPreferences by lazy {
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }

    var nightMode: Int by PreferenceDelegate(
            defaultPrefs,
            Preferences.NIGHT_MODE,
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    )

    var nightModeLive: LiveData<Int> =
            PreferenceLiveData(defaultPrefs, Preferences.NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

}

