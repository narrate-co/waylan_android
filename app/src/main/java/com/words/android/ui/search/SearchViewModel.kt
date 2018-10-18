package com.words.android.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.words.android.data.repository.WordRepository
import com.words.android.data.repository.WordSource
import com.words.android.di.UserScope
import javax.inject.Inject

@UserScope
class SearchViewModel @Inject constructor(private val wordRepository: WordRepository): ViewModel() {

    var searchInput: String = ""
        set(value) {
            if (value == field) return
            field = value
            searchInputData.value = value

        }
    val searchInputData: MutableLiveData<String> = MutableLiveData()
    val searchResults: LiveData<List<WordSource>> = Transformations.switchMap(searchInputData) {
        if (it.isEmpty()) {
            wordRepository.getRecents(25L)
        } else {
            wordRepository.filterWords(it)
        }
    }

    init {
        searchInputData.value = ""
    }

}

