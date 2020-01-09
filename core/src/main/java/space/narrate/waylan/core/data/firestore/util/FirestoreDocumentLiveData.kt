package space.narrate.waylan.core.data.firestore.util

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestoreException
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

    private var onError: ((FirebaseFirestoreException) -> Unit)? = null

    override fun onActive() {
        super.onActive()
        listenerRegistration =
            documentReference.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    launch { value = documentSnapshot.toObject(clazz) }
                } else {
                    onError?.let {
                        it(firebaseFirestoreException
                            ?: getFirestoreNotFoundException(documentReference.id))
                    }
                }
            }
    }

    /**
     * Optionally run logic when a snapshot listener captures an error.
     *
     * This can be useful if, for example, you always want to create a a document when it is not
     * found. Creating that document in this block will then re-trigger the above listener and
     * pass the end result through the live data.
     */
    fun doOnError(block: (FirebaseFirestoreException) -> Unit): FirestoreDocumentLiveData<T> {
        onError = block
        return this
    }

    override fun onInactive() {
        super.onInactive()
        listenerRegistration?.remove()
        listenerRegistration = null
    }
}