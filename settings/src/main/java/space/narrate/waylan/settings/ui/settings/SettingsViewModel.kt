package space.narrate.waylan.settings.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import space.narrate.waylan.settings.billing.BillingConfig
import space.narrate.waylan.core.data.prefs.NightMode
import space.narrate.waylan.core.data.prefs.Orientation
import space.narrate.waylan.core.repo.UserRepository
import space.narrate.waylan.core.ui.common.Event
import space.narrate.waylan.core.util.mapTransform

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

    val bannerModel: LiveData<MwBannerModel>
        get() = userRepository.user.mapTransform {
            MwBannerModel.create(it)
        }

    private val _shouldLaunchSignUp: MutableLiveData<Event<Boolean>> =
        MutableLiveData()
    val shouldLaunchSignUp: LiveData<Event<Boolean>>
        get() = _shouldLaunchSignUp

    private val _shouldLaunchLogIn: MutableLiveData<Event<Boolean>> =
        MutableLiveData()
    val shouldLaunchLogIn: LiveData<Event<Boolean>>
        get() = _shouldLaunchLogIn

    private val _shouldLaunchMwPurchaseFlow: MutableLiveData<Event<PurchaseFlowModel>> =
        MutableLiveData()
    val shouldLaunchMwPurchaseFlow: LiveData<Event<PurchaseFlowModel>>
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
            MwBannerAction.LOG_IN -> _shouldLaunchLogIn.value = Event(true)
            MwBannerAction.SIGN_UP -> _shouldLaunchSignUp.value = Event(true)
            MwBannerAction.LAUNCH_PURCHASE_FLOW -> {
                val sku = if (userRepository.useTestSkus) {
                    BillingConfig.TEST_SKU_MERRIAM_WEBSTER
                } else {
                    BillingConfig.SKU_MERRIAM_WEBSTER
                }
                _shouldLaunchMwPurchaseFlow.value = Event(PurchaseFlowModel(sku))
            }
        }
    }

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

