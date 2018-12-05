package com.wordsdict.android.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.wordsdict.android.data.firestore.users.PluginState
import com.wordsdict.android.data.firestore.users.User
import com.wordsdict.android.data.prefs.Orientation
import com.wordsdict.android.data.prefs.UserPreferenceStore
import com.wordsdict.android.data.repository.UserRepository
import com.wordsdict.android.di.UserScope
import javax.inject.Inject

@UserScope
class SettingsViewModel @Inject constructor(
        private val userRepository: UserRepository,
        private val userPreferenceStore: UserPreferenceStore): ViewModel() {

    fun getUserLive(): LiveData<User> = userRepository.getUser()

    var nightMode: Int
        get() = userPreferenceStore.nightMode
        set(value) {
            userPreferenceStore.nightMode = value
        }

    var nightModeLive: LiveData<Int> = userPreferenceStore.nightModeLive

    var useTestSkus: Boolean
        get() = userPreferenceStore.useTestSkus
        set(value) {
            userPreferenceStore.useTestSkus = value
        }

    var useTestSkusLive: LiveData<Boolean> = userPreferenceStore.useTestSkusLive

    var orientation: Orientation
        get() = Orientation.fromActivityInfoScreenOrientation(userPreferenceStore.orientationLock)
        set(value) {
            userPreferenceStore.orientationLock = value.value
        }
    var orientationLive: LiveData<Orientation> = userPreferenceStore.orientationLockLive

    fun clearUserPreferences() = userPreferenceStore.resetAll()

    fun setMerriamWebsterState(state: PluginState) {
        userRepository.setUserMerriamWebsterState(state)
    }

}

