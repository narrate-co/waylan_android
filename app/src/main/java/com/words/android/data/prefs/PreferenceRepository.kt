package com.words.android.data.prefs

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.core.content.edit
import com.words.android.data.firestore.users.User

class PreferenceRepository(
        private val applicationContext: Context,
        private val listener: OnValueChangedListener? = null,
        userId: String? = null
) {

    private val sharedPrefs: SharedPreferences by lazy {
        if (userId == null) {
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        } else {
            applicationContext.getSharedPreferences(userId, Context.MODE_PRIVATE)
        }
    }

    var usesDarkMode: Boolean = false
        get() = sharedPrefs.getBoolean(Preferences.USES_DARK_MODE, false)
        set(value) {
            sharedPrefs.edit {
                putBoolean(Preferences.USES_DARK_MODE, value)
            }
            listener?.onValueChanged(Preferences.USES_DARK_MODE)
            field = value
        }

}

