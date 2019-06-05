package space.narrate.words.android.data.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import space.narrate.words.android.data.disk.AppDatabase
import space.narrate.words.android.data.firestore.FirestoreStore
import space.narrate.words.android.data.firestore.users.UserWord
import space.narrate.words.android.data.firestore.words.GlobalWord
import space.narrate.words.android.data.mw.MerriamWebsterStore
import space.narrate.words.android.data.spell.SymSpellStore
import space.narrate.words.android.ui.search.Period
import space.narrate.words.android.util.LiveDataUtils
import kotlinx.coroutines.launch
import space.narrate.words.android.data.auth.AuthenticationStore
import space.narrate.words.android.data.disk.mw.MwWordAndDefinitionGroups
import space.narrate.words.android.data.disk.wordset.Word
import space.narrate.words.android.data.disk.wordset.WordAndMeanings
import space.narrate.words.android.data.spell.SuggestItem
import space.narrate.words.android.util.switchMapTransform
import kotlin.coroutines.CoroutineContext

/**
 * A repository for all data access to all underlying dictionaries, including from WordSet,
 * Merriam-Webster, SymSpell, Firestore [UserWord]s and Firestore [GlobalWord]s. Clients should
 * access word-related data through [WordRepository].
 */
class WordRepository(
        private val db: AppDatabase,
        private val authenticationStore: AuthenticationStore,
        private val firestoreStore: FirestoreStore,
        private val merriamWebsterStore: MerriamWebsterStore,
        private val symSpellStore: SymSpellStore
) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    fun getWordsetWord(word: String): LiveData<Word?> {
        return db.wordDao().getLive(word)
    }

    fun getWordsetWords(input: String): LiveData<List<Word>> {
        return db.wordDao().load("$input%")
    }

    fun getWordsetWordAndMeanings(word: String): LiveData<WordAndMeanings?> {
        return db.wordDao().getWordAndMeanings(word)
    }

    fun getSuggestItems(input: String): LiveData<List<SuggestItem>> {
        return symSpellStore.lookupLive(input)
    }

    fun getUserWord(id: String): LiveData<UserWord> {
        return authenticationStore.user.switchMapTransform { user ->
            firestoreStore.getUserWordLive(id, user.uid)
        }
    }

    fun getMerriamWebsterWord(word: String): LiveData<List<MwWordAndDefinitionGroups>> {
        return merriamWebsterStore.getWordAndDefinitions(word)
    }

    fun getGlobalWordTrending(
        limit: Long? = null,
        filter: List<Period> = emptyList()
    ): LiveData<List<GlobalWord>> {
        return firestoreStore.getTrending(limit, filter)
    }

    fun getUserWordFavorites(limit: Long? = null) : LiveData<List<UserWord>> {
        return authenticationStore.user.switchMapTransform { user ->
            firestoreStore.getFavorites(user.uid, limit)
        }
    }

    fun setUserWordFavorite(id: String, favorite: Boolean) {
        if (id.isBlank()) return
        val uid = authenticationStore.user.value?.uid ?: return

        // Launch and forget
        launch {
            firestoreStore.setFavorite(id, uid, favorite)
        }
    }

    fun getUserWordRecents(limit: Long? = null): LiveData<List<UserWord>> {
        return authenticationStore.user.switchMapTransform { user ->
            firestoreStore.getRecents(user.uid, limit)
        }
    }

    fun setUserWordRecent(id: String) {
        if (id.isBlank()) return
        val uid = authenticationStore.user.value?.uid ?: return

        // Launch and forget
        launch {
            firestoreStore.setRecent(id, uid)
        }
    }
}

