package com.words.android.ui.search

import androidx.lifecycle.*
import com.words.android.data.repository.FirestoreUserSource
import com.words.android.data.repository.WordRepository
import com.words.android.data.repository.WordSource
import com.words.android.data.spell.SuggestItem
import com.words.android.di.UserScope
import com.words.android.util.LiveDataHelper
import javax.inject.Inject

@UserScope
class SearchViewModel @Inject constructor(
        private val wordRepository: WordRepository
): ViewModel() {

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

    fun setWordId(id: String) {
        if (wordId.value != id) {
            wordId.value = id
        }
    }

}

