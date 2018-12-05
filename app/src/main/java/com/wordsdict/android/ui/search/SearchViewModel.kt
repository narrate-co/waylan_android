package com.wordsdict.android.ui.search

import androidx.lifecycle.*
import com.wordsdict.android.data.analytics.AnalyticsRepository
import com.wordsdict.android.data.prefs.*
import com.wordsdict.android.data.repository.FirestoreUserSource
import com.wordsdict.android.data.repository.WordRepository
import com.wordsdict.android.data.repository.WordSource
import com.wordsdict.android.di.UserScope
import javax.inject.Inject

@UserScope
class SearchViewModel @Inject constructor(
        private val wordRepository: WordRepository,
        private val userPreferenceStore: UserPreferenceStore,
        private val analyticsRepository: AnalyticsRepository
): ViewModel(), RotationManager.Observer, RotationManager.PatternObserver {

    var searchInput: String = ""
        set(value) {
            if (value == field) return
            field = value
            searchInputData.value = value
        }

    private val searchInputData: MutableLiveData<String> = MutableLiveData()
    val searchResults: LiveData<List<WordSource>> = Transformations.switchMap(searchInputData) {
        if (it.isEmpty()) {
            wordRepository.getRecents(25L)
        } else {
            wordRepository.getSearchWords(it)
        }
    }

    init {
        searchInputData.value = ""
    }

    private val wordId = MutableLiveData<String>()

    val firestoreUserSource: LiveData<FirestoreUserSource> = Transformations.switchMap(wordId) {
        wordRepository.getFirestoreUserSource(it)
    }

    private val orientationPrompt: MutableLiveData<OrientationPrompt?> = MutableLiveData()

    fun getOrientationPrompt(): LiveData<OrientationPrompt?> = orientationPrompt

    var orientation: Orientation
        get() = Orientation.fromActivityInfoScreenOrientation(userPreferenceStore.orientationLock)
        set(value) {
            userPreferenceStore.orientationLock = value.value
        }


    fun setWordId(id: String) {
        if (wordId.value != id) {
            wordId.value = id
        }
    }

    fun logSearchWordEvent(id: String, word: WordSource) {
        analyticsRepository.logSearchWordEvent(searchInput, id, word::class.java.simpleName)
    }

    override fun onLockedRotate(old: RotationManager.RotationEvent, new: RotationManager.RotationEvent, lockedTo: Int) {
        //do nothing
    }

    override fun onUnlockedOrientationChange(old: RotationManager.RotationEvent, new: RotationManager.RotationEvent) {
        if (isPortraitToLandscape(old, new)) {
            userPreferenceStore.portraitToLandscapeOrientationChangeCount++
            if (userPreferenceStore.portraitToLandscapeOrientationChangeCount == 2L) {
                orientationPrompt.value = OrientationPrompt.LockToLandscape(Orientation.fromActivityInfoScreenOrientation(new.orientation))
                orientationPrompt.value = null
            }
        } else if (isLandscapeToPortrait(old, new)) {
            userPreferenceStore.landscapeToPortraitOrientationChangeCount++
            if (userPreferenceStore.landscapeToPortraitOrientationChangeCount == 1L) {
                orientationPrompt.value = OrientationPrompt.LockToPortrait(Orientation.fromActivityInfoScreenOrientation(new.orientation))
                orientationPrompt.value = null
            }
        }
    }

    override fun onLockedRotatePatternSeen(pattern: List<RotationManager.RotationEvent>, lockedTo: Int, observedSince: Long) {
        if (isLikelyUnlockDesiredScenario(pattern, lockedTo, observedSince, System.nanoTime())) {
            //should suggest unlocking
            orientationPrompt.value = OrientationPrompt.UnlockOrientation(Orientation.UNSPECIFIED)
            orientationPrompt.value = null
        }
    }

    override fun onUnlockedOrientationPatternSeen(pattern: List<RotationManager.RotationEvent>) {
        //do nothing. maybe move to pattern matching over orientation change counts
    }


}

