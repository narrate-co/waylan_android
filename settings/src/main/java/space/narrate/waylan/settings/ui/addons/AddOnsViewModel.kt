package space.narrate.waylan.settings.ui.addons

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import space.narrate.waylan.core.billing.BillingEvent
import space.narrate.waylan.core.data.firestore.users.AddOn
import space.narrate.waylan.core.data.firestore.users.AddOnAction
import space.narrate.waylan.core.data.firestore.users.AddOnAction.*
import space.narrate.waylan.core.data.firestore.users.AddOnState
import space.narrate.waylan.core.data.firestore.users.UserAddOnActionUseCase
import space.narrate.waylan.core.data.firestore.users.state
import space.narrate.waylan.core.repo.UserRepository
import space.narrate.waylan.core.ui.common.Event
import space.narrate.waylan.core.ui.common.SnackbarModel
import space.narrate.waylan.core.util.LiveDataUtils
import space.narrate.waylan.core.util.mapOnTransform
import space.narrate.waylan.core.util.mapTransform
import space.narrate.waylan.core.util.switchMapTransform
import space.narrate.waylan.settings.R

/**
 * ViewModel for [AddOnsFragment].
 */
class AddOnsViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    val addOns: LiveData<List<AddOnItemModel>> = AddOnItemListMediatorLiveData().apply {
        AddOn.values().forEach {
            addSource(userRepository.getUserAddOnLive(it))
        }
    }

    private val _currentPosition: MutableLiveData<Int> = MutableLiveData()

    val currentAddOn: LiveData<AddOnItemModel> = addOns.mapOnTransform(_currentPosition) { addOns, position ->
        addOns[position]
    }

    val shouldShowStatusTextLabel: LiveData<Boolean>
        get() = currentAddOn.mapTransform {
            when (it.userAddOn.state) {
                AddOnState.NONE -> false
                else -> true
            }
        }

    private val _shouldLaunchPurchaseFlow: MutableLiveData<Event<PurchaseFlowModel>> =
        MutableLiveData()
    val shouldLaunchPurchaseFlow: LiveData<Event<PurchaseFlowModel>>
        get() = _shouldLaunchPurchaseFlow

    private val _shouldShowSnackbar: MutableLiveData<Event<SnackbarModel>> = MutableLiveData()
    val shouldShowSnackbar: LiveData<Event<SnackbarModel>>
        get() = _shouldShowSnackbar

    fun onCurrentAddOnPageChanged(position: Int) {
        _currentPosition.value = position
    }

    fun onActionClicked(addOnItemModel: AddOnItemModel, action: AddOnAction) {
        when (action) {
            TRY_FOR_FREE -> userRepository.setUserAddOn(
                addOnItemModel.addOn,
                UserAddOnActionUseCase.TryForFree
            )
            ADD,
            RENEW -> _shouldLaunchPurchaseFlow.value = Event(
                PurchaseFlowModel(addOnItemModel.addOn, action)
            )
        }
    }

    fun onBillingEvent(event: BillingEvent) {
        when (event) {
            is BillingEvent.Purchased -> {
                val message = when (event.addOn) {
                    AddOn.MERRIAM_WEBSTER -> R.string.add_on_merriam_webster_successfully_purchased
                    AddOn.MERRIAM_WEBSTER_THESAURUS -> R.string.add_on_merriam_webster_thesaurus_successfully_purchased
                }
                _shouldShowSnackbar.value = Event(SnackbarModel(
                    message,
                    SnackbarModel.LENGTH_SHORT
                ))
            }
        }
    }
}