package space.narrate.words.android.ui.search

import androidx.lifecycle.*
import space.narrate.words.android.R
import space.narrate.words.android.data.analytics.AnalyticsRepository
import space.narrate.words.android.data.firestore.users.UserWord
import space.narrate.words.android.data.repository.UserRepository
import space.narrate.words.android.data.repository.WordRepository
import space.narrate.words.android.data.prefs.*
import space.narrate.words.android.ui.Event
import space.narrate.words.android.util.mapTransform
import space.narrate.words.android.util.switchMapTransform
import space.narrate.words.android.util.widget.MergedLiveData
import javax.inject.Inject

/**
 * A ViewModel for [SearchFragment]
 */
class SearchViewModel @Inject constructor(
        private val wordRepository: WordRepository,
        private val userRepository: UserRepository,
        private val analyticsRepository: AnalyticsRepository
): ViewModel(), RotationManager.Observer, RotationManager.PatternObserver {

    private val searchInput: MutableLiveData<String> = MutableLiveData()

    val searchResults: LiveData<List<SearchItemModel>> = searchInput
        .switchMapTransform { if (it.isEmpty()) getRecent() else getSearch(it) }
        .mapTransform { if (it.isEmpty()) addHeader(it) else it }

    private val _shouldShowDetails: MutableLiveData<Event<String>> = MutableLiveData()
    val shouldShowDetails: LiveData<Event<String>>
        get() = _shouldShowDetails

    private val _shouldShowOrientationPrompt: MutableLiveData<Event<OrientationPrompt>>
        = MutableLiveData()
    val shouldShowOrientationPrompt: LiveData<Event<OrientationPrompt>>
        get() = _shouldShowOrientationPrompt

    init {
        searchInput.value = ""
    }

    private fun getRecent(): LiveData<List<SearchItemModel>> {
        return wordRepository.getUserWordRecents(25L)
            .mapTransform { recents -> recents.map { SearchItemModel.UserWordModel(it) } }
    }

    private fun getSearch(input: String): LiveData<List<SearchItemModel>> {
        return MergedLiveData(wordRepository.getWordsetWords(input), wordRepository.getSuggestItems(input)) { words, suggestions ->
            (words.map { SearchItemModel.WordModel(it) } + suggestions.map { SearchItemModel.SuggestModel(it) }).distinctBy { item ->
                when (item) {
                    is SearchItemModel.WordModel -> item.word.word
                    is SearchItemModel.SuggestModel -> item.suggestItem.term
                    else -> ""
                }
            }
        }
    }

    private fun addHeader(list: List<SearchItemModel>): List<SearchItemModel> {
        return if (list.isEmpty()) {
            list.toMutableList().apply {
                add(0, SearchItemModel.HeaderModel(
                    R.string.search_banner_body
                ))
            }
        } else {
            list
        }
    }

    fun onSearchInputTextChanged(input: CharSequence?) {
        searchInput.value = input?.toString() ?: ""
    }

    fun onWordClicked(item: SearchItemModel) {
        val word = when (item) {
            is SearchItemModel.WordModel -> item.word.word
            is SearchItemModel.UserWordModel -> item.userWord.word
            is SearchItemModel.SuggestModel -> item.suggestItem.term
            else -> return
        }

        analyticsRepository.logSearchWordEvent(word, word, item::class.java.simpleName)
        _shouldShowDetails.value = Event(word)
    }

    /**
     * Set the users [Orientation] preference. This preferenced is watched by the app, which will
     * send a broadcast to be received by all relevant components (active Activities) which will
     * handle calling the appropriate Activity methods.
     *
     * @param orientation The orientation the app should be set to
     */
    fun onOrientationPromptClicked(orientationPrompt: OrientationPrompt) {
        userRepository.orientationLock = orientationPrompt.orientationToRequest
    }


    // RotationManager callbacks

    override fun onLockedRotate(
            old: RotationManager.RotationEvent,
            new: RotationManager.RotationEvent,
            lockedTo: Int
    ) {
        //do nothing
    }

    override fun onUnlockedOrientationChange(
            old: RotationManager.RotationEvent,
            new: RotationManager.RotationEvent
    ) {
        if (RotationUtils.isPortraitToLandscape(old, new)) {
            userRepository.portraitToLandscapeOrientationChangeCount++
            if (userRepository.portraitToLandscapeOrientationChangeCount == 2L) {
                _shouldShowOrientationPrompt.value = Event(OrientationPrompt.LockToLandscape(
                    Orientation.fromActivityInfoScreenOrientation(new.orientation)
                ))
            }
        } else if (RotationUtils.isLandscapeToPortrait(old, new)) {
            userRepository.landscapeToPortraitOrientationChangeCount++
            if (userRepository.landscapeToPortraitOrientationChangeCount == 1L) {
                _shouldShowOrientationPrompt.value = Event(OrientationPrompt.LockToPortrait(
                    Orientation.fromActivityInfoScreenOrientation(new.orientation)
                ))
            }
        }
    }

    override fun onLockedRotatePatternSeen(
            pattern: List<RotationManager.RotationEvent>,
            lockedTo: Int,
            observedSince: Long
    ) {
        if (RotationUtils.isLikelyUnlockDesiredScenario(
                        pattern,
                        lockedTo,
                        observedSince,
                        System.nanoTime()
                )) {
            //should suggest unlocking
            _shouldShowOrientationPrompt.value = Event(OrientationPrompt.UnlockOrientation(
                Orientation.UNSPECIFIED
            ))
        }
    }

    override fun onUnlockedOrientationPatternSeen(pattern: List<RotationManager.RotationEvent>) {
        //do nothing. maybe move to pattern matching over orientation change counts
    }


}

