package com.wordsdict.android.ui.search

import androidx.lifecycle.*
import com.wordsdict.android.data.analytics.AnalyticsRepository
import com.wordsdict.android.data.prefs.Orientation
import com.wordsdict.android.data.prefs.PreferenceRepository
import com.wordsdict.android.data.prefs.UserPreferenceRepository
import com.wordsdict.android.data.prefs.getOrientationPref
import com.wordsdict.android.data.repository.FirestoreUserSource
import com.wordsdict.android.data.repository.WordRepository
import com.wordsdict.android.data.repository.WordSource
import com.wordsdict.android.di.UserScope
import com.wordsdict.android.util.RotationManager
import com.wordsdict.android.util.isLandscapeToPortrait
import com.wordsdict.android.util.isPortraitToLandscape
import javax.inject.Inject

@UserScope
class SearchViewModel @Inject constructor(
        private val wordRepository: WordRepository,
        private val preferenceRepository: PreferenceRepository,
        private val userPreferenceRepository: UserPreferenceRepository,
        private val analyticsRepository: AnalyticsRepository
): ViewModel(), RotationManager.Observer {

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
            wordRepository.lookup(it)
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
        get() = Orientation.valueOf(preferenceRepository.orientationLock)
        set(value) {
            preferenceRepository.orientationLock = value.name
        }


    fun setWordId(id: String) {
        if (wordId.value != id) {
            wordId.value = id
        }
    }

    fun logSearchWordEvent(id: String, word: WordSource) {
        analyticsRepository.logSearchWordEvent(searchInput, id, word::class.java.simpleName)
    }

    override fun onLockedRotate(old: Int, new: Int) {
        println("SearchViewModel::onLockedRotation old: $old, new: $new")
        //TODO prompt to unlock orientation lock
        //TODO possibly make a sequence detector in RotationManager to handle this
    }

    override fun onUnlockedOrientationChange(old: Int, new: Int) {
        println("SearchViewModel::onUnlockedOrientationChange: $old, new: $new")
        if (isPortraitToLandscape(old, new)) {
            userPreferenceRepository.portraitToLandscapeOrientationChangeCount++
            if (userPreferenceRepository.portraitToLandscapeOrientationChangeCount == 2L) {
                orientationPrompt.value = OrientationPrompt.LockToLandscape(getOrientationPref(new))
                orientationPrompt.value = null
            }
        } else if (isLandscapeToPortrait(old, new)) {
            userPreferenceRepository.landscapeToPortraitOrientationChangeCount++
            if (userPreferenceRepository.landscapeToPortraitOrientationChangeCount == 1L) {
                orientationPrompt.value = OrientationPrompt.LockToPortrait(getOrientationPref(new))
                orientationPrompt.value = null
            }
        }
    }


}

