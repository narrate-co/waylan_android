package com.wordsdict.android.data.prefs

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import java.lang.RuntimeException

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
            else -> throw RuntimeException("Unsupported preference type")
        }
    }

}

