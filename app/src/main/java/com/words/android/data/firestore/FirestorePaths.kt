package com.words.android.data.firestore

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

object RootCollection {
    const val USERS = "users"
    const val WORDS = "words"
}

val FirebaseFirestore.users : CollectionReference
    get() = collection(RootCollection.USERS)

val FirebaseFirestore.words : CollectionReference
    get() = collection(RootCollection.WORDS)

fun FirebaseFirestore.userWords(uid: String) : CollectionReference
        = users.document(uid).collection("words")

