package space.narrate.waylan.core.data.firestore.util

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * A helper class to turn a Firestore [QuerySnapshot]
 * [EventListener] into a [LiveData] object
 */
class FirestoreCollectionLiveData<T>(
    private val query: Query,
    private val clazz: Class<T>
): LiveData<List<T>>(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private var listenerRegistration: ListenerRegistration? = null

    private val eventListener =
        EventListener<QuerySnapshot> { querySnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException != null) {
                firebaseFirestoreException.printStackTrace()
            } else {
                // move parsing off the main thread
                launch { value = querySnapshot?.documents?.map { it.toObject(clazz)!! } }
            }
        }

    override fun onActive() {
        listenerRegistration = query.addSnapshotListener(MetadataChanges.INCLUDE, eventListener)
    }

    override fun onInactive() {
        listenerRegistration?.remove()
    }

}

