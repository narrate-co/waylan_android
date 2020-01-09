package space.narrate.waylan.settings.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import space.narrate.waylan.core.billing.BillingConfig
import space.narrate.waylan.core.data.prefs.NightMode
import space.narrate.waylan.core.data.prefs.Orientation
import space.narrate.waylan.core.repo.UserRepository
import space.narrate.waylan.core.ui.common.Event
import space.narrate.waylan.core.util.mapTransform
import space.narrate.waylan.settings.R

/**
 * A ViewModel used by [SettingsFragment] and [DeveloperSettingsFragment]. Handles the manipulation
 * of LiveData exposure of [UserPreferences] and [Preferences].
 *
 * To set or get a preference immediately, use its exposed variable representation.
 *
 * To observe a variable and react to its underlying SharedPreference changes, observe it's
 * exposed LiveData variable.
 */
class SettingsViewModel(private val userRepository: UserRepository): ViewModel() {

    val nightMode: LiveData<NightMode>
        get() = userRepository.nightModeLive

    val orientation: LiveData<Orientation>
        get() = userRepository.orientationLockLive

    val logInSignOut: LiveData<LogInSignOutModel>
        get() = userRepository.user.mapTransform {
            when {
                it.isAnonymous -> LogInSignOutModel(
                    R.string.settings_log_in_sign_up_title,
                    R.string.settings_log_in_sign_up_desc
                )
                it.email.isNotBlank() -> LogInSignOutModel(
                    R.string.settings_sign_out_title,
                    descString = it.email
                )
                else -> LogInSignOutModel(
                    R.string.settings_sign_out_title,
                    R.string.settings_sign_out_default_desc
                )
            }
        }

    private val _shouldLaunchSignUp: MutableLiveData<Event<Boolean>> =
        MutableLiveData()
    val shouldLaunchSignUp: LiveData<Event<Boolean>>
        get() = _shouldLaunchSignUp

    private val _shouldLaunchLogIn: MutableLiveData<Event<Boolean>> =
        MutableLiveData()
    val shouldLaunchLogIn: LiveData<Event<Boolean>>
        get() = _shouldLaunchLogIn

    private val _shouldShowNightModeDialog: MutableLiveData<Event<List<NightModeRadioItemModel>>>
        = MutableLiveData()
    val shouldShowNightModeDialog: LiveData<Event<List<NightModeRadioItemModel>>>
        get() = _shouldShowNightModeDialog

    private val _shouldShowOrientationDialog: MutableLiveData<Event<List<OrientationRadioItemModel>>>
        = MutableLiveData()
    val shouldShowOrientationDialog: LiveData<Event<List<OrientationRadioItemModel>>>
        get() = _shouldShowOrientationDialog

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
        _shouldLaunchLogIn.value = Event(true)
    }
}

