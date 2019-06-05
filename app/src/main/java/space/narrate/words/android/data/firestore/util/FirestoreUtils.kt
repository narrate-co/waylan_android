package space.narrate.words.android.data.firestore.util

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query


/**
 * Convert a Firestore [Query] into a [LiveData] object
 */
fun <T> Query.liveData(clazz: Class<T>): LiveData<List<T>> {
    return FirestoreCollectionLiveData(this, clazz)
}

/**
 * convert a Firestore [DocumentReference] into a [LiveData] object
 */
fun <T> DocumentReference.liveData(clazz: Class<T>): LiveData<T> {
    return FirestoreDocumentLiveData(this, clazz)
}

/**
 * Create a [FirebaseFirestoreException.Code.NOT_FOUND] exception with the given [docId]
 */
fun getFirestoreNotFoundException(docId: String) =
        FirebaseFirestoreException(
                "Document $docId not found",
                FirebaseFirestoreException.Code.NOT_FOUND
        )
