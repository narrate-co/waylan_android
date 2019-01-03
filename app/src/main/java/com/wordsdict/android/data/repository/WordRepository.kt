package com.wordsdict.android.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.wordsdict.android.data.disk.wordset.WordAndMeanings
import com.wordsdict.android.data.disk.AppDatabase
import com.wordsdict.android.data.disk.mw.PermissiveWordsDefinitions
import com.wordsdict.android.data.firestore.FirestoreStore
import com.wordsdict.android.data.firestore.users.UserWord
import com.wordsdict.android.data.firestore.words.GlobalWord
import com.wordsdict.android.data.mw.MerriamWebsterStore
import com.wordsdict.android.data.spell.SymSpellStore
import com.wordsdict.android.ui.search.Period
import com.wordsdict.android.util.LiveDataHelper
import com.wordsdict.android.util.widget.MergedLiveData
import kotlinx.coroutines.launch

/**
 * A repository for all data access to all underlying dictionaries, including from WordSet,
 * Merriam-Webster, SymSpell, Firestore [UserWord]s and Firestore [GlobalWord]s. Clients should
 * access word-related data through [WordRepository].
 */
class WordRepository(
        private val db: AppDatabase,
        private val firestoreStore: FirestoreStore?,
        private val merriamWebsterStore: MerriamWebsterStore?,
        private val symSpellStore: SymSpellStore
) {

    /**
     * Query for a simple list of [SimpleWordSource] and [SuggestSource] that possibly match
     * the given [input]. This should be used to return a list of available and suggested
     * words while a user is actively typing a search.
     *
     * @param input The partial, misspelled or full target word (as it might appear
     *      in the dictionary)
     * @return A LiveData List that contains a mix of [SimpleWordSource] and [SuggestSource]
     *      items matching the given [input]. If [input] is not in the WordSet db or SymSpell's
     *      corpus, an empty List will be returned.
     */
    fun getSearchWords(input: String): LiveData<List<WordSource>> {
        val wordsetSource = Transformations.map(db.wordDao().load("$input%")) { words ->
            words.map { SimpleWordSource(it) }
        }

        val suggestionsSource = Transformations.map(symSpellStore.lookupLive(input)) { list ->
            list.map { SuggestSource(it) }
        }

        return MergedLiveData(wordsetSource, suggestionsSource) { d1, d2 ->
            //TODO deduplicate/sort smartly. Make WordSource comparable?
            (d1 + d2).distinctBy {
                val t = if (it is SimpleWordSource) it.word.word else if (it is SuggestSource) it.item.term else ""
                t
            }
        }
    }

    /**
     * Immediately returns a [WordPropertiesSource] containing the value of [word].
     *
     * This is useful for API consistency and can be used if a UI element only needs the word
     * (as it appears in the dictionary) in order to set UI elements.
     *
     * @see [DetailsComponentViewHolder.TitleComponentViewHolder]
     */
    fun getWordPropertiesSource(word: String): LiveData<WordPropertiesSource> {
        val data = MutableLiveData<WordPropertiesSource>()
        data.value = WordPropertiesSource(word, word)
        return data
    }

    /**
     * Query for a [WordsetSource] where [WordsetSource.word] exactly matches to [word].
     * results for exact matches
     *
     * @param word The exact word (as it appears in the dictionary) to query for.
     */
    fun getWordsetSource(word: String): LiveData<WordsetSource?> {
        val wordAndMeaning = if (word.isNotBlank()) db.wordDao().getWordAndMeanings(word) else LiveDataHelper.empty()

        return Transformations.map(wordAndMeaning) {
            if (it != null) WordsetSource(it) else null
        }
    }

    /**
     * Query for a Firestore [UserWord] where [UserWord.id] exactly matches [id].
     *
     * @param id The document id of the Firestore [UserWord]. Document id's are the same as their
     *      word property value (the word as the it appears in the dictionary). ie, A [UserWord]
     *      with [UserWord.word] equal to <i>quiescent</i> will have the id <i>quiescent</i>
     */
   fun getFirestoreUserSource(id: String): LiveData<FirestoreUserSource> {
       val userWord = if (id.isNotBlank()) firestoreStore?.getUserWordLive(id) ?: LiveDataHelper.empty() else LiveDataHelper.empty()
        return Transformations.map(userWord) {
            FirestoreUserSource(it)
        }
    }

    /**
     * Query for a Firestore [GlobalWord] where [GlobalWord.id] exactly matches [id].
     *
     * @param id The document id of the Firestore [GlobalWord]. Document id's are the same as their
     *      word property value (the word as the it appears in the dictionary). ie, A [GlobalWord]
     *      with [GlobalWord.word] equal to <i>quiescent</i> will have the id <i>quiescent</i>
     */
    fun getFirestoreGlobalSource(id: String): LiveData<FirestoreGlobalSource> {
        val globalWord = if (id.isNotBlank()) firestoreStore?.getGlobalWordLive(id) ?: LiveDataHelper.empty() else LiveDataHelper.empty()
        return Transformations.map(globalWord) {
            FirestoreGlobalSource(it)
        }
    }

    /**
     * Query for a [MerriamWebsterSource] where [com.wordsdict.android.data.disk.mw.Word.word]
     * exactly matches [word].
     *
     * @param word The word (as it appears in the dictionary) to query for
     */
    fun getMerriamWebsterSource(word: String): LiveData<MerriamWebsterSource> {
        val permissiveWordsDefinitions: LiveData<PermissiveWordsDefinitions> =
                if (word.isBlank() || merriamWebsterStore == null || firestoreStore == null) {
                    LiveDataHelper.empty()
                } else {
                    MergedLiveData(
                            merriamWebsterStore.getWordAndDefinitions(word),
                            firestoreStore.getUserLive()) { d1, d2 ->
                        PermissiveWordsDefinitions(d2, d1)
                    }
                }


        return Transformations.map(permissiveWordsDefinitions) {
            MerriamWebsterSource(it)
        }
    }

    /**
     * Get a list of [FirestoreGlobalSource] words that are "trending" (aka have the highest
     * [GlobalWord.totalViewCount].
     *
     * TODO create a cascading counter to hold view counts for different intervals (now, hour,
     * TODO day, week, month, quater, year, all_time)
     *
     * @param limit The max number of items to return
     */
    fun getTrending(limit: Long? = null, filter: List<Period>): LiveData<List<FirestoreGlobalSource>> {
        if (firestoreStore == null) return LiveDataHelper.empty()
        return Transformations.map(firestoreStore.getTrending(limit, filter)) { globalWords ->
            globalWords.map { FirestoreGlobalSource(it) }
        }
    }

    /**
     * Get a list of [FirestoreUserSource] words that are "favorited" (contain
     * [UserWordType.FAVORITE] in their [UserWord.types] map set to true).
     *
     * @param limit The max number of items to return
     */
    fun getFavorites(limit: Long? = null): LiveData<List<FirestoreUserSource>> {
        if (firestoreStore == null) return LiveDataHelper.empty()
        return Transformations.map(firestoreStore.getFavorites(limit)) { userWords ->
            userWords.map { FirestoreUserSource(it) }
        }
    }

    /**
     * Set a [UserWord] as "favorited" (put or set [UserWordType.FAVORITE] in the
     * [UserWord.types] map to true)
     *
     * @param id The Firestore document id of the word to be favorited. The document id should be
     *      the same as the [UserWord.word] property (the word as it appears in the dictionary)
     */
    fun setFavorite(id: String, favorite: Boolean) {
        if (id.isBlank()) return

        launch {
            firestoreStore?.setFavorite(id, favorite)
        }
    }

    /**
     * Get a list of [FirestoreUserSource] words that a user has recently viewed (where
     * [UserWord.types] contains [UserWordType.RECENT] set to true)
     *
     * @param limit The max number of items to return
     */
    fun getRecents(limit: Long? = null): LiveData<List<FirestoreUserSource>> {
        if (firestoreStore == null) return LiveDataHelper.empty()
        return Transformations.map(firestoreStore.getRecents(limit)) { userWords ->
            userWords.map { FirestoreUserSource(it) }
        }
    }

    /**
     * Set a [UserWord] as "recented" (put or set [UserWordType.RECENT] in the [UserWord.types] map
     * to true)
     *
     * @param id The Firstore document id of the word to set as recented. The document id should
     *      be the same as the [UserWord.word] property (the word as it appears in the dictionary)
     */
    fun setRecent(id: String) {
        if (id.isBlank()) return

        launch {
            firestoreStore?.setRecent(id)
        }
    }

}

