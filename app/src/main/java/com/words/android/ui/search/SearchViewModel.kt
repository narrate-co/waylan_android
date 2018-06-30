package com.words.android.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

    val searchInputData: MutableLiveData<String> = MutableLiveData()

    fun getSearchResults(query: String): LiveData<List<Word>> {
        return wordRepository.filterWords(query)
    }
}

