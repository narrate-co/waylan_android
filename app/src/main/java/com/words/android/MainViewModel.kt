package com.words.android

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.words.android.data.Meaning
import com.words.android.data.Word
import com.words.android.data.WordAndMeanings
import com.words.android.data.repository.WordRepository
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

class MainViewModel(private val wordRepository: WordRepository) : ViewModel() {

    private var currentWord = MutableLiveData<Word>()
    private var currentMeaning = MutableLiveData<List<Meaning>>()


    fun getCurrentWord(): LiveData<Word> = currentWord
    fun setCurrentWord(value: String) {
        launch (UI) {
            val word = wordRepository.getWord(value).await()
            setCurrentWord(word)
        }
    }
    fun setCurrentWord(word: Word?) {
        if (word == null) return
        currentWord.value = word
        launch (UI) {
            val meanings = wordRepository.getMeanings(word.word).await()
            println("meanings:: $meanings")
            currentMeaning.value = meanings
        }
    }

    fun getCurrentMeanings(): LiveData<List<Meaning>>
            = currentMeaning

    fun getCurrentWordAndMeanings(): LiveData<WordAndMeanings>
            = wordRepository.getWordAndMeanings(currentWord.value?.word ?: "")


}
