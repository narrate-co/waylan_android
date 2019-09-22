package space.narrate.waylan.android.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import space.narrate.waylan.android.data.repository.*
import space.narrate.waylan.core.ui.common.Event
import space.narrate.waylan.core.ui.common.SnackbarModel
import space.narrate.waylan.android.util.mapOnTransform
import space.narrate.waylan.android.util.mapTransform
import space.narrate.waylan.android.util.notNullTransform
import space.narrate.waylan.android.util.switchMapTransform
import space.narrate.waylan.android.util.MergedLiveData

/**
 * ViewModel for [DetailsFragment]
 */
class DetailsViewModel(
        private val wordRepository: WordRepository,
        private val userRepository: UserRepository
): ViewModel() {

    // The current word being displayed (as it appears in the dictionary)
    private var _word = MutableLiveData<String>()

    val list: LiveData<List<DetailItemModel>> = _word
        .switchMapTransform { word ->
            DetailItemListMediatorLiveData().apply {

                addSource(wordRepository.getWordsetWord(word).mapTransform {
                    DetailItemModel.TitleModel(it?.word ?: word)
                })

                addSource(wordRepository.getWordsetWordAndMeanings(word)
                    .notNullTransform()
                    .mapTransform {
                        DetailItemModel.WordsetModel(it)
                    })

                addSource(wordRepository.getWordsetWordAndMeanings(word)
                    .notNullTransform()
                    .mapTransform {
                        DetailItemModel.ExamplesModel(it.meanings.map { m -> m.examples }.flatten())
                    })

                addSource(MergedLiveData(
                    wordRepository.getMerriamWebsterWord(word),
                    userRepository.user
                ) { mw, user ->
                    DetailItemModel.MerriamWebsterModel(mw, user)
                })
            }
        }
        .mapOnTransform(userRepository.hasSeenMerriamWebsterPermissionPaneLive) { list, hasSeen ->
            if (hasSeen) {
                list.toMutableList().apply {
                    removeAll { it is DetailItemModel.MerriamWebsterModel }
                }
            } else {
                list
            }
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

    fun onMerriamWebsterPermissionPaneDismissClicked() {
        userRepository.hasSeenMerriamWebsterPermissionPane = true
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