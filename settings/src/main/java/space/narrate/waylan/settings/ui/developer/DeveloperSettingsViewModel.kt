package space.narrate.waylan.settings.ui.developer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import space.narrate.waylan.core.billing.BillingConfig
import space.narrate.waylan.core.data.Result
import space.narrate.waylan.core.data.firestore.users.PluginState
import space.narrate.waylan.core.data.firestore.users.merriamWebsterState
import space.narrate.waylan.core.data.firestore.users.oneDayPastExpiration
import space.narrate.waylan.core.repo.UserRepository
import space.narrate.waylan.core.ui.common.Event
import space.narrate.waylan.core.ui.common.SnackbarModel
import space.narrate.waylan.core.util.mapTransform
import space.narrate.waylan.settings.R
import java.util.*

class DeveloperSettingsViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    val mwState: LiveData<PluginState>
        get() = userRepository.user.mapTransform {
            it.merriamWebsterState
        }

    val useTestSkus: LiveData<Boolean>
        get() = userRepository.useTestSkusLive

    private val _mwBillingResponse: MutableLiveData<String> = MutableLiveData()
    val mwBillingResponse: LiveData<String>
    get() = _mwBillingResponse

    // TODO: Add Merriam-Webster Thesaurus mocking

    private val _shouldShowSnackbar: MutableLiveData<Event<SnackbarModel>> = MutableLiveData()
    val shouldShowSnackbar: LiveData<Event<SnackbarModel>>
        get() = _shouldShowSnackbar

    init {
        _mwBillingResponse.value = BillingConfig.TEST_SKU_MERRIAM_WEBSTER
    }

    fun onClearPreferencesPreferenceClicked() {
        userRepository.resetPreferences()
        _shouldShowSnackbar.value = Event(SnackbarModel(
            R.string.developer_settings_all_preferences_cleared,
            SnackbarModel.LENGTH_SHORT,
            false
        ))
    }

    fun onMwStatePreferenceClicked() = viewModelScope.launch {
        // cycle state
        val result = userRepository.getUser()

        when (result) {
            is Result.Success -> {
                val user = result.data
                val state = user.merriamWebsterState
                val newState = when {
                    //None -> Free Trial (valid)
                    state is PluginState.None -> {
                        PluginState.FreeTrial(user.isAnonymous)
                    }
                    //FreeTrial (valid) -> FreeTrial (expired)
                    state is PluginState.FreeTrial && state.isValid -> {
                        PluginState.FreeTrial(user.isAnonymous, user.oneDayPastExpiration)
                    }
                    //FreeTrial (expired) -> Purchased (valid)
                    state is PluginState.FreeTrial && !state.isValid -> {
                        PluginState.Purchased(purchaseToken = UUID.randomUUID().toString())
                    }
                    //Purchased (valid) -> Purchased (expired)
                    state is PluginState.Purchased && state.isValid -> {
                        PluginState.Purchased(
                            user.oneDayPastExpiration,
                            user.merriamWebsterPurchaseToken
                        )
                    }
                    //Purchased (expired) -> FreeTrial (valid)
                    state is PluginState.Purchased && !state.isValid -> {
                        PluginState.FreeTrial(user.isAnonymous)
                    }
                    //Default
                    else -> PluginState.None()
                }
                userRepository.setUserMerriamWebsterState(newState)
            }
            is Result.Error -> {
                _shouldShowSnackbar.value = Event(SnackbarModel(
                    R.string.auth_error_message_no_current_user,
                    isError = true
                ))
            }
        }

    }

    fun onUseTestSkusPreferenceClicked() {
        userRepository.useTestSkus = !userRepository.useTestSkus
    }

    fun onMwBillingResponsePreferenceClicked() {
        val newResponse = when (_mwBillingResponse.value) {
            BillingConfig.TEST_SKU_PURCHASED -> BillingConfig.TEST_SKU_CANCELED
            BillingConfig.TEST_SKU_CANCELED -> BillingConfig.TEST_SKU_ITEM_UNAVAILABLE
            BillingConfig.TEST_SKU_ITEM_UNAVAILABLE -> BillingConfig.TEST_SKU_PURCHASED
            else -> BillingConfig.TEST_SKU_PURCHASED
        }
        BillingConfig.TEST_SKU_MERRIAM_WEBSTER = newResponse
        _mwBillingResponse.value = BillingConfig.TEST_SKU_MERRIAM_WEBSTER
    }

    fun onInformativeSnackbarPreferenceClicked() {
        _shouldShowSnackbar.value = Event(SnackbarModel(
            R.string.developer_settings_informative_snackbar_test_message,
            SnackbarModel.LENGTH_SHORT,
            false,
            R.string.developer_settings_informative_snackbar_test_action
        ))
    }

    fun onErrorSnackbarPreferenceClicked() {
        _shouldShowSnackbar.value = Event(SnackbarModel(
            R.string.developer_settings_error_snackbar_test_message,
            SnackbarModel.LENGTH_SHORT,
            true,
            R.string.developer_settings_error_snackbar_test_action
        ))
    }
}