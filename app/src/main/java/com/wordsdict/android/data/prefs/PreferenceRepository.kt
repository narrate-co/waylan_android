package com.wordsdict.android.data.prefs

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations

/**
 * A repo for global preferences
 *
 * Some preferences need to be accessible before the User scope has been established
 * such as set basic defaults like orientation and theme
 *
 * It should <b>not</b> be necessary to write directly to preferences here using a [PreferenceRepository] instance
 *
 * This should be a class that is only read from and used to simply separate preference scope, aiding
 * developer understanding
 *
 * To write to these preferences, do so via [UserPreferenceRepository]
 */
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


     var orientationLock: Int by PreferenceDelegate(
             defaultPrefs,
             Preferences.ORIENTATION_LOCK,
             Orientation.UNSPECIFIED.value
     )
    var orientationLive: LiveData<Orientation> =
            Transformations.map(PreferenceLiveData(defaultPrefs, Preferences.ORIENTATION_LOCK, Orientation.UNSPECIFIED.value)) {
                Orientation.fromActivityInfoScreenOrientation(it)
            }

}

