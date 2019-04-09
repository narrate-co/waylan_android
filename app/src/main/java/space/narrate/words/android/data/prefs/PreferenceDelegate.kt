package space.narrate.words.android.data.prefs

import android.content.SharedPreferences
import androidx.core.content.edit
import java.lang.RuntimeException
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A delegate that handles reading and writing from SharedPreferences
 */
@Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
class PreferenceDelegate<T>(
        private val sharedPrefs: SharedPreferences,
        private val key: String,
        private val default: T
): ReadWriteProperty<Any?, T> {


    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return when (default) {
            is Boolean -> sharedPrefs.getBoolean(key, default)
            is String -> sharedPrefs.getString(key, default)
            is Int -> sharedPrefs.getInt(key, default)
            is Float -> sharedPrefs.getFloat(key, default)
            is Long -> sharedPrefs.getLong(key, default)
            is Set<*> -> sharedPrefs.getStringSet(key, default as Set<String>)
            else -> throw RuntimeException("Unsupported preference type")
        } as T
    }


    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        when (value) {
            is Boolean -> sharedPrefs.edit { putBoolean(key, value) }
            is String -> sharedPrefs.edit { putString(key, value) }
            is Int -> sharedPrefs.edit { putInt(key, value) }
            is Float -> sharedPrefs.edit { putFloat(key, value) }
            is Long -> sharedPrefs.edit { putLong(key, value) }
            is Set<*> -> sharedPrefs.edit { putStringSet(key, value as Set<String>) }
            else -> throw RuntimeException("Unsupported preference type")
        }
    }
}
