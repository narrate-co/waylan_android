package space.narrate.waylan.android.data.prefs

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import space.narrate.waylan.android.util.DefaultingMutableLiveData
import space.narrate.waylan.core.util.isAtLeastQ

/**
 * A top-level store for default (non user tied) [SharedPreferences]
 *
 * Some preferences need to be accessible outside of [UserScope]. [Preferences] and
 * [PreferenceStore] are for those variables.
 *
 * It should <b>not</b> be necessary to write to [Preferences] directly using
 * [PreferenceStore]. This should be a class that is only read from and used to separate
 * where preferences are stored (either in default [SharedPreferences] or a user tied
 * instance), aiding code clarity. To write to these preferences, do so using
 * [UserRepository].
 */
class PreferenceStore(applicationContext: Context) {

    private val defaultPrefs = DefaultingMutableLiveData(
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
    )

    private val defaultNightMode = if (isAtLeastQ) {
        NightMode.SYSTEM_DEFAULT
    } else {
        NightMode.BATTERY_SAVER
    }

    val nightMode = BoxedPreference(
        Preferences.NIGHT_MODE,
        defaultNightMode,
        defaultPrefs,
        { it.value },
        { NightMode.fromAppCompatDelegate(it) }
    )

    val allNightModes: List<NightMode> = NightMode.values().toList().filter {
        if (isAtLeastQ) it != NightMode.BATTERY_SAVER else it != NightMode.SYSTEM_DEFAULT
    }

    val orientationLock = BoxedPreference(
        Preferences.ORIENTATION_LOCK,
        Orientation.UNSPECIFIED,
        defaultPrefs,
        { it.value },
        { Orientation.fromActivityInfoScreenOrientation(it) }
    )

    val allOrientations: List<Orientation> = Orientation.values().toList()

    fun resetAll() {
        nightMode.clear()
        orientationLock.clear()
    }
}

