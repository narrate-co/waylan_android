package com.words.android

import androidx.lifecycle.*
import com.words.android.data.repository.Word
import com.words.android.data.repository.WordRepository
import com.words.android.ui.list.ListFragment

class MainViewModel(private val wordRepository: WordRepository) : ViewModel() {

    private var currentWordId = MutableLiveData<String>()
    val currentWord: LiveData<Word> = Transformations.switchMap(currentWordId) {
        wordRepository.getWord(it)
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

    fun getList(type: ListFragment.ListType): LiveData<List<Word>> {
        return when (type) {
            ListFragment.ListType.TRENDING -> wordRepository.getFavorites()
            ListFragment.ListType.RECENT -> wordRepository.getRecents()
            ListFragment.ListType.FAVORITE -> wordRepository.getFavorites()
        }
    }


}
