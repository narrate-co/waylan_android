package com.wordsdict.android.data.prefs

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.arch.core.util.Function
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations

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


     var orientationLock: String by PreferenceDelegate(
             defaultPrefs,
             Preferences.ORIENTATION_LOCK,
             Orientation.UNSPECIFIED.name
     )
    var orientationLive: LiveData<Orientation> =
            Transformations.map(PreferenceLiveData(defaultPrefs, Preferences.ORIENTATION_LOCK, Orientation.UNSPECIFIED.name)) {
                Orientation.valueOf(it)
            }

    var useTestSkus: Boolean by PreferenceDelegate(
            defaultPrefs,
            Preferences.USE_TEST_SKUS,
            false
    )
    val useTestSkusLive: LiveData<Boolean> =
            PreferenceLiveData(defaultPrefs, Preferences.USE_TEST_SKUS, false)
}

