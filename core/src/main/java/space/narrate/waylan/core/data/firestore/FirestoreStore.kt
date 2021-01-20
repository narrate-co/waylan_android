package space.narrate.waylan.core.data.firestore

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import space.narrate.waylan.core.data.Result
import space.narrate.waylan.core.data.firestore.users.AddOn
import space.narrate.waylan.core.data.firestore.users.User
import space.narrate.waylan.core.data.firestore.users.UserAddOn
import space.narrate.waylan.core.data.firestore.users.UserAddOnActionUseCase
import space.narrate.waylan.core.data.firestore.users.UserWord
import space.narrate.waylan.core.data.firestore.users.UserWordType
import space.narrate.waylan.core.data.firestore.util.getFirestoreNotFoundException
import space.narrate.waylan.core.data.firestore.util.liveData
import space.narrate.waylan.core.data.firestore.util.userAddOns
import space.narrate.waylan.core.data.firestore.util.userWords
import space.narrate.waylan.core.data.firestore.util.users
import space.narrate.waylan.core.data.firestore.util.words
import space.narrate.waylan.core.data.firestore.words.GlobalWord
import space.narrate.waylan.core.data.wordset.WordsetDatabase
import space.narrate.waylan.core.util.LiveDataUtils
import space.narrate.waylan.core.util.isMoreThanOneMinuteAgo
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import space.narrate.waylan.core.data.firestore.users.UserWordExample
import space.narrate.waylan.core.data.firestore.util.userWordExamples

/**
 * The top-most store for access to Firestore data. This class handles CRUD operations
 * for [User], [UserWord] and [GlobalWord].
 */
class FirestoreStore(
    private val firestore: FirebaseFirestore,
    private val db: WordsetDatabase
) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

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

    fun getUserLive(uid: String): LiveData<User> {
        return firestore.users
            .document(uid)
            .liveData(User::class.java)
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
     * Create or overwrite a user
     */
    private suspend fun setUser(user: User): Result<User> = suspendCancellableCoroutine { cont ->
        firestore.users.document(user.uid).set(user)
            .addOnSuccessListener { cont.resume(Result.Success(user)) }
            .addOnFailureListener { cont.resume(Result.Error(it)) }
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
     * Get the specified [UserAddOn] on of [addOn] from the user with with [uid].
     *
     * This method will always create the [UserAddOn] if it does not exist.
     */
    suspend fun getUserAddOn(
        uid: String,
        addOn: AddOn
    ): Result<UserAddOn> {
        try {
            val userAddOn = suspendCoroutine<UserAddOn> { cont ->
                firestore.userAddOns(uid).document(addOn.id).get()
                    .addOnFailureListener {
                        cont.resumeWithException(it)
                    }
                    .addOnSuccessListener {
                        if (it.exists()) {
                            val obj = it.toObject(UserAddOn::class.java)!!
                            cont.resume(obj)
                        } else {
                            cont.resumeWithException(getFirestoreNotFoundException(addOn.id))
                        }
                    }
            }
            return Result.Success(userAddOn)
        } catch (e: Exception) {
            return when ((e as FirebaseFirestoreException).code) {
                FirebaseFirestoreException.Code.UNAVAILABLE,
                FirebaseFirestoreException.Code.NOT_FOUND -> newUserAddOn(uid, addOn)
                else -> Result.Error(e)
            }
        }
    }

    fun getUserAddOnLive(uid: String, addOn: AddOn): LiveData<UserAddOn> {
        return firestore.userAddOns(uid)
            .document(addOn.id)
            .liveData(UserAddOn::class.java)
            .doOnError {
                when (it.code) {
                    FirebaseFirestoreException.Code.UNAVAILABLE,
                    FirebaseFirestoreException.Code.NOT_FOUND -> launch {
                        newUserAddOn(uid, addOn)
                    }
                }
            }
    }

    fun getUserAddOnsLive(uid: String): LiveData<List<UserAddOn>> {
        return firestore.userAddOns(uid)
            .liveData(UserAddOn::class.java)
    }

    private suspend fun newUserAddOn(uid: String, addOn: AddOn): Result<UserAddOn> {
        return when (val userResult = getUser(uid)) {
            // Create a new UserAddOn in a valid, free trial state
            is Result.Success -> {
                val user = userResult.data
                val userAddOn = UserAddOn(
                    addOn.id,
                    if (user.isAnonymous) 7L else 30L,
                    false
                )

                // Transfer legacy User properties which kept track of Merriam-Webster purchases
                // over to the new UserAddOn document.
                if (addOn == AddOn.MERRIAM_WEBSTER) {
                    userAddOn.apply {
                        started = user.merriamWebsterStarted
                        hasStartedFreeTrial = true
                        purchaseToken = user.merriamWebsterPurchaseToken
                        validDurationDays = when {
                            user.merriamWebsterPurchaseToken.isNotBlank() -> 365L
                            !user.isAnonymous -> 30L
                            else -> 7L
                        }
                        isAwareOfExpiration = false
                    }
                }
                return setUserAddOn(uid, userAddOn)
            }
            is Result.Error -> Result.Error(userResult.exception)
        }
    }

    private suspend fun setUserAddOn(
        uid: String,
        userAddOn: UserAddOn
    ): Result<UserAddOn> = suspendCancellableCoroutine { cont ->
        firestore.userAddOns(uid).document(userAddOn.id).set(userAddOn)
            .addOnSuccessListener { cont.resume(Result.Success(userAddOn)) }
            .addOnFailureListener { cont.resume(Result.Error(it)) }
    }

    private suspend fun updateUserAddOn(
        uid: String,
        addOn: AddOn,
        with: UserAddOn.(user: User) -> Unit
    ): Result<UserAddOn> {
        val result = getUserAddOn(uid, addOn)
        val userResult = getUser(uid)
        return if (result is Result.Success && userResult is Result.Success) {
            val userAddOn = result.data.apply { with(userResult.data) }
            return setUserAddOn(uid, userAddOn)
        } else {
            result
        }

    }

    suspend fun updateUserAddOnAction(
        uid: String,
        addOn: AddOn,
        useCase: UserAddOnActionUseCase
    ) : Result<UserAddOn> {
        return updateUserAddOn(uid, addOn) { user ->
            useCase.perform(user, this)
        }
    }

    fun getGlobalWordLive(id: String): LiveData<GlobalWord> {
        if (id.isBlank()) return LiveDataUtils.empty()
        return firestore.words
            .document(id)
            .liveData(GlobalWord::class.java)
    }

    fun getGlobalWordsTrendingLive(limit: Long?, filter: List<Period>): LiveData<List<GlobalWord>> {
        val period = filter.firstOrNull()?.viewCountProp ?: Period.ALL_TIME.viewCountProp
        val query = firestore.words
            .orderBy(period, Query.Direction.DESCENDING)
            .limit(limit ?: 25)

        return query.liveData(GlobalWord::class.java)
    }

    private suspend fun getUserWord(
        id: String,
        uid: String,
        createIfDoesNotExist: Boolean
    ): Result<UserWord> {
        try {
            val userWord = suspendCoroutine<UserWord> { cont ->
                firestore.userWords(uid).document(id).get()
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

    fun getUserWordLive(id: String, uid: String): LiveData<UserWord> {
        if (id.isBlank()) return LiveDataUtils.empty()
        return firestore.userWords(uid)
            .document(id)
            .liveData(UserWord::class.java)
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

    fun getUserWordsFavoriteLive(uid: String, limit: Long?): LiveData<List<UserWord>> {
        val query = firestore.userWords(uid)
            .whereEqualTo("types.${UserWordType.FAVORITED.name}", true)
            .orderBy("modified", Query.Direction.DESCENDING)
            .limit(limit ?: 25)

        return query.liveData(UserWord::class.java)
    }

    suspend fun setFavorite(
        id: String,
        uid: String,
        favorite: Boolean
    ): Result<UserWord> {
        return updateUserWord(id, uid, true) {
            if (favorite) {
                types[UserWordType.FAVORITED.name] = true
            } else {
                types.remove(UserWordType.FAVORITED.name)
            }
            types[UserWordType.RECENT.name] = true
        }
    }

    fun getUserWordsRecentLive(uid: String, limit: Long?): LiveData<List<UserWord>> {
        val query = firestore.userWords(uid)
            .whereEqualTo("types.${UserWordType.RECENT.name}", true)
            .orderBy("modified", Query.Direction.DESCENDING)
            .limit(limit ?: 25)

        return query.liveData(UserWord::class.java)
    }


    suspend fun setRecent(id: String, uid: String): Result<UserWord> {
        return updateUserWord(id, uid, true) {
            if (!types.containsKey(UserWordType.RECENT.name) || modified.isMoreThanOneMinuteAgo) {
                types[UserWordType.RECENT.name] = true
                modified = Date()
            }
        }
    }

    private suspend fun setUserWord(
        userWord: UserWord,
        uid: String
    ): Result<UserWord> = suspendCancellableCoroutine { cont ->
        firestore.userWords(uid).document(userWord.id).set(userWord)
            .addOnSuccessListener { cont.resume(Result.Success(userWord)) }
            .addOnFailureListener { cont.resume(Result.Error(it)) }
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

    fun getUserWordExamplesLive(
        id: String,
        uid: String,
        limit: Long?
    ): LiveData<List<UserWordExample>> {
        val query = firestore.userWordExamples(uid, id)
            .orderBy("modified", Query.Direction.DESCENDING)
            .limit(limit ?: 25)

        return query.liveData(UserWordExample::class.java)
    }
}