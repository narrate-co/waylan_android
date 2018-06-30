package com.words.android.data.firestore

import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.words.android.data.DataOwners
import com.words.android.data.disk.AppDatabase
import com.words.android.data.firestore.util.FirebaseFirestoreNotFoundException
import com.words.android.data.firestore.util.liveData
import com.words.android.util.toDate
import kotlin.coroutines.experimental.suspendCoroutine

class FirestoreStore(
        private val firestore: FirebaseFirestore,
        private val db: AppDatabase,
        private val user: FirebaseUser
) {

    companion object {
        private const val TAG = "FirestoreStore"
    }

    fun getUserWordLive(id: String): LiveData<UserWord> {
        return firestore.userWords(user.uid)
                .document(id)
                .liveData(UserWord::class.java)
    }

    private suspend fun getUserWord(id: String): UserWord = suspendCoroutine { cont ->
        firestore.userWords(user.uid).document(id).get()
                .addOnFailureListener { cont.resumeWithException(it) }
                .addOnSuccessListener {
                    if (it.exists()) {
                        cont.resume(it.toObject(UserWord::class.java)!!)
                    } else {
                        cont.resumeWithException(FirebaseFirestoreNotFoundException(id))
                    }
                }
    }

    suspend fun newUserWord(id: String): UserWord = suspendCoroutine { cont ->
        //get word from db.
        val word = db.wordDao().get(id)
        //get meanings from db.
        val meanings = db.meaningDao().get(id)

        if (word == null) {
            cont.resumeWithException(IllegalArgumentException("No word fround for id $id"))
        } else {

            val partOfSpeech: Map<String, String> = meanings?.map { it.partOfSpeech to DataOwners.WORDSET.name }?.distinct()?.toMap() ?: mapOf()
            val defs: Map<String, String> = meanings?.map { it.def to DataOwners.WORDSET.name }?.distinct()?.toMap() ?: mapOf()
            val synonyms: Map<String, String> = meanings?.flatMap { it.synonyms }?.map { it.synonym to DataOwners.WORDSET.name }?.distinct()?.toMap() ?: mapOf()
            val labels: Map<String, String> = meanings?.flatMap { it.labels }?.map { it.name to DataOwners.WORDSET.name }?.distinct()?.toMap() ?: mapOf()


            val userWord = UserWord(
                    id,
                    word.word,
                    word.created.toDate,
                    word.modified.toDate,
                    mutableMapOf(),
                    partOfSpeech.toMutableMap(),
                    defs.toMutableMap(),
                    synonyms.toMutableMap(),
                    labels.toMutableMap())

            cont.resume(userWord)
        }
    }

    //get all favorites for user
    fun getFavorites(): LiveData<List<UserWord>> {
        return firestore.userWords(user.uid)
                .whereEqualTo("types.${UserWordType.FAVORITED.name}", true)
                .liveData(UserWord::class.java)
    }

    //favorite a word for user
    suspend fun setFavorite(id: String, favorite: Boolean) {
        try {
            val userWord = getUserWord(id)
            setFavorite(favorite, userWord)
        } catch (e: Exception) {
            try {
                if (!favorite) return

                val newUserWord = newUserWord(id)
                setFavorite(favorite, newUserWord)
            } catch (e: Exception) {
                Log.d(TAG, "Unable to create new UserWord: $e")
            }
        }

    }

    private fun setFavorite(favorite: Boolean, userWord: UserWord) {
        if (favorite) {
            userWord.types[UserWordType.FAVORITED.name] = true
        } else {
            userWord.types.remove(UserWordType.FAVORITED.name)
        }
        userWord.types[UserWordType.RECENT.name] = true
        firestore.userWords(user.uid).document(userWord.id).set(userWord)
                .addOnFailureListener { Log.d(TAG, "Unable to set UserWord ${userWord.id}: $it") }
                .addOnSuccessListener { Log.d(TAG, "Successfully set UserWord ${userWord.id}") }
    }


    //add meaning

    //edit meaning (synonyms, examples, part of speech, labels

    //delete meaning
}