package com.wordsdict.android.data.firestore.util

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.*
import kotlinx.coroutines.android.UI
import kotlinx.coroutines.launch


fun <T> Query.liveData(clazz: Class<T>): LiveData<List<T>> {
    return FirestoreCollectionLiveData(this, clazz)
}

/**
 * A helper class to turn a [QuerySnapshot] [EventListener] into a LiveData object
 */
class FirestoreCollectionLiveData<T>(private val query: Query, private val clazz: Class<T>): LiveData<List<T>>() {

    companion object {
        private const val TAG = "FirestoreCollLiveData"
    }
    private var listenerRegistration: ListenerRegistration? = null

    private val eventListener = EventListener<QuerySnapshot> { querySnapshot, firebaseFirestoreException ->
        val source = if (querySnapshot != null && querySnapshot.metadata.hasPendingWrites()) "Local" else "Server"
        if (firebaseFirestoreException != null) {
            firebaseFirestoreException.printStackTrace()
        } else {
            launch(UI) {
                value = querySnapshot?.documents?.map { it.toObject(clazz)!! }
            }
        }
    }

    override fun onActive() {
        listenerRegistration = query.addSnapshotListener(MetadataChanges.INCLUDE, eventListener)
    }

    override fun onInactive() {
        listenerRegistration?.remove()
    }

}

