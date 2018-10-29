package com.words.android.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.words.android.data.firestore.users.PluginState
import com.words.android.data.firestore.users.User
import com.words.android.data.prefs.PreferenceRepository
import com.words.android.data.prefs.UserPreferenceRepository
import com.words.android.data.repository.UserRepository
import com.words.android.data.repository.WordRepository
import com.words.android.di.FragmentScope
import com.words.android.di.UserScope
import com.words.android.util.daysElapsed
import javax.inject.Inject

@UserScope
class SettingsViewModel @Inject constructor(
        private val userRepository: UserRepository,
        private val preferenceRepository: PreferenceRepository,
        private val userPreferenceRepository: UserPreferenceRepository): ViewModel() {

    fun getUserLive(): LiveData<User> = userRepository.getUser()

    var usesDarkMode: Boolean
        get() = preferenceRepository.usesDarkMode
        set(value) {
            preferenceRepository.usesDarkMode = value
        }

    fun clearUserPreferences() = userPreferenceRepository.resetAll()

    fun setMerriamWebsterState(state: PluginState) {
        userRepository.setUserMerriamWebsterState(state)
    }

}

