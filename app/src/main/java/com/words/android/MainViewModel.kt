package com.words.android

import androidx.lifecycle.*
import com.words.android.data.repository.WordRepository
import com.words.android.data.repository.WordSource
import com.words.android.di.UserScope
import java.util.*
import javax.inject.Inject

@UserScope
class MainViewModel @Inject constructor(private val wordRepository: WordRepository) : ViewModel() {

    private var backStack: MutableLiveData<Stack<Navigator.HomeDestination>> = MutableLiveData()

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

    fun getBackStack(): LiveData<Stack<Navigator.HomeDestination>> {
        return backStack
    }

    fun pushToBackStack(dest: Navigator.HomeDestination) {
        val stack = backStack.value ?: Stack()
        stack.push(dest)
        backStack.value = stack
    }

    fun popBackStack() {
        val stack = backStack.value
        stack?.pop()
        if (stack != null) backStack.value = stack
    }

}
