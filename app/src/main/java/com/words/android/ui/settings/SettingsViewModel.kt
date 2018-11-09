package com.words.android.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.words.android.data.firestore.users.PluginState
import com.words.android.data.firestore.users.User
import com.words.android.data.prefs.PreferenceRepository
import com.words.android.data.prefs.UserPreferenceRepository
import com.words.android.data.repository.UserRepository
import com.words.android.di.UserScope
import javax.inject.Inject

@UserScope
class SettingsViewModel @Inject constructor(
        private val userRepository: UserRepository,
        private val preferenceRepository: PreferenceRepository,
        private val userPreferenceRepository: UserPreferenceRepository): ViewModel() {

    fun getUserLive(): LiveData<User> = userRepository.getUser()

    var nightMode: Int
        get() = preferenceRepository.nightMode
        set(value) {
            preferenceRepository.nightMode = value
        }

    var nightModeLive: LiveData<Int> = preferenceRepository.nightModeLive

    fun clearUserPreferences() = userPreferenceRepository.resetAll()

    fun setMerriamWebsterState(state: PluginState) {
        userRepository.setUserMerriamWebsterState(state)
    }

}

