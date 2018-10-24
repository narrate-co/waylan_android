package com.words.android

import androidx.lifecycle.*
import com.words.android.data.repository.WordRepository
import com.words.android.data.repository.WordSource
import com.words.android.di.UserScope
import com.words.android.ui.list.ListFragment
import javax.inject.Inject

@UserScope
class MainViewModel @Inject constructor(private val wordRepository: WordRepository) : ViewModel() {

    private var currentWordId = MutableLiveData<String>()
    val currentSources: LiveData<WordSource> = Transformations.switchMap(currentWordId) {
        wordRepository.getWordSources(it)
    }

    fun setCurrentWordId(id: String) {
        currentWordId.value = id
    }

    fun setCurrentWordFavorited(favorite: Boolean) {
        val id = currentWordId.value ?: return
        wordRepository.setFavorite(id, favorite)
    }

    fun setCurrentWordRecented() {
        val id = currentWordId.value ?: return
        wordRepository.setRecent(id)
    }


}
