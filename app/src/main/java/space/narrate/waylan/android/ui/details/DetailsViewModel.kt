package space.narrate.waylan.android.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import space.narrate.waylan.core.data.firestore.users.AddOn
import space.narrate.waylan.core.data.firestore.users.AddOnState
import space.narrate.waylan.core.data.firestore.users.isValid
import space.narrate.waylan.core.data.firestore.users.state
import space.narrate.waylan.core.details.DetailDataProviderRegistry
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.core.details.DetailItemType
import space.narrate.waylan.core.repo.UserRepository
import space.narrate.waylan.core.ui.common.Event
import space.narrate.waylan.core.ui.common.SnackbarModel
import space.narrate.waylan.core.util.mapOnTransform
import space.narrate.waylan.core.util.switchMapTransform

/**
 * ViewModel for [DetailsFragment]
 */
class DetailsViewModel(
    private val detailDataProviderRegistry: DetailDataProviderRegistry,
    private val userRepository: UserRepository
): ViewModel() {

    // The current word being displayed (as it appears in the dictionary)
    private var _word = MutableLiveData<String>()

    val list: LiveData<List<DetailItemModel>> = _word
        .switchMapTransform { word ->
            DetailItemListMediatorLiveData().apply {
                detailDataProviderRegistry.providers.forEach {
                    addSource(it.loadWord(word))
                }
            }
        }
        .mapOnTransform(userRepository.getUserAddOnsLive()) { list, addOns ->
            val filteredList = list.toMutableList()
            addOns.forEach { addOn ->
                if (addOn.state == AddOnState.NONE ||
                    (!addOn.isValid && addOn.isAwareOfExpiration)) {
                    filteredList.removeAll {
                        // Map UserAddOn to a DetailItemType
                        val type = when (AddOn.fromId(addOn.id)) {
                            AddOn.MERRIAM_WEBSTER ->
                                DetailItemType.MERRIAM_WEBSTER
                            AddOn.MERRIAM_WEBSTER_THESAURUS ->
                                DetailItemType.MERRIAM_WEBSTER_THESAURUS
                            AddOn.AMERICAN_HERITAGE ->
                                DetailItemType.AMERICAN_HERITAGE
                        }
                        it.itemType == type
                    }
                }
            }
            filteredList
        }

    private val _shouldShowDragDismissOverlay: MutableLiveData<Event<Boolean>> = MutableLiveData()
    val shouldShowDragDismissOverlay: LiveData<Event<Boolean>>
        get() = _shouldShowDragDismissOverlay

    private val _audioClipAction: MutableLiveData<Event<AudioClipAction>> = MutableLiveData()
    val audioClipAction: LiveData<Event<AudioClipAction>>
        get() = _audioClipAction

    private val _shouldShowSnackbar: MutableLiveData<Event<SnackbarModel>> = MutableLiveData()
    val shouldShowSnackbar: LiveData<Event<SnackbarModel>>
        get() = _shouldShowSnackbar

    init {
        if (!userRepository.hasSeenDragDismissOverlay) {
            _shouldShowDragDismissOverlay.value = Event(true)
            userRepository.hasSeenDragDismissOverlay = true
        }
    }

    fun onCurrentWordChanged(word: String) {
        if (_word.value != word) {
            _word.value = word
        }
    }

    fun onAddOnDismissClicked(addOn: AddOn) {
        userRepository.updateUserAddOnWith(addOn) {
            isAwareOfExpiration = true
        }
    }

    fun onPlayAudioClicked(url: String?) {
        if (url == null) return
        _audioClipAction.value = Event(AudioClipAction.Play(url))
    }

    fun onStopAudioClicked() {
        _audioClipAction.value = Event(AudioClipAction.Stop)
    }

    fun onAudioClipError(messageRes: Int) {
        _shouldShowSnackbar.value = Event(SnackbarModel(
            messageRes,
            SnackbarModel.LENGTH_SHORT,
            true
        ))
    }
}