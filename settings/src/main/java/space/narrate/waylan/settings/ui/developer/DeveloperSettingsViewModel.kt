package space.narrate.waylan.settings.ui.developer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import space.narrate.waylan.core.billing.BillingConfig
import space.narrate.waylan.core.data.Result
import space.narrate.waylan.core.data.firestore.users.AddOn
import space.narrate.waylan.core.data.firestore.users.AddOnState
import space.narrate.waylan.core.data.firestore.users.UserAddOnActionUseCase
import space.narrate.waylan.core.data.firestore.users.state
import space.narrate.waylan.core.repo.UserRepository
import space.narrate.waylan.core.ui.common.Event
import space.narrate.waylan.core.ui.common.SnackbarModel
import space.narrate.waylan.core.util.mapTransform
import space.narrate.waylan.core.util.minusDays
import space.narrate.waylan.settings.R
import java.util.*

class DeveloperSettingsViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    val isAnonymousUser: LiveData<Boolean>
        get() = userRepository.user.mapTransform { it.isAnonymous }

    val mwState: LiveData<String>
        get() = userRepository.getUserAddOnLive(AddOn.MERRIAM_WEBSTER).mapTransform {
            getLabelForAddOnState(it.state)
        }

    val mwThesaurusState: LiveData<String>
        get() = userRepository.getUserAddOnLive(AddOn.MERRIAM_WEBSTER_THESAURUS).mapTransform {
            getLabelForAddOnState(it.state)
        }

    val ahdState: LiveData<String>
        get() = userRepository.getUserAddOnLive(AddOn.AMERICAN_HERITAGE).mapTransform {
            getLabelForAddOnState(it.state)
        }

    val useTestSkus: LiveData<Boolean>
        get() = userRepository.useTestSkusLive

    private val _billingResponse: MutableLiveData<String> = MutableLiveData()
    val billingResponse: LiveData<String>
    get() = _billingResponse


    private val _shouldShowSnackbar: MutableLiveData<Event<SnackbarModel>> = MutableLiveData()
    val shouldShowSnackbar: LiveData<Event<SnackbarModel>>
        get() = _shouldShowSnackbar

    init {
        _billingResponse.value = BillingConfig.TEST_SKU
    }

    private fun getLabelForAddOnState(state: AddOnState): String {
        return when (state) {
            AddOnState.NONE -> "None"
            AddOnState.FREE_TRIAL_VALID ->
                "Free trial (valid)"
            AddOnState.FREE_TRIAL_EXPIRED ->
                "Free trial (expired)"
            AddOnState.PURCHASED_VALID ->
                "Purchased (valid)"
            AddOnState.PURCHASED_EXPIRED ->
                "Purchased (expired)"
        }
    }

    fun onClearPreferencesPreferenceClicked() {
        userRepository.resetPreferences()
        _shouldShowSnackbar.value = Event(SnackbarModel(
            R.string.developer_settings_all_preferences_cleared,
            SnackbarModel.LENGTH_SHORT,
            false
        ))
    }

    fun onIsAnonymousUserPreferenceClicked() {
        userRepository.updateUserWith {
            isAnonymous = !isAnonymous
        }
    }

    fun onMwStatePreferenceClicked() {
        toggleUserAddOn(AddOn.MERRIAM_WEBSTER)
    }

    fun onMwThesaurusPreferenceClicked() {
        toggleUserAddOn(AddOn.MERRIAM_WEBSTER_THESAURUS)
    }

    fun onAdhPreferenceClicked() {
        toggleUserAddOn(AddOn.AMERICAN_HERITAGE)
    }

    private fun toggleUserAddOn(addOn: AddOn) = viewModelScope.launch {
        // cycle state
        val result = userRepository.getUserAddOn(addOn)
        if (result is Result.Success) {
            val useCase = UserAddOnActionUseCase.Manual {
                when (result.data.state) {
                    // PURCHASED_EXPIRED -> NONE
                    AddOnState.PURCHASED_EXPIRED -> {
                        hasStartedFreeTrial = false
                        purchaseToken = ""
                        isAwareOfExpiration = false
                    }
                    // NONE -> FREE_TRIAL_VALID
                    AddOnState.NONE -> {
                        hasStartedFreeTrial = true
                        started = Date()
                        validDurationDays = 30L
                    }
                    // FREE_TRIAL_VALID -> FREE_TRIAL_EXPIRED
                    AddOnState.FREE_TRIAL_VALID -> {
                        started = Date().minusDays(validDurationDays + 1)
                    }
                    // FREE_TRIAL_EXPIRED -> PURCHASED_VALID
                    AddOnState.FREE_TRIAL_EXPIRED -> {
                        purchaseToken = "abc"
                        started = Date()
                        validDurationDays = 365L
                        isAwareOfExpiration = false
                    }
                    // PURCHASED_VALID -> PURCHASED_EXPIRED
                    AddOnState.PURCHASED_VALID -> {
                        started = Date().minusDays(validDurationDays + 1)
                    }
                }
            }
            userRepository.updateUserAddOn(addOn, useCase)
        } else if (result is Result.Error){
            _shouldShowSnackbar.value = Event(SnackbarModel(
                // TODO: Change error message to use actual exception
                R.string.auth_error_message_no_current_user,
                isError = true
            ))
        }
    }

    fun onUseTestSkusPreferenceClicked() {
        userRepository.useTestSkus = !userRepository.useTestSkus
    }

    fun onBillingResponsePreferenceClicked() {
        val newResponse = when (_billingResponse.value) {
            BillingConfig.TEST_SKU_PURCHASED -> BillingConfig.TEST_SKU_CANCELED
            BillingConfig.TEST_SKU_CANCELED -> BillingConfig.TEST_SKU_ITEM_UNAVAILABLE
            BillingConfig.TEST_SKU_ITEM_UNAVAILABLE -> BillingConfig.TEST_SKU_PURCHASED
            else -> BillingConfig.TEST_SKU_PURCHASED
        }
        BillingConfig.TEST_SKU = newResponse
        _billingResponse.value = BillingConfig.TEST_SKU
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