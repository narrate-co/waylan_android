package com.wordsdict.android.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.wordsdict.android.data.firestore.users.PluginState
import com.wordsdict.android.data.firestore.users.User
import com.wordsdict.android.data.prefs.PreferenceRepository
import com.wordsdict.android.data.prefs.UserPreferenceRepository
import com.wordsdict.android.data.repository.UserRepository
import com.wordsdict.android.di.UserScope
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

    var useTestSkus: Boolean
        get() = preferenceRepository.useTestSkus
        set(value) {
            preferenceRepository.useTestSkus = value
        }

    var useTestSkusLive: LiveData<Boolean> = preferenceRepository.useTestSkusLive

    fun clearUserPreferences() = userPreferenceRepository.resetAll()

    fun setMerriamWebsterState(state: PluginState) {
        userRepository.setUserMerriamWebsterState(state)
    }

}

