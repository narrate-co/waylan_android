package com.words.android.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.words.android.data.repository.Word
import com.words.android.data.repository.WordRepository

class SearchViewModel(val wordRepository: WordRepository): ViewModel() {

    var searchInput: String = ""
        set(value) {
            //TODO sanitize input and check for equality with current field value
                field = value
                searchInputData.value = value

        }
    private val searchInputData: MutableLiveData<String> = MutableLiveData()
    val searchResults: LiveData<List<Word>> = Transformations.switchMap(searchInputData) {
        if (it.isEmpty()) {
            wordRepository.getRecents(25L)
        } else {
            wordRepository.filterWords(it)
        }
    }

}

