package space.narrate.waylan.settings.ui.addons

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import space.narrate.waylan.core.billing.BillingEvent
import space.narrate.waylan.core.data.firestore.users.AddOn
import space.narrate.waylan.core.data.firestore.users.AddOnAction
import space.narrate.waylan.core.data.firestore.users.AddOnAction.*
import space.narrate.waylan.core.data.firestore.users.AddOnState
import space.narrate.waylan.core.data.firestore.users.User
import space.narrate.waylan.core.data.firestore.users.UserAddOnActionUseCase
import space.narrate.waylan.core.data.firestore.users.state
import space.narrate.waylan.core.repo.AnalyticsRepository
import space.narrate.waylan.core.repo.UserRepository
import space.narrate.waylan.core.ui.common.Event
import space.narrate.waylan.core.ui.common.SnackbarModel
import space.narrate.waylan.core.util.doOnEmission
import space.narrate.waylan.core.util.mapOnTransform
import space.narrate.waylan.core.util.mapTransform
import space.narrate.waylan.settings.R

/**
 * ViewModel for [AddOnsFragment].
 */
class AddOnsViewModel(
    private val userRepository: UserRepository,
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    var user: User? = null

    private var showOnOpenAddOn: AddOn = AddOn.MERRIAM_WEBSTER
    private var hasShownOnOpenAddOn = false

    val addOns: LiveData<List<AddOnItemModel>> = AddOnItemListMediatorLiveData().apply {
        AddOn.values().forEach {
            addSource(userRepository.getUserAddOnLive(it))
        }
    }.doOnEmission { list ->
        // This first time addOn's emits a list of AddOnItemModels, scroll to the position of
        // the showOnOpenAddOn if we have navigated to this Fragment with the intent of showing
        // a specific AddOn in the list.
        val position = list.indexOfFirst { it.addOn == showOnOpenAddOn }
        if (position != -1 && !hasShownOnOpenAddOn) {
            hasShownOnOpenAddOn = true
            viewModelScope.launch {
                delay(250L) // Wait for the adapter to lay out the items.
                _shouldScrollToPosition.value = Event(position)
            }
        }
    }

    private val _currentPosition: MutableLiveData<Int> = MutableLiveData()

    val currentAddOn: LiveData<AddOnItemModel> =
        addOns.mapOnTransform(_currentPosition) { addOns, position ->
            addOns[position]
        }

    private val _shouldScrollToPosition: MutableLiveData<Event<Int>> = MutableLiveData()
    val shouldScrollToPosition: LiveData<Event<Int>>
        get() = _shouldScrollToPosition

    val shouldShowStatusTextLabel: LiveData<Boolean>
        get() = currentAddOn.mapTransform {
            when (it.userAddOn.state) {
                AddOnState.NONE -> false
                else -> true
            }
        }

    private val _shouldShowAccountRequiredDialog: MutableLiveData<Event<Boolean>> =
        MutableLiveData()
    val shouldShowAccountRequiredDialog: LiveData<Event<Boolean>>
        get() = _shouldShowAccountRequiredDialog

    private val _shouldLaunchSignUp: MutableLiveData<Event<Boolean>> =
        MutableLiveData()
    val shouldLaunchSignUp: LiveData<Event<Boolean>>
        get() = _shouldLaunchSignUp

    private val _shouldLaunchLogIn: MutableLiveData<Event<Boolean>> =
        MutableLiveData()
    val shouldLaunchLogIn: LiveData<Event<Boolean>>
        get() = _shouldLaunchLogIn

    private val _shouldLaunchPurchaseFlow: MutableLiveData<Event<PurchaseFlowModel>> =
        MutableLiveData()
    val shouldLaunchPurchaseFlow: LiveData<Event<PurchaseFlowModel>>
        get() = _shouldLaunchPurchaseFlow

    private val _shouldShowSnackbar: MutableLiveData<Event<SnackbarModel>> = MutableLiveData()
    val shouldShowSnackbar: LiveData<Event<SnackbarModel>>
        get() = _shouldShowSnackbar

    init {
        // Keep a local reference of the current User ready and available to be used when
        // performing actions on add-ons.
        userRepository.user.observeForever {
            this.user = it
        }
    }

    /**
     * Set the AddOn to be shown when the list of add-ons is finished loading for the first time.
     */
    fun setShowOnOpenAddOn(addOn: AddOn) {
        showOnOpenAddOn = addOn
    }

    fun onCurrentAddOnPageChanged(position: Int) {
        _currentPosition.value = position
    }

    fun onActionClicked(addOnItemModel: AddOnItemModel, action: AddOnAction) {
        // Block any actions taken on add-ons if a user is anonymous, prompting them to
        // log in or create an account before continuing.
        if (user == null || user?.isAnonymous == true) {
            _shouldShowAccountRequiredDialog.value = Event(true)
            return
        }

        when (action) {
            TRY_FOR_FREE -> {
                userRepository.updateUserAddOn(
                    addOnItemModel.addOn,
                    UserAddOnActionUseCase.TryForFree
                )
                analyticsRepository.logAddOnEvent(addOnItemModel.addOn, action)
            }
            ADD,
            RENEW -> _shouldLaunchPurchaseFlow.value = Event(
                PurchaseFlowModel(addOnItemModel.addOn, action)
            )
        }
    }

    fun onBillingEvent(event: BillingEvent) {
        analyticsRepository.logAddOnBillingEvent(event)
        when (event) {
            is BillingEvent.Purchased -> {
                val message = when (event.addOn) {
                    AddOn.MERRIAM_WEBSTER ->
                        R.string.add_on_merriam_webster_successfully_purchased
                    AddOn.MERRIAM_WEBSTER_THESAURUS ->
                        R.string.add_on_merriam_webster_thesaurus_successfully_purchased
                    AddOn.AMERICAN_HERITAGE ->
                        R.string.add_on_american_heritage_successfully_purchased
                }
                _shouldShowSnackbar.value = Event(SnackbarModel(
                    message,
                    SnackbarModel.LENGTH_SHORT
                ))
                analyticsRepository.logAddOnEvent(event.addOn, ADD)
            }
        }
    }

    fun onAccountRequiredLogInClicked() {
        _shouldLaunchLogIn.value = Event(true)
    }

    fun onAccountRequiredSignUpClicked() {
        _shouldLaunchSignUp.value = Event(true)
    }
}