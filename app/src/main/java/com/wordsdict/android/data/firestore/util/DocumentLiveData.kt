package com.wordsdict.android.data.firestore.util

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ListenerRegistration

fun <T> DocumentReference.liveData(clazz: Class<T>): LiveData<T> {
    return FirestoreDocumentLiveData(this, clazz)
}

/**
 * A helper class to turn a [DocumentSnapshot] [EventListener] into a LiveData object
 */
class FirestoreDocumentLiveData<T>(
        private val documentReference: DocumentReference,
        private val clazz: Class<T>): LiveData<T>() {

    companion object {
        private const val TAG = "FstrDocumentLiveData"
    }

    private var listenerRegistration: ListenerRegistration? = null

    override fun onActive() {
        super.onActive()
        listenerRegistration = documentReference.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException == null) {
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    value = documentSnapshot.toObject(clazz)
                }
            } else {
                firebaseFirestoreException.printStackTrace()
            }
        }
    }

    override fun onInactive() {
        super.onInactive()
        listenerRegistration?.remove()
        listenerRegistration = null
    }
}