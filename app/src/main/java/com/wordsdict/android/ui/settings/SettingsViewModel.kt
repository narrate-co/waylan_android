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

/**
 * A ViewModel used by [SettingsFragment] and [DeveloperSettingsFragment]. Handles the manipulation
 * of LiveData exposure of [UserPreferences] and [Preferences].
 *
 * To set or get a preference immediately, use its exposed variable representation.
 *
 * To observe a variable and react to its underlying SharedPreference changes, observe it's
 * exposed LiveData variable.
 */
@UserScope
class SettingsViewModel @Inject constructor(
        private val userRepository: UserRepository,
        private val userPreferenceStore: UserPreferenceStore
): ViewModel() {

    /**
     * A LiveData object to observe the current User and configure settings accordingly
     */
    val userLive: LiveData<User> = userRepository.getUser()

    /**
     * A property used to get and set [Preferences.NIGHT_MODE]
     */
    var nightMode: Int
        get() = userPreferenceStore.nightMode
        set(value) {
            userPreferenceStore.nightMode = value
        }

    /**
     * A LiveData object to observe changes to [Preferences.NIGHT_MODE]
     */
    val nightModeLive: LiveData<Int> = userPreferenceStore.nightModeLive

    /**
     * A property to get and set [UserPrferences.USE_TEST_SKUS]
     */
    var useTestSkus: Boolean
        get() = userPreferenceStore.useTestSkus
        set(value) {
            userPreferenceStore.useTestSkus = value
        }

    /**
     * A LiveData object to observe changes to [UserPreferences.USE_TEST_SKUS]
     */
    var useTestSkusLive: LiveData<Boolean> = userPreferenceStore.useTestSkusLive

    /**
     * A property to get and set [Preferences.ORIENTATION_LOCK]
     */
    var orientation: Orientation
        get() = Orientation.fromActivityInfoScreenOrientation(userPreferenceStore.orientationLock)
        set(value) {
            userPreferenceStore.orientationLock = value.value
        }

    /**
     * A LiveData object to observe changes to [Preferences.OREINTATION_LOCK]
     */
    var orientationLive: LiveData<Orientation> = userPreferenceStore.orientationLockLive

    /**
     * Reset all [UserPreferences] to their default values
     */
    fun clearUserPreferences() = userPreferenceStore.resetAll()

    /**
     * Manually alter the PluginState of the current user's Merriam-Webster plugin
     */
    fun setMerriamWebsterState(state: PluginState) {
        userRepository.setUserMerriamWebsterState(state)
    }

}

