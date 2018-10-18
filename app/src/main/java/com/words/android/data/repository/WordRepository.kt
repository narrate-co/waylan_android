package com.words.android.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.sqlite.db.SimpleSQLiteQuery
import com.words.android.data.disk.wordset.WordAndMeanings
import com.words.android.data.disk.AppDatabase
import com.words.android.data.disk.mw.WordAndDefinitions
import com.words.android.data.firestore.FirestoreStore
import com.words.android.data.firestore.users.UserWord
import com.words.android.data.firestore.words.GlobalWord
import com.words.android.data.mw.MerriamWebsterStore
import com.words.android.util.LiveDataHelper
import kotlinx.coroutines.experimental.launch

class WordRepository(
        private val db: AppDatabase,
        private val firestoreStore: FirestoreStore?,
        private val merriamWebsterStore: MerriamWebsterStore?
) {

    fun filterWords(query: String): LiveData<List<WordSource>> {
        return Transformations.map(db.wordDao().load("$query%")) { word ->
            word.map { WordSource.SimpleWordSource(it) }
        }
    }

    fun getWordSources(id: String): LiveData<WordSource> {
        val mediatorLiveData = MediatorLiveData<WordSource>()

        // Word Properties
        mediatorLiveData.addSource(getWordProperties(id)) {
            if (it != null) mediatorLiveData.value = WordSource.WordProperties(it)
        }

        // Wordset
        mediatorLiveData.addSource(getWordAndMeanings(id)) {
            if (it != null) mediatorLiveData.value = WordSource.WordsetSource(it)
        }

        // Firestore User Word
        mediatorLiveData.addSource(getUserWord(id)) {
            if (it != null) mediatorLiveData.value = WordSource.FirestoreUserSource(it)
        }

        // Firestore Global Word
        mediatorLiveData.addSource(getGlobalWord(id)) {
            if (it != null) mediatorLiveData.value = WordSource.FirestoreGlobalSource(it)
        }

        mediatorLiveData.addSource(getMerriamWebsterWordAndDefinitions(id)) {
            if (it != null) mediatorLiveData.value = WordSource.MerriamWebsterSource(it)
        }

        return mediatorLiveData
    }

    private fun getWordProperties(word: String): LiveData<WordProperties> {
        val data = MutableLiveData<WordProperties>()
        data.value = WordProperties(word, word)
        return data
    }

    private fun getWordAndMeanings(word: String): LiveData<WordAndMeanings> =
            if (word.isNotBlank()) db.wordDao().getWordAndMeanings(word) else LiveDataHelper.empty()

    private fun getUserWord(id: String): LiveData<UserWord> {
        return if (id.isNotBlank()) firestoreStore?.getUserWordLive(id) ?: LiveDataHelper.empty() else LiveDataHelper.empty()
    }

    private fun getGlobalWord(id: String): LiveData<GlobalWord> {
        return if (id.isNotBlank()) firestoreStore?.getGlobalWordLive(id) ?: LiveDataHelper.empty() else LiveDataHelper.empty()
    }

    private fun getMerriamWebsterWordAndDefinitions(id: String): LiveData<List<WordAndDefinitions>> {
        return if (id.isNotBlank()) merriamWebsterStore?.getWord(id) ?: LiveDataHelper.empty() else LiveDataHelper.empty()
    }

    fun getTrending(limit: Long? = null): LiveData<List<WordSource>> {
        if (firestoreStore == null) return LiveDataHelper.empty()
        return Transformations.map(firestoreStore.getTrending(limit)) { globalWords ->
            globalWords.map { WordSource.FirestoreGlobalSource(it) }
        }
    }

    fun getFavorites(limit: Long? = null): LiveData<List<WordSource>> {
        if (firestoreStore == null) return LiveDataHelper.empty()
        return Transformations.map(firestoreStore.getFavorites(limit)) { userWords ->
            userWords.map { WordSource.FirestoreUserSource(it) }
        }
    }

    fun setFavorite(id: String, favorite: Boolean) {
        if (id.isBlank()) return

        launch {
            firestoreStore?.setFavorite(id, favorite)
        }
    }

    fun getRecents(limit: Long? = null): LiveData<List<WordSource>> {
        if (firestoreStore == null) return LiveDataHelper.empty()
        return Transformations.map(firestoreStore.getRecents(limit)) { userWords ->
            userWords.map { WordSource.FirestoreUserSource(it) }
        }
    }

    fun setRecent(id: String) {
        if (id.isBlank()) return

        launch {
            firestoreStore?.setRecent(id)
        }
    }

}

