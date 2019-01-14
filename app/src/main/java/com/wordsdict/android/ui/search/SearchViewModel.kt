package com.wordsdict.android.ui.search

import androidx.lifecycle.*
import com.wordsdict.android.data.analytics.AnalyticsRepository
import com.wordsdict.android.data.prefs.*
import com.wordsdict.android.data.repository.UserRepository
import com.wordsdict.android.data.repository.WordRepository
import com.wordsdict.android.data.repository.WordSource
import javax.inject.Inject

/**
 * A ViewModel for [SearchFragment]
 */
class SearchViewModel @Inject constructor(
        private val wordRepository: WordRepository,
        private val userRepository: UserRepository,
        private val analyticsRepository: AnalyticsRepository
): ViewModel(), RotationManager.Observer, RotationManager.PatternObserver {

    /**
     * The current text of the [SearchFragment]'s search input field. Set this property when
     * the search input field's EditText changes to have [searchResults] re-query for new
     * results.
     *
     * Client should only need to <i>set</i> this property while getting results from
     * [searchResults]
     */
    var searchInput: String = ""
        set(value) {
            if (value == field) return
            field = value
            searchInputLive.value = value
        }

    // A live data copy of [searchInput] to be observed by [searchResults]
    private val searchInputLive: MutableLiveData<String> = MutableLiveData()

    /**
     * A LiveData object that an appropriate list of results based on the value of [searchInput].
     * When [searchInput], this LiveData's value will be updated to reflect either search results,
     * if [searchInput] is not blank, or a list of recently viewed words if it is.
     */
    val searchResults: LiveData<List<WordSource>> = Transformations.switchMap(searchInputLive) {
        if (it.isEmpty()) {
            wordRepository.getRecents(25L) as LiveData<List<WordSource>>
        } else {
            wordRepository.getSearchWords(it)
        }
    }

    // A mutable live data backing object to be set when an orientation prompt should be shown
    // TODO create a SingularLiveData class to automatically clear a value after emitting a value
    // TODO something like RxJava's Single
    private val _orientationPrompt: MutableLiveData<OrientationPrompt?> = MutableLiveData()

    /**
     * A LiveData object that broadcasts [OrientationPrompt]s when prompts should be immediately
     * shown. After an [OrientationPrompt] is set as the value, the the value immediately return
     * to null. This is becuase orientation prompts are extremely "timely" events and should only
     * be acted on the instant they are seen. The value will return to null to avoid observers
     * resubscribing and being passed the last value seen, which would then be an out-dated (no
     * longer timely prompt)
     */
    val orientationPrompt: LiveData<OrientationPrompt?> = _orientationPrompt

    // initialize properties to defaults
    init {
        searchInputLive.value = ""
    }

    /**
     * Set the users [Orientation] preference. This preferenced is watched by the app, which will
     * send a broadcast to be received by all relevant components (active Activities) which will
     * handle calling the appropriate Activity methods.
     *
     * @param orientation The orientation the app should be set to
     */
    fun setOrientationPreference(orientation: Orientation) {
        userRepository.orientationLock = orientation.value
    }

    /**
     * Log a user having searched for a word and clicked on a result.
     *
     * @see [AnalyticsRepository.EVENT_SEARCH_WORD] for more details
     */
    fun logSearchWordEvent(id: String, word: WordSource) {
        analyticsRepository.logSearchWordEvent(searchInput, id, word::class.java.simpleName)
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
        if (isPortraitToLandscape(old, new)) {
            userRepository.portraitToLandscapeOrientationChangeCount++
            if (userRepository.portraitToLandscapeOrientationChangeCount == 2L) {
                _orientationPrompt.value = OrientationPrompt.LockToLandscape(
                        Orientation.fromActivityInfoScreenOrientation(new.orientation)
                )
                _orientationPrompt.value = null
            }
        } else if (isLandscapeToPortrait(old, new)) {
            userRepository.landscapeToPortraitOrientationChangeCount++
            if (userRepository.landscapeToPortraitOrientationChangeCount == 1L) {
                _orientationPrompt.value = OrientationPrompt.LockToPortrait(
                        Orientation.fromActivityInfoScreenOrientation(new.orientation)
                )
                _orientationPrompt.value = null
            }
        }
    }

    override fun onLockedRotatePatternSeen(
            pattern: List<RotationManager.RotationEvent>,
            lockedTo: Int,
            observedSince: Long
    ) {
        if (isLikelyUnlockDesiredScenario(pattern, lockedTo, observedSince, System.nanoTime())) {
            //should suggest unlocking
            _orientationPrompt.value = OrientationPrompt.UnlockOrientation(Orientation.UNSPECIFIED)
            _orientationPrompt.value = null
        }
    }

    override fun onUnlockedOrientationPatternSeen(pattern: List<RotationManager.RotationEvent>) {
        //do nothing. maybe move to pattern matching over orientation change counts
    }


}

