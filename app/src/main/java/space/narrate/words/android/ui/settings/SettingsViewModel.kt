package space.narrate.words.android.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import space.narrate.words.android.data.firestore.users.User
import space.narrate.words.android.data.prefs.NightMode
import space.narrate.words.android.data.prefs.Orientation
import space.narrate.words.android.data.repository.UserRepository
import space.narrate.words.android.ui.common.Event
import space.narrate.words.android.ui.auth.AuthRoute

/**
 * A ViewModel used by [SettingsFragment] and [DeveloperSettingsFragment]. Handles the manipulation
 * of LiveData exposure of [UserPreferences] and [Preferences].
 *
 * To set or get a preference immediately, use its exposed variable representation.
 *
 * To observe a variable and react to its underlying SharedPreference changes, observe it's
 * exposed LiveData variable.
 */
class SettingsViewModel(
        private val userRepository: UserRepository
): ViewModel() {

    val user: LiveData<User> = userRepository.user

    val nightMode: LiveData<NightMode> = userRepository.nightModeLive

    var orientation: LiveData<Orientation> = userRepository.orientationLockLive

    val bannerModel: LiveData<MwBannerModel> = Transformations.map(userRepository.user) {
        MwBannerModel.create(it)
    }

    private val _shouldLaunchAuth: MutableLiveData<Event<AuthRoute>> =
        MutableLiveData()
    val shouldLaunchAuth: LiveData<Event<AuthRoute>>
        get() = _shouldLaunchAuth

    private val _shouldLaunchMwPurchaseFlow: MutableLiveData<Event<Boolean>> = MutableLiveData()
    val shouldLaunchMwPurchaseFlow: LiveData<Event<Boolean>>
        get() = _shouldLaunchMwPurchaseFlow

    private val _shouldShowNightModeDialog: MutableLiveData<Event<List<NightModeRadioItemModel>>>
        = MutableLiveData()
    val shouldShowNightModeDialog: LiveData<Event<List<NightModeRadioItemModel>>>
        get() = _shouldShowNightModeDialog

    private val _shouldShowOrientationDialog: MutableLiveData<Event<List<OrientationRadioItemModel>>>
        = MutableLiveData()
    val shouldShowOrientationDialog: LiveData<Event<List<OrientationRadioItemModel>>>
        get() = _shouldShowOrientationDialog

    fun onMwBannerActionClicked(action: MwBannerAction?) {
        when (action) {
            MwBannerAction.LOG_IN -> _shouldLaunchAuth.value = Event(AuthRoute.LOG_IN)
            MwBannerAction.SIGN_UP -> _shouldLaunchAuth.value = Event(AuthRoute.SIGN_UP)
            MwBannerAction.LAUNCH_PURCHASE_FLOW -> _shouldLaunchMwPurchaseFlow.value = Event(true)
        }
    }

//    fun onBannerTopButtonClicked() {
//        if (user.value?.isAnonymous == true) {
//            _shouldLaunchAuth.value = Event(AuthRoute.SIGN_UP)
//        } else {
//            _shouldLaunchMwPurchaseFlow.value = Event(true)
//        }
//    }
//
//    fun onBannerBottomButtonClicked() {
//        if (user.value?.isAnonymous == true) {
//            _shouldLaunchAuth.value = Event(AuthRoute.LOG_IN)
//        } else {
//            _shouldLaunchMwPurchaseFlow.value = Event(true)
//        }
//    }

    fun onNightModePreferenceClicked() {
        val currentNightMode = userRepository.nightMode
        _shouldShowNightModeDialog.value = Event(userRepository.allNightModes.map {
            NightModeRadioItemModel(it, it == currentNightMode)
        }
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
        _shouldLaunchAuth.value = Event(AuthRoute.LOG_IN)
    }


}

