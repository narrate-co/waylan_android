package com.wordsdict.android.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.wordsdict.android.data.firestore.users.PluginState
import com.wordsdict.android.data.firestore.users.User
import com.wordsdict.android.data.prefs.Orientation
import com.wordsdict.android.data.repository.UserRepository
import javax.inject.Inject

/**
 * A ViewModel used by [SettingsFragment] and [DeveloperSettingsFragment]. Handles the manipulation
 * of LiveData exposure of [UserPreferences] and [Preferences].
 *
 * To set or get a preference immediately, use its exposed variable representation.
 *
 * To observe a variable and react to its underlying SharedPreference changes, observe it's
 * exposed LiveData variable.
 */
class SettingsViewModel @Inject constructor(
        private val userRepository: UserRepository
): ViewModel() {

    /**
     * A LiveData object to observe the current User and configure settings accordingly
     */
    val userLive: LiveData<User> = userRepository.getUser()

    /**
     * A property used to get and set [Preferences.NIGHT_MODE]
     */
    var nightMode: Int
        get() = userRepository.nightMode
        set(value) {
            userRepository.nightMode = value
        }

    /**
     * A LiveData object to observe changes to [Preferences.NIGHT_MODE]
     */
    val nightModeLive: LiveData<Int> = userRepository.nightModeLive

    /**
     * A property to get and set [UserPrferences.USE_TEST_SKUS]
     */
    var useTestSkus: Boolean
        get() = userRepository.useTestSkus
        set(value) {
            userRepository.useTestSkus = value
        }

    /**
     * A LiveData object to observe changes to [UserPreferences.USE_TEST_SKUS]
     */
    var useTestSkusLive: LiveData<Boolean> = userRepository.useTestSkusLive

    /**
     * A property to get and set [Preferences.ORIENTATION_LOCK]
     */
    var orientation: Orientation
        get() = Orientation.fromActivityInfoScreenOrientation(userRepository.orientationLock)
        set(value) {
            userRepository.orientationLock = value.value
        }

    /**
     * A LiveData object to observe changes to [Preferences.OREINTATION_LOCK]
     */
    var orientationLive: LiveData<Orientation> = userRepository.orientationLockLive

    /**
     * Reset all [UserPreferences] to their default values
     */
    fun clearUserPreferences() = userRepository.resetPreferences()

    /**
     * Manually alter the PluginState of the current user's Merriam-Webster plugin
     */
    fun setMerriamWebsterState(state: PluginState) {
        userRepository.setUserMerriamWebsterState(state)
    }

}

