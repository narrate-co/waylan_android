package space.narrate.words.android.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import space.narrate.words.android.data.firestore.users.User
import space.narrate.words.android.data.prefs.NightMode
import space.narrate.words.android.data.prefs.Orientation
import space.narrate.words.android.data.repository.UserRepository
import space.narrate.words.android.ui.Event
import space.narrate.words.android.ui.auth.AuthActivity
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

    val nightMode: LiveData<NightMode> = userRepository.nightModeLive

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

    private val _shouldShowNightModeDialog: MutableLiveData<Event<List<NightModeRadioItemModel>>> = MutableLiveData()
    val shouldShowNightModeDialog: LiveData<Event<List<NightModeRadioItemModel>>>
        get() = _shouldShowNightModeDialog

    private val _shouldShowOrientationDialog: MutableLiveData<Event<List<OrientationRadioItemModel>>> =
        MutableLiveData()
    val shouldShowOrientationDialog: LiveData<Event<List<OrientationRadioItemModel>>>
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
        val currentNightMode = userRepository.nightMode
        _shouldShowNightModeDialog.value = Event(userRepository.allNightModes.map {
            NightModeRadioItemModel(it, it == currentNightMode) }
        )
    }

    fun onNightModeSelected(item: NightModeRadioItemModel) {
        userRepository.nightMode = item.nightMode
    }

    fun onOrientationPreferenceClicked() {
        val currentOrientation = userRepository.orientationLock
        _shouldShowOrientationDialog.value = Event(userRepository.allOrientations.map {
                OrientationRadioItemModel(it, it == currentOrientation)
        })
    }

    fun onOrientationSelected(item: OrientationRadioItemModel) {
        userRepository.orientationLock = item.orientation
    }

    fun onSignOutClicked() {
        _shouldLaunchAuth.value = Event(AuthActivity.AuthRoute.LOG_IN)
    }


}

