package space.narrate.words.android.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import space.narrate.words.android.data.disk.AppDatabase
import space.narrate.words.android.data.mw.PermissiveWordsDefinitions
import space.narrate.words.android.data.firestore.FirestoreStore
import space.narrate.words.android.data.firestore.users.UserWord
import space.narrate.words.android.data.firestore.words.GlobalWord
import space.narrate.words.android.data.mw.MerriamWebsterStore
import space.narrate.words.android.data.spell.SymSpellStore
import space.narrate.words.android.ui.search.Period
import space.narrate.words.android.util.LiveDataUtils
import space.narrate.words.android.util.widget.MergedLiveData
import kotlinx.coroutines.launch
import space.narrate.words.android.data.disk.mw.MwWordAndDefinitionGroups
import space.narrate.words.android.data.disk.wordset.Word
import space.narrate.words.android.data.disk.wordset.WordAndMeanings
import space.narrate.words.android.data.spell.SuggestItem
import kotlin.coroutines.CoroutineContext

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
) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    fun getWordsetWord(word: String): LiveData<Word> {
        return db.wordDao().getLive(word)
    }

    fun getWordsetWords(input: String): LiveData<List<Word>> {
        return db.wordDao().load("$input%")
    }

    fun getWordsetWordAndMeanings(word: String): LiveData<WordAndMeanings> {
        return db.wordDao().getWordAndMeanings(word)
    }

    fun getSuggestItems(input: String): LiveData<List<SuggestItem>> {
        return symSpellStore.lookupLive(input)
    }

    fun getUserWord(id: String): LiveData<UserWord> {
        return firestoreStore?.getUserWordLive(id) ?: LiveDataUtils.empty()
    }

    fun getMerriamWebsterWord(word: String): LiveData<List<MwWordAndDefinitionGroups>> {
        return merriamWebsterStore?.getWordAndDefinitions(word) ?: LiveDataUtils.empty()
    }

    fun getGlobalWordTrending(
        limit: Long? = null,
        filter: List<Period> = emptyList()
    ): LiveData<List<GlobalWord>> {
        return firestoreStore?.getTrending(limit, filter) ?: LiveDataUtils.empty()
    }

    fun getUserWordFavorites(limit: Long? = null) : LiveData<List<UserWord>> {
        return firestoreStore?.getFavorites(limit) ?: LiveDataUtils.empty()
    }

    fun setUserWordFavorite(id: String, favorite: Boolean) {
        if (id.isBlank()) return

        launch {
            firestoreStore?.setFavorite(id, favorite)
        }
    }

    fun getUserWordRecents(limit: Long? = null): LiveData<List<UserWord>> {
        return firestoreStore?.getRecents(limit) ?: LiveDataUtils.empty()
    }

    fun setUserWordRecent(id: String) {
        if (id.isBlank()) return

        launch {
            firestoreStore?.setRecent(id)
        }
    }

}

