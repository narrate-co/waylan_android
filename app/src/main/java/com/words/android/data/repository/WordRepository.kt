package com.words.android.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
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

        // Wordset
        mediatorLiveData.addSource(getWordAndMeanings(id)) {
            mediatorLiveData.value = WordSource.WordsetSource(it)
        }

        // Firestore User Word
        mediatorLiveData.addSource(getUserWord(id)) {
            mediatorLiveData.value = WordSource.FirestoreUserSource(it)
        }

        // Firestore Global Word
        mediatorLiveData.addSource(getGlobalWord(id)) {
            mediatorLiveData.value = WordSource.FirestoreGlobalSource(it)
        }

        mediatorLiveData.addSource(getMerriamWebsterWordAndDefinitions(id)) {
            mediatorLiveData.value = WordSource.MerriamWebsterSource(it)
        }

        return mediatorLiveData
    }

    private fun getWordAndMeanings(word: String): LiveData<WordAndMeanings> =
            db.wordDao().getWordAndMeanings(word)

    private fun getUserWord(id: String): LiveData<UserWord> {
        return firestoreStore?.getUserWordLive(id) ?: LiveDataHelper.empty()
    }

    private fun getGlobalWord(id: String): LiveData<GlobalWord> {
        return firestoreStore?.getGlobalWordLive(id) ?: LiveDataHelper.empty()
    }

    private fun getMerriamWebsterWordAndDefinitions(id: String): LiveData<List<WordAndDefinitions>> {
        return merriamWebsterStore?.getWord(id) ?: LiveDataHelper.empty()
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
        launch {
            firestoreStore?.setRecent(id)
        }
    }

}

