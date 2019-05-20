package space.narrate.words.android.data.prefs

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import java.lang.RuntimeException

/**
 * A LiveData object that makes it easy to observe values in [SharedPreferences]
 */
class PreferenceLiveData<T>(
        private val sharedPrefs: SharedPreferences,
        private val key: String,
        private val default: T
): LiveData<T>() {


    override fun onActive() {
        super.onActive()
        value = getPreference()
        sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onInactive() {
        super.onInactive()
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == this.key) {
            value = getPreference()
        }
    }

    private fun getPreference(): T {
        return  when (default) {
            is Boolean -> sharedPrefs.getBoolean(key, default) as T
            is String -> sharedPrefs.getString(key, default) as T
            is Long -> sharedPrefs.getLong(key, default) as T
            is Int -> sharedPrefs.getInt(key, default) as T
            is Set<*> -> sharedPrefs.getStringSet(key, default as Set<String>) as T
            else -> throw RuntimeException("Unsupported preference listType")
        }
    }

}

