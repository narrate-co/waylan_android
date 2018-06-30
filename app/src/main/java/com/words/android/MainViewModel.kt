package com.words.android

import androidx.lifecycle.*
import com.words.android.data.repository.Word
import com.words.android.data.repository.WordRepository

class MainViewModel(private val wordRepository: WordRepository) : ViewModel() {

    private var currentWordId = MutableLiveData<String>()
    val currentWord: LiveData<Word> = Transformations.switchMap(currentWordId) {
        wordRepository.getRepoWord(it)
    }

    fun setCurrentWordId(id: String) {
        currentWordId.value = id
    }

    fun setCurrentWordFavorited(favorite: Boolean) {
        val id = currentWordId?.value ?: return

        wordRepository.setFavorite(id, favorite)
    }


}
