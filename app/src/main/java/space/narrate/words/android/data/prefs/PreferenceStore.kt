package space.narrate.words.android.data.prefs

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import space.narrate.words.android.ui.settings.NightModeRadioItemModel
import space.narrate.words.android.ui.settings.OrientationRadioItemModel
import space.narrate.words.android.util.isAtLeastQ
import space.narrate.words.android.util.mapTransform

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
class PreferenceStore(private val applicationContext: Context) {

    private val defaultPrefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }

    private val defaultNightMode = if (isAtLeastQ) {
        NightMode.SYSTEM_DEFAULT
    } else {
        NightMode.BATTERY_SAVER
    }

    var nightMode: NightMode
        get() = NightMode.fromAppCompatDelegate(
            defaultPrefs.getInt(Preferences.NIGHT_MODE, defaultNightMode.value)
        )
        set(value) {
            defaultPrefs.edit { putInt(Preferences.NIGHT_MODE, value.value) }
        }

    var nightModeLive: LiveData<NightMode> =
        PreferenceLiveData(
            defaultPrefs,
            Preferences.NIGHT_MODE,
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        ).mapTransform {
            NightMode.fromAppCompatDelegate(it)
        }

    val allNightModes: List<NightMode> = NightMode.values().toList().filter {
        if (isAtLeastQ) it != NightMode.BATTERY_SAVER else it != NightMode.SYSTEM_DEFAULT
    }

    private val defaultOrientationLock = Orientation.UNSPECIFIED

    var orientationLock: Orientation
        get() = Orientation.fromActivityInfoScreenOrientation(
            defaultPrefs.getInt(Preferences.ORIENTATION_LOCK, defaultOrientationLock.value)
        )
        set(value) {
            defaultPrefs.edit { putInt(Preferences.ORIENTATION_LOCK, value.value) }
        }

    var orientationLockLive: LiveData<Orientation> =
        Transformations.map(PreferenceLiveData(
            defaultPrefs,
            Preferences.ORIENTATION_LOCK,
            Orientation.UNSPECIFIED.value
        )) {
            Orientation.fromActivityInfoScreenOrientation(it)
        }

    val allOrientations: List<Orientation> = Orientation.values().toList()

    fun resetAll() {
        nightMode = defaultNightMode
        orientationLock = defaultOrientationLock
    }
}

