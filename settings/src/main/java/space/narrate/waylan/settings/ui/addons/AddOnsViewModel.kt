package space.narrate.waylan.settings.ui.addons

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import space.narrate.waylan.core.data.firestore.users.PluginState
import space.narrate.waylan.core.repo.UserRepository
import space.narrate.waylan.core.util.mapTransform

class AddOnsViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _currentAddOn: MutableLiveData<AddOnItemModel> = MutableLiveData()
    val currentAddOn: LiveData<AddOnItemModel>
        get() = _currentAddOn

    val addOns: LiveData<List<AddOnItemModel>> = userRepository.user.mapTransform { user ->
        listOf(
            AddOnItemModel.MerriamWebster(user),
            AddOnItemModel.MerriamWebsterThesaurus(user)
        )
    }

    fun onCurrentAddOnPageChanged(addOn: AddOnItemModel) {
        _currentAddOn.value = addOn
    }

    fun onActionClicked(addOn: AddOnItemModel, action: PluginState.Action) {
        // TODO: Initiate purchase flow
    }
}