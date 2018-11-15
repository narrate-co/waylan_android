package com.words.android.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.words.android.data.repository.WordRepository
import com.words.android.data.repository.WordSource
import com.words.android.di.UserScope
import javax.inject.Inject


@UserScope
class DetailsViewModel @Inject constructor(
        private val wordRepository: WordRepository
): ViewModel() {

    private var wordId = MutableLiveData<String>()

    val wordPropertiesSource: LiveData<WordSource.WordPropertiesSource> = Transformations.switchMap(wordId) {
        wordRepository.getWordPropertiesSource(it)
    }

    val wordsetSource: LiveData<WordSource.WordsetSource> = Transformations.switchMap(wordId) {
        wordRepository.getWordsetSource(it)
    }

    val firestoreUserSource: LiveData<WordSource.FirestoreUserSource> = Transformations.switchMap(wordId) {
        wordRepository.getFirestoreUserSource(it)
    }

    val firestoreGlobalSource: LiveData<WordSource.FirestoreGlobalSource> = Transformations.switchMap(wordId) {
        wordRepository.getFirestoreGlobalSource(it)
    }

    val merriamWebsterSource: LiveData<WordSource.MerriamWebsterSource> = Transformations.switchMap(wordId) {
        wordRepository.getMerriamWebsterSource(it)
    }
    fun setWordId(id: String) {
        if (wordId.value != id) {
            wordId.value = id
        }
    }

}