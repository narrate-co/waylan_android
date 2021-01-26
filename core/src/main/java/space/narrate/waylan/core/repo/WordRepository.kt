package space.narrate.waylan.core.repo

import androidx.lifecycle.LiveData
import java.lang.Exception
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import space.narrate.waylan.core.data.firestore.AuthenticationStore
import space.narrate.waylan.core.data.firestore.FirestoreStore
import space.narrate.waylan.core.data.firestore.Period
import space.narrate.waylan.core.data.firestore.users.UserWord
import space.narrate.waylan.core.data.firestore.words.GlobalWord
import space.narrate.waylan.core.data.spell.SuggestItem
import space.narrate.waylan.core.data.spell.SymSpellStore
import space.narrate.waylan.core.data.wordset.Word
import space.narrate.waylan.core.data.wordset.WordAndMeanings
import space.narrate.waylan.core.data.wordset.WordsetDatabase
import space.narrate.waylan.core.util.switchMapTransform
import kotlin.coroutines.CoroutineContext
import space.narrate.waylan.core.data.Result
import space.narrate.waylan.core.data.firestore.users.UserWordExample

/**
 * A repository for all data access to all underlying dictionaries, including from WordSet,
 * Merriam-Webster, SymSpell, Firestore [UserWord]s and Firestore [GlobalWord]s. Clients should
 * access word-related data through [WordRepository].
 */
class WordRepository(
    private val db: WordsetDatabase,
    private val authenticationStore: AuthenticationStore,
    private val firestoreStore: FirestoreStore,
    private val symSpellStore: SymSpellStore,
    private val ioDispatcher: CoroutineDispatcher
) : CoroutineScope by CoroutineScope(ioDispatcher) {

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
        return authenticationStore.uid.switchMapTransform {
            firestoreStore.getUserWordLive(id, it)
        }
    }

    fun getGlobalWordTrending(
        limit: Long? = null,
        filter: List<Period> = emptyList()
    ): LiveData<List<GlobalWord>> {
        return firestoreStore.getGlobalWordsTrendingLive(limit, filter)
    }

    fun getUserWordFavorites(limit: Long? = null) : LiveData<List<UserWord>> {
        return authenticationStore.uid.switchMapTransform {
            firestoreStore.getUserWordsFavoriteLive(it, limit)
        }
    }

    fun setUserWordFavorite(id: String, favorite: Boolean) {
        if (id.isBlank()) return
        val uid = authenticationStore.uid.value ?: return

        // Launch and forget
        launch {
            firestoreStore.setFavorite(id, uid, favorite)
        }
    }

    fun getUserWordRecents(limit: Long? = null): LiveData<List<UserWord>> {
        return authenticationStore.uid.switchMapTransform {
            firestoreStore.getUserWordsRecentLive(it, limit)
        }
    }

    fun setUserWordRecent(id: String) {
        if (id.isBlank()) return
        val uid = authenticationStore.uid.value ?: return

        // Launch and forget
        launch {
            firestoreStore.setRecent(id, uid)
        }
    }

    fun getUserWordExamples(id: String, limit: Long? = null): LiveData<List<UserWordExample>> {
        return authenticationStore.uid.switchMapTransform {
            firestoreStore.getUserWordExamplesLive(id, it, limit)
        }
    }

    suspend fun updateUserWordExample(
        id: String,
        example: UserWordExample,
    ): Result<UserWordExample> {
        val uid = authenticationStore.uid.value ?: return Result.Error(Exception("No authorized user"))
        if (example.id.isEmpty()) { // This is a transfer object to create a new example
            return firestoreStore.newUserWordExample(uid, id, example)
        } else { // This is an existing example that should be updated
            return firestoreStore.setUserWordExample(uid, id, example)
        }
    }
}

