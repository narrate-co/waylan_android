package space.narrate.words.android.data.firestore

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import space.narrate.words.android.data.disk.AppDatabase
import space.narrate.words.android.data.firestore.util.*
import space.narrate.words.android.data.firestore.words.GlobalWord
import space.narrate.words.android.ui.search.Period
import space.narrate.words.android.util.isMoreThanOneMinuteAgo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import space.narrate.words.android.data.Result
import space.narrate.words.android.data.firestore.users.PluginState
import space.narrate.words.android.data.firestore.users.User
import space.narrate.words.android.data.firestore.users.UserWord
import space.narrate.words.android.data.firestore.users.UserWordType
import space.narrate.words.android.util.LiveDataUtils
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * The top-most store for access to Firestore data. This class abstracts handles CRUD operations
 * for [User], [UserWord] and [GlobalWord].
 *
 * Note this store is user-centric. Meaning it's only available with a valid user and all
 * actions made are seen as made by the current [firestoreUser]
 */
class FirestoreStore(
    private val firestore: FirebaseFirestore,
    private val db: AppDatabase
) : CoroutineScope {

    companion object {
        private const val TAG = "FirestoreStore"
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    fun getGlobalWordLive(id: String): LiveData<GlobalWord> {
        if (id.isBlank()) return LiveDataUtils.empty()
        return firestore.words
            .document(id)
            .liveData(GlobalWord::class.java)
    }

    fun getUserWordLive(id: String, userId: String): LiveData<UserWord> {
        if (id.isBlank()) return LiveDataUtils.empty()
        return firestore.userWords(userId)
            .document(id)
            .liveData(UserWord::class.java)
    }

    private suspend fun getUserWord(
        id: String,
        userId: String,
        createIfDoesNotExist: Boolean
    ): Result<UserWord> {
        try {
            val userWord = suspendCoroutine<UserWord> { cont ->
                firestore.userWords(userId).document(id).get()
                    .addOnFailureListener {
                        cont.resumeWithException(it)
                    }
                    .addOnSuccessListener {
                        if (it.exists()) {
                            cont.resume(it.toObject(UserWord::class.java)!!)
                        } else {
                            cont.resumeWithException(getFirestoreNotFoundException(id))

                        }
                    }
            }
            return Result.Success(userWord)
        } catch (e: Exception) {
            return when ((e as FirebaseFirestoreException).code) {
                FirebaseFirestoreException.Code.UNAVAILABLE,
                FirebaseFirestoreException.Code.NOT_FOUND -> {
                    if (createIfDoesNotExist) {
                        newUserWord(id)
                    } else {
                        Result.Error(e)
                    }
                }
                else -> Result.Error(e)
            }
        }
    }

    private suspend fun newUserWord(id: String): Result<UserWord> {
        //get word from db.
        val word = db.wordDao().get(id)

        // get meanings from db.
        // we add a limited number of definitions, synonyms etc to make it easy to query for,
        // for example, a user's favorites and have a list populate with the word and definition
        // preview without the need for extraneous joins, queries, etc.
        val meanings = db.meaningDao().get(id)

        if (word == null) {
            return Result.Error(Exception("Word '$id' does not exist in wordset, a necessary condition for creating a UserWord"))
        } else {

            val partOfSpeech: Map<String, String> =
                meanings?.map { it.partOfSpeech to DataOwners.WORDSET.name }
                    ?.distinct()
                    ?.toMap() ?: mapOf()

            val defs: Map<String, String> =
                meanings?.map { it.def to DataOwners.WORDSET.name }
                    ?.distinct()
                    ?.toMap() ?: mapOf()

            val synonyms: Map<String, String> =
                meanings?.flatMap { it.synonyms }
                    ?.map { it.synonym to DataOwners.WORDSET.name }
                    ?.distinct()
                    ?.toMap() ?: mapOf()

            val labels: Map<String, String> =
                meanings?.flatMap { it.labels }
                    ?.map { it.name to DataOwners.WORDSET.name }
                    ?.distinct()
                    ?.toMap() ?: mapOf()


            val userWord = UserWord(
                id,
                word.word,
                Date(),
                Date(),
                mutableMapOf(),
                partOfSpeech.toMutableMap(),
                defs.toMutableMap(),
                synonyms.toMutableMap(),
                labels.toMutableMap()
            )

            return Result.Success(userWord)
        }
    }

    fun getTrending(limit: Long? = null, filter: List<Period>): LiveData<List<GlobalWord>> {
        val period = filter.firstOrNull()?.viewCountProp ?: Period.ALL_TIME.viewCountProp
        val query = firestore.words
            .orderBy(period, Query.Direction.DESCENDING)
            .limit(limit ?: 25)

        return query.liveData(GlobalWord::class.java)
    }

    //get all favorites for firestoreUser
    fun getFavorites(userId: String, limit: Long? = null): LiveData<List<UserWord>> {
        val query = firestore.userWords(userId)
            .whereEqualTo("types.${UserWordType.FAVORITED.name}", true)
            .orderBy("modified", Query.Direction.DESCENDING)
            .limit(limit ?: 25)

        return query.liveData(UserWord::class.java)
    }

    //favorite a word for firestoreUser
    suspend fun setFavorite(
        id: String,
        userId: String,
        favorite: Boolean
    ): Result<UserWord> {
        return updateUserWord(id, userId, true) {
            if (favorite) {
                types[UserWordType.FAVORITED.name] = true
            } else {
                types.remove(UserWordType.FAVORITED.name)
            }
            types[UserWordType.RECENT.name] = true
        }
    }

    fun getRecents(userId: String, limit: Long? = null): LiveData<List<UserWord>> {
        val query = firestore.userWords(userId)
            .whereEqualTo("types.${UserWordType.RECENT.name}", true)
            .orderBy("modified", Query.Direction.DESCENDING)
            .limit(limit ?: 25)

        return query.liveData(UserWord::class.java)
    }

    suspend fun setRecent(id: String, userId: String): Result<UserWord> {
        return updateUserWord(id, userId, true) {
            if (!types.containsKey(UserWordType.RECENT.name) || modified.isMoreThanOneMinuteAgo) {
                types[UserWordType.RECENT.name] = true
                modified = Date()
            }
        }
    }

    private suspend fun setUserWord(
        userWord: UserWord,
        userId: String
    ): Result<UserWord> = suspendCancellableCoroutine { cont ->
        firestore.userWords(userId).document(userWord.id).set(userWord)
            .addOnSuccessListener {
                cont.resume(Result.Success(userWord))
            }
            .addOnFailureListener {
                cont.resume(Result.Error(it))
            }
    }

    private suspend fun updateUserWord(
        id: String,
        uid: String,
        createIfDoesNotExist: Boolean,
        update: UserWord.() -> Unit
    ): Result<UserWord> {
        val result = getUserWord(id, uid, createIfDoesNotExist)
        if (result is Result.Success) {
            val userWord = result.data
            userWord.update()
            return setUserWord(userWord, uid)
        }

        return result
    }

    fun getUserLive(userId: String): LiveData<User> {
        return firestore.users
            .document(userId)
            .liveData(User::class.java)
    }

    suspend fun getUser(uid: String): Result<User> = suspendCancellableCoroutine { cont ->
        firestore.users.document(uid).get()
            .addOnFailureListener {
                cont.resume(Result.Error(it))
            }
            .addOnSuccessListener {
                if (it.exists()) {
                    cont.resume(Result.Success(it.toObject(User::class.java)!!))
                } else {
                    cont.resume(Result.Error(getFirestoreNotFoundException(uid)))
                }
            }
    }

    suspend fun newUser(uid: String, with: User.() -> Unit): Result<User> {
        return try {
            val user = User(uid)
            user.with()
            setUser(user)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Update user as specified by [update].
     */
    suspend fun updateUser(uid: String, update: User.() -> Unit): Result<User> {
        val result = getUser(uid)
        return if (result is Result.Success) {
            val user = result.data
            user.update()
            setUser(user)
        } else {
            result
        }
    }

    /**
     * Create or overwrite a user
     */
    private suspend fun setUser(user: User): Result<User> = suspendCancellableCoroutine { cont ->
        firestore.users.document(user.uid).set(user)
            .addOnSuccessListener {
                cont.resume(Result.Success(user))
            }
            .addOnFailureListener {
                cont.resume(Result.Error(it))
            }
    }

    suspend fun setUserMerriamWebsterState(
        userId: String,
        state: PluginState
    ): Result<User> {
        val result = getUser(userId)
        return if (result is Result.Success) {
            val user = result.data.apply {
                merriamWebsterStarted = state.started
                merriamWebsterPurchaseToken = state.purchaseToken
            }
            setUser(user)
        } else {
            result
        }
    }

    //allow users to add, edit and delete their own definitions, set to either public or private
    //TODO add meaning

    //TODO edit meaning (synonyms, examples, part of speech, labels)

    //TODO delete meaning

}