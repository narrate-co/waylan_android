package com.wordsdict.android.data.prefs

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations

/**
 * A top-level store for default (non user tied) [SharedPreferences]
 *
 * Some preferences need to be accessible outside of [UserScope]. [Preferences] and
 * [PreferenceStore] are for those variables.
 *
 * It should <b>not</b> be necessary to write directly [Preferences] directly using
 * [PreferenceStore]. This should be a class that is only read from and used to simply separate
 * where preferences are stored (either in default [SharedPreferences] or a user tied
 * instance), aiding code/function clarity. To write to these preferences, do so using
 * [UserPreferenceStore].
 */
class PreferenceStore(
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
            PreferenceLiveData(
                    defaultPrefs,
                    Preferences.NIGHT_MODE,
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )


     var orientationLock: Int by PreferenceDelegate(
             defaultPrefs,
             Preferences.ORIENTATION_LOCK,
             Orientation.UNSPECIFIED.value
     )
    var orientationLive: LiveData<Orientation> =
            Transformations.map(PreferenceLiveData(
                    defaultPrefs,
                    Preferences.ORIENTATION_LOCK,
                    Orientation.UNSPECIFIED.value
            )) {
                Orientation.fromActivityInfoScreenOrientation(it)
            }

}

