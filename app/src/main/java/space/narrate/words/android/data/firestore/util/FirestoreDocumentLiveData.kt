package space.narrate.words.android.data.firestore.util

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


/**
 * A helper class to turn a [DocumentSnapshot] [EventListener] into a LiveData object
 */
class FirestoreDocumentLiveData<T>(
        private val documentReference: DocumentReference,
        private val clazz: Class<T>): LiveData<T>(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private var listenerRegistration: ListenerRegistration? = null

    override fun onActive() {
        super.onActive()
        listenerRegistration = documentReference.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException == null) {
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    // move parsing off the main thread
                    launch {
                        value = documentSnapshot.toObject(clazz)
                    }
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