package space.narrate.words.android.data.prefs

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import space.narrate.words.android.util.DefaultingMutableLiveData
import space.narrate.words.android.util.mapTransform

/**
 * A wrapper class for [Preference] which allows marshalling from a non-supported SharedPreference
 * type to a supported SharedPreference type. For example, use this class if you want to store
 * a List<Enum> or other non-primitive types.
 */
class BoxedPreference<R, T>(
    key: String,
    defValue: R,
    sharedPreferences: DefaultingMutableLiveData<SharedPreferences>,
    private val from: (R) -> T,
    private val to: (T) -> R
) {

    private val preference = Preference(key, from(defValue), sharedPreferences)

    fun getLive(): LiveData<R> {
        return preference.getLive().mapTransform {
            to(it)
        }
    }

    fun getValue(): R {
        return to(preference.getValue())
    }

    fun setValue(value: R) {
        preference.setValue(from(value))
    }

    fun clear() {
        preference.clear()
    }

}