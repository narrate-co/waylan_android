package com.words.android.ui.settings

import androidx.lifecycle.ViewModel
import com.words.android.data.firestore.users.User
import com.words.android.data.prefs.PreferenceRepository
import com.words.android.data.prefs.UserPreferenceRepository
import com.words.android.di.FragmentScope
import com.words.android.di.UserScope
import javax.inject.Inject

@UserScope
class SettingsViewModel @Inject constructor(
        private val user: User?,
        private val preferenceRepository: PreferenceRepository,
        private val userPreferenceRepository: UserPreferenceRepository): ViewModel() {

    fun getUser() = user

    var usesDarkMode: Boolean
        get() = preferenceRepository.usesDarkMode
        set(value) {
            preferenceRepository.usesDarkMode = value
        }

    fun clearUserPreferences() = userPreferenceRepository.resetAll()

}

