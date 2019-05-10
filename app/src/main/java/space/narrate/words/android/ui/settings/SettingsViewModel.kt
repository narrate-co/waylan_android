package space.narrate.words.android.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import space.narrate.words.android.R
import space.narrate.words.android.data.firestore.users.PluginState
import space.narrate.words.android.data.firestore.users.User
import space.narrate.words.android.data.firestore.users.merriamWebsterState
import space.narrate.words.android.data.firestore.users.oneDayPastExpiration
import space.narrate.words.android.data.prefs.Orientation
import space.narrate.words.android.data.repository.UserRepository
import space.narrate.words.android.ui.Event
import space.narrate.words.android.ui.auth.AuthActivity
import space.narrate.words.android.ui.common.SnackbarModel
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

    val user: LiveData<User> = userRepository.getUser()

    val nightMode: LiveData<Int> = userRepository.nightModeLive

    var orientation: LiveData<Orientation> = userRepository.orientationLockLive

    val bannerModel: LiveData<MwBannerModel> = Transformations.map(userRepository.getUser()) {
        MwBannerModel.create(it)
    }

    private val _shouldLaunchAuth: MutableLiveData<Event<AuthActivity.AuthRoute>> =
        MutableLiveData()
    val shouldLaunchAuth: LiveData<Event<AuthActivity.AuthRoute>>
        get() = _shouldLaunchAuth

    private val _shouldLaunchMwPurchaseFlow: MutableLiveData<Event<Boolean>> = MutableLiveData()
    val shouldLaunchMwPurchaseFlow: LiveData<Event<Boolean>>
        get() = _shouldLaunchMwPurchaseFlow

    private val _shouldShowNightModeDialog: MutableLiveData<Event<Int>> = MutableLiveData()
    val shouldShowNightModeDialog: LiveData<Event<Int>>
        get() = _shouldShowNightModeDialog

    private val _shouldShowOrientationDialog: MutableLiveData<Event<Orientation>> =
        MutableLiveData()
    val shouldShowOrientationDialog: LiveData<Event<Orientation>>
        get() = _shouldShowOrientationDialog


    fun onBannerTopButtonClicked() {
        if (user.value?.isAnonymous == true) {
            _shouldLaunchAuth.value = Event(AuthActivity.AuthRoute.SIGN_UP)
        } else {
            _shouldLaunchMwPurchaseFlow.value = Event(true)
        }
    }

    fun onBannerBottomButtonClicked() {
        if (user.value?.isAnonymous == true) {
            _shouldLaunchAuth.value = Event(AuthActivity.AuthRoute.LOG_IN)
        } else {
            _shouldLaunchMwPurchaseFlow.value = Event(true)
        }
    }

    fun onNightModePreferenceClicked() {
        _shouldShowNightModeDialog.value = Event(userRepository.nightMode)
    }

    fun onNightModeSelected(mode: Int) {
        userRepository.nightMode = mode
    }

    fun onOrientationPreferenceClicked() {
        _shouldShowOrientationDialog.value = Event(
            Orientation.fromActivityInfoScreenOrientation(userRepository.orientationLock)
        )
    }

    fun onOrientationSelected(orientation: Orientation) {
        userRepository.orientationLock = orientation.value
    }

    fun onSignOutClicked() {
        _shouldLaunchAuth.value = Event(AuthActivity.AuthRoute.LOG_IN)
    }


}

