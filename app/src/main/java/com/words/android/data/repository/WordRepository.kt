package com.words.android.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.words.android.data.disk.wordset.WordAndMeanings
import com.words.android.data.disk.AppDatabase
import com.words.android.data.disk.mw.WordAndDefinitions
import com.words.android.data.firestore.FirestoreStore
import com.words.android.data.firestore.UserWord
import com.words.android.data.mw.MerriamWebsterStore
import com.words.android.util.LiveDataHelper
import kotlinx.coroutines.experimental.launch

class WordRepository(
        private val db: AppDatabase,
        private val firestoreStore: FirestoreStore?,
        private val merriamWebsterStore: MerriamWebsterStore?
) {

    fun filterWords(query: String): LiveData<List<Word>> {
        val mediatorLiveData = MediatorLiveData<List<Word>>()
        mediatorLiveData.addSource(db.wordDao().load("$query%")) {
            val words = it?.map { Word().apply { dbWord = it } }
            mediatorLiveData.value = words
        }
        return mediatorLiveData
    }

    fun getWord(id: String): LiveData<Word>  {
        println("WordREpo - getWord $id")
        val mediatorLiveData = MediatorLiveData<Word>()
        mediatorLiveData.addSource(getWordAndMeanings(id)) {
            println("WordRepo - mediatorLiveData word&Meaning = ${it?.word?.word}")
            val word = mediatorLiveData.value ?: Word()
            word.dbWord = it?.word
            word.dbMeanings = it?.meanings ?: emptyList()
            mediatorLiveData.value = word
        }
        mediatorLiveData.addSource(getUserWord(id)) {
            println("WordRepo - mediatorLiveData getUserWord = ${it?.id}")
            val word = mediatorLiveData.value ?: Word()
            word.userWord = it
            mediatorLiveData.value = word
        }
        mediatorLiveData.addSource(getMerriamWebsterWordAndDefinitions(id)) {
            println("WordRepo - mediatorLiveData merriamWebsterWord = ${it?.word?.word}")
            val word = mediatorLiveData.value ?: Word()
            word.mwWord = it?.word
            word.mwDefinitions = it?.definitions ?: emptyList()
            mediatorLiveData.value = word
        }

        return mediatorLiveData
    }

    private fun getWordAndMeanings(word: String): LiveData<WordAndMeanings> =
            db.wordDao().getWordAndMeanings(word)

    private fun getUserWord(id: String): LiveData<UserWord> {
        return firestoreStore?.getUserWordLive(id) ?: LiveDataHelper.empty()
    }

    private fun getMerriamWebsterWordAndDefinitions(id: String): LiveData<WordAndDefinitions> {
        //TODO save mw words to local db!
        return merriamWebsterStore?.getWord(id) ?: LiveDataHelper.empty()
    }

    fun getFavorites(): LiveData<List<Word>> {
        if (firestoreStore == null) return LiveDataHelper.empty()

        val mediatorLiveData = MediatorLiveData<List<Word>>()
        mediatorLiveData.addSource(firestoreStore.getFavorites()) {
            mediatorLiveData.value = it?.map { Word().apply { userWord = it } } ?: emptyList()
        }

        return mediatorLiveData
    }

    fun setFavorite(id: String, favorite: Boolean) {
        launch {
            firestoreStore?.setFavorite(id, favorite)
        }
    }

    fun getRecents(): LiveData<List<Word>> {
        if (firestoreStore == null) return LiveDataHelper.empty()

        val mediatorLiveData = MediatorLiveData<List<Word>>()
        mediatorLiveData.addSource(firestoreStore.getRecents()) {
            mediatorLiveData.value = it?.map { Word().apply { userWord = it } } ?: emptyList()
        }

        return mediatorLiveData
    }

    fun setRecent(id: String) {
        launch {
            firestoreStore?.setRecent(id)
        }
    }

}

