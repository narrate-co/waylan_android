package space.narrate.words.android.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import space.narrate.words.android.R
import space.narrate.words.android.billing.BillingConfig
import space.narrate.words.android.data.firestore.users.PluginState
import space.narrate.words.android.data.firestore.users.User
import space.narrate.words.android.data.firestore.users.merriamWebsterState
import space.narrate.words.android.data.firestore.users.oneDayPastExpiration
import space.narrate.words.android.data.repository.UserRepository
import space.narrate.words.android.ui.Event
import space.narrate.words.android.ui.common.SnackbarModel
import javax.inject.Inject

class DeveloperSettingsViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    val user: LiveData<User> = userRepository.getUser()

    val mwState: LiveData<PluginState> = Transformations.map(user) {
        it.merriamWebsterState
    }

    var useTestSkus: LiveData<Boolean> = userRepository.useTestSkusLive

    private val _mwBillingResponse: MutableLiveData<String> = MutableLiveData()
    val mwBillingResponse: LiveData<String>
    get() = _mwBillingResponse

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
            false,
            false
        ))
    }

    fun onMwStatePreferenceClicked() {
        // cycle state
        val user = user.value ?: return

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
                PluginState.Purchased(purchaseToken = user.merriamWebsterPurchaseToken)
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
            false,
            R.string.developer_settings_informative_snackbar_test_action
        ))
    }

    fun onErrorSnackbarPreferenceClicked() {
        _shouldShowSnackbar.value = Event(SnackbarModel(
            R.string.developer_settings_error_snackbar_test_message,
            SnackbarModel.LENGTH_SHORT,
            true,
            false,
            R.string.developer_settings_error_snackbar_test_action
        ))
    }
}