package com.wordsdict.android.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.wordsdict.android.data.repository.*
import com.wordsdict.android.di.UserScope
import javax.inject.Inject


@UserScope
class DetailsViewModel @Inject constructor(
        private val wordRepository: WordRepository
): ViewModel() {

    private var wordId = MutableLiveData<String>()

    val wordPropertiesSource: LiveData<WordPropertiesSource> = Transformations.switchMap(wordId) {
        wordRepository.getWordPropertiesSource(it)
    }

    val wordsetSource: LiveData<WordsetSource?> = Transformations.switchMap(wordId) {
        wordRepository.getWordsetSource(it)
    }

    val firestoreUserSource: LiveData<FirestoreUserSource> = Transformations.switchMap(wordId) {
        wordRepository.getFirestoreUserSource(it)
    }

    val firestoreGlobalSource: LiveData<FirestoreGlobalSource> = Transformations.switchMap(wordId) {
        wordRepository.getFirestoreGlobalSource(it)
    }

    val merriamWebsterSource: LiveData<MerriamWebsterSource> = Transformations.switchMap(wordId) {
        wordRepository.getMerriamWebsterSource(it)
    }
    fun setWordId(id: String) {
        if (wordId.value != id) {
            wordId.value = id
        }
    }

}