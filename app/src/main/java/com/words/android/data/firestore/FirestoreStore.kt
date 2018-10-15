package com.words.android.data.firestore

import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.words.android.data.DataOwners
import com.words.android.data.disk.AppDatabase
import com.words.android.data.firestore.users.UserWord
import com.words.android.data.firestore.users.UserWordType
import com.words.android.data.firestore.util.FirebaseFirestoreNotFoundException
import com.words.android.data.firestore.util.liveData
import com.words.android.data.firestore.words.GlobalWord
import com.words.android.util.isMoreThanOneMinuteAgo
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.util.*
import kotlin.coroutines.experimental.suspendCoroutine

class FirestoreStore(
        private val firestore: FirebaseFirestore,
        private val db: AppDatabase,
        private val user: FirebaseUser
) {

    companion object {
        private const val TAG = "FirestoreStore"
    }

    fun getGlobalWordLive(id: String): LiveData<GlobalWord> {
        return firestore.words
                .document(id)
                .liveData(GlobalWord::class.java)
    }

    fun getUserWordLive(id: String): LiveData<UserWord> {
        return firestore.userWords(user.uid)
                .document(id)
                .liveData(UserWord::class.java)
    }

    private suspend fun getUserWord(id: String, createIfDoesNotExist: Boolean): UserWord = suspendCoroutine { cont ->
        firestore.userWords(user.uid).document(id).get()
                .addOnFailureListener {
                    Log.d(TAG, "getUserWord onFailureListner = $it. Code = ${(it as FirebaseFirestoreException).code}")
                    when ((it as FirebaseFirestoreException).code) {
                        FirebaseFirestoreException.Code.UNAVAILABLE -> {
                            if (createIfDoesNotExist) {
                                launch {
                                    val newUserWord = newUserWord(id).await()
                                    if (newUserWord != null) {
                                        cont.resume(newUserWord)
                                    } else {
                                        cont.resumeWithException(FirebaseFirestoreException("Unable to create new UserWord", FirebaseFirestoreException.Code.UNKNOWN))
                                    }
                                }
                            } else {
                                cont.resumeWithException(it)
                            }
                        }
                        else -> cont.resumeWithException(it)
                    }
                }
                .addOnSuccessListener {
                    if (it.exists()) {
                        cont.resume(it.toObject(UserWord::class.java)!!)
                    } else {
                        if (createIfDoesNotExist) {
                            launch {
                                val newUserWord = newUserWord(id).await()
                                if (newUserWord != null) {
                                    cont.resume(newUserWord)
                                } else {
                                    cont.resumeWithException(FirebaseFirestoreException("Unable to create new UserWord", FirebaseFirestoreException.Code.UNKNOWN))
                                }
                            }
                        } else {
                            Log.d(TAG, "getUserWord !createIfDoesNotExist. Does not exist: $id")
                            cont.resumeWithException(FirebaseFirestoreNotFoundException(id))
                        }
                    }
                }
    }

    private fun newUserWord(id: String): Deferred<UserWord?> = async {
        //get word from db.
        val word = db.wordDao().get(id)
        //get meanings from db.
        val meanings = db.meaningDao().get(id)

        if (word == null) {
            null
        } else {

            val partOfSpeech: Map<String, String> = meanings?.map { it.partOfSpeech to DataOwners.WORDSET.name }?.distinct()?.toMap() ?: mapOf()
            val defs: Map<String, String> = meanings?.map { it.def to DataOwners.WORDSET.name }?.distinct()?.toMap() ?: mapOf()
            val synonyms: Map<String, String> = meanings?.flatMap { it.synonyms }?.map { it.synonym to DataOwners.WORDSET.name }?.distinct()?.toMap() ?: mapOf()
            val labels: Map<String, String> = meanings?.flatMap { it.labels }?.map { it.name to DataOwners.WORDSET.name }?.distinct()?.toMap() ?: mapOf()


            val userWord = UserWord(
                    id,
                    word.word,
                    Date(),
                    Date(),
                    mutableMapOf(),
                    partOfSpeech.toMutableMap(),
                    defs.toMutableMap(),
                    synonyms.toMutableMap(),
                    labels.toMutableMap())

            userWord
        }
    }

    fun getTrending(limit: Long? = null): LiveData<List<GlobalWord>> {
        val query = firestore.words
                .orderBy("totalViewCount", Query.Direction.DESCENDING)
                .limit(limit ?: 25)

        return query.liveData(GlobalWord::class.java)
    }

    //get all favorites for user
    fun getFavorites(limit: Long? = null): LiveData<List<UserWord>> {
        val query = firestore.userWords(user.uid)
                .whereEqualTo("types.${UserWordType.FAVORITED.name}", true)
                .orderBy("modified", Query.Direction.DESCENDING)
                .limit(limit ?: 25)

        return query.liveData(UserWord::class.java)
    }

    //favorite a word for user
    suspend fun setFavorite(id: String, favorite: Boolean) {
        try {
            val userWord = getUserWord(id, favorite)
            if (favorite) {
                userWord.types[UserWordType.FAVORITED.name] = true
            } else {
                userWord.types.remove(UserWordType.FAVORITED.name)
            }
            userWord.types[UserWordType.RECENT.name] = true
            setUserWord(userWord)
        } catch (e: Exception) {
            Log.d(TAG, "Unable to ${if (favorite) "favorite" else "unfavorite"} UserWord: $e")
        }
    }

    fun getRecents(limit: Long? = null): LiveData<List<UserWord>> {
        val query = firestore.userWords(user.uid)
                .whereEqualTo("types.${UserWordType.RECENT.name}", true)
                .orderBy("modified", Query.Direction.DESCENDING)
                .limit(limit ?: 25)

        return query.liveData(UserWord::class.java)
    }

    suspend fun setRecent(id: String) {
        try {
            val userWord = getUserWord(id, true)
            if (!userWord.types.containsKey(UserWordType.RECENT.name) || userWord.modified.isMoreThanOneMinuteAgo) {
                println("$TAG::setRecent - current view count = ${userWord.totalViewCount}, new view count = ${userWord.totalViewCount + 1}")
                userWord.types[UserWordType.RECENT.name] = true
                userWord.modified = Date()
                userWord.totalViewCount = userWord.totalViewCount + 1
                setUserWord(userWord)
            }
        } catch (e: Exception) {
            Log.d(TAG, "Unable to set recent UserWord: $e")
        }
    }

    private fun setUserWord(userWord: UserWord) {
        firestore.userWords(user.uid).document(userWord.id).set(userWord)
                .addOnFailureListener { Log.d(TAG, "Unable to set UserWord ${userWord.id}: $it") }
                .addOnSuccessListener { Log.d(TAG, "Successfully set UserWord ${userWord.id}") }
    }


    //add meaning

    //edit meaning (synonyms, examples, part of speech, labels

    //delete meaning
}