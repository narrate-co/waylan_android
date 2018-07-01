package com.words.android.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.words.android.data.repository.Word
import com.words.android.data.repository.WordRepository

class DashboardViewModel(
        private val wordRepository: WordRepository
): ViewModel() {

    val recentWords: LiveData<List<Word>> = wordRepository.getRecents()
    val favoriteWord: LiveData<List<Word>> = wordRepository.getFavorites()

}

