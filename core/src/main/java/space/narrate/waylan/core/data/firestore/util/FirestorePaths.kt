package space.narrate.waylan.core.data.firestore.util

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Helpers for easier Firestore data access
 */

object RootCollection {
    const val USERS = "users"
    const val WORDS = "words"
}

object UserCollection {
    const val WORDS = "words"
    const val ADD_ONS = "add_ons"
}

object UserWordCollection {
    const val EXAMPLES = "examples"
}

val FirebaseFirestore.users : CollectionReference
    get() = collection(RootCollection.USERS)

val FirebaseFirestore.words : CollectionReference
    get() = collection(RootCollection.WORDS)

fun FirebaseFirestore.userWords(uid: String) : CollectionReference =
    users.document(uid).collection(UserCollection.WORDS)

fun FirebaseFirestore.userAddOns(uid: String): CollectionReference =
    users.document(uid).collection(UserCollection.ADD_ONS)

fun FirebaseFirestore.userWordExamples(uid: String, wordId: String): CollectionReference =
    userWords(uid).document(wordId).collection(UserWordCollection.EXAMPLES)


