package com.wordsdict.android.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.wordsdict.android.data.firestore.users.PluginState
import com.wordsdict.android.data.firestore.users.User
import com.wordsdict.android.data.prefs.Orientation
import com.wordsdict.android.data.prefs.PreferenceRepository
import com.wordsdict.android.data.prefs.UserPreferenceRepository
import com.wordsdict.android.data.repository.UserRepository
import com.wordsdict.android.di.UserScope
import javax.inject.Inject

@UserScope
class SettingsViewModel @Inject constructor(
        private val userRepository: UserRepository,
        private val userPreferenceRepository: UserPreferenceRepository): ViewModel() {

    fun getUserLive(): LiveData<User> = userRepository.getUser()

    var nightMode: Int
        get() = userPreferenceRepository.nightMode
        set(value) {
            userPreferenceRepository.nightMode = value
        }

    var nightModeLive: LiveData<Int> = userPreferenceRepository.nightModeLive

    var useTestSkus: Boolean
        get() = userPreferenceRepository.useTestSkus
        set(value) {
            userPreferenceRepository.useTestSkus = value
        }

    var useTestSkusLive: LiveData<Boolean> = userPreferenceRepository.useTestSkusLive

    var orientation: Orientation
        get() = Orientation.fromActivityInfoScreenOrientation(userPreferenceRepository.orientationLock)
        set(value) {
            userPreferenceRepository.orientationLock = value.value
        }
    var orientationLive: LiveData<Orientation> = userPreferenceRepository.orientationLockLive

    fun clearUserPreferences() = userPreferenceRepository.resetAll()

    fun setMerriamWebsterState(state: PluginState) {
        userRepository.setUserMerriamWebsterState(state)
    }

}

