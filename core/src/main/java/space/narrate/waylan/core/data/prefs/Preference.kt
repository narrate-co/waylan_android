package space.narrate.waylan.core.data.prefs

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import space.narrate.waylan.core.util.DefaultingMutableLiveData
import space.narrate.waylan.core.util.switchMapTransform

/**
 * A class which handles getting and setting a SharedPreference value. This class is able to react
 * to changes in the given [sharedPreferences] in the case of the user changing. If the user
 * changes, observers will be notified of new shared preference values when the new user's
 * SharedPreference document is loaded.
 */
class Preference<T>(
    private val key: String,
    private val defValue: T,
    private val sharedPreferences: DefaultingMutableLiveData<SharedPreferences>
) {

    // get live data
    fun getLive(): LiveData<T> {
        return sharedPreferences.switchMapTransform {
            PreferenceLiveData(it, key, defValue)
        }
    }

    // get value
    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
    fun getValue(): T {
        val prefs = sharedPreferences.valueOrDefault

        return when (defValue) {
            is Boolean -> prefs.getBoolean(key, defValue)
            is String -> prefs.getString(key, defValue)
            is Int -> prefs.getInt(key, defValue)
            is Long -> prefs.getLong(key, defValue)
            is Set<*> -> prefs.getStringSet(key, defValue as Set<String>)
            else -> throw IllegalArgumentException("Unsupported preference type $defValue")
        } as T
    }

    // set value
    @Suppress("UNCHECKED_CAST")
    fun setValue(value: T) {
        sharedPreferences.valueOrDefault.edit {
            when (value) {
                is Boolean -> putBoolean(key, value)
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Set<*> -> putStringSet(key, value as Set<String>)
                else -> throw IllegalArgumentException("Unsupported preference type $defValue")
            }
        }
    }

    fun clear() {
        setValue(defValue)
    }
}