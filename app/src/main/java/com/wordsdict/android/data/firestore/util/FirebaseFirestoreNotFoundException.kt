package com.wordsdict.android.data.firestore.util

import com.google.firebase.firestore.FirebaseFirestoreException

class FirebaseFirestoreNotFoundException(
        docId: String
) : FirebaseFirestoreException("Document $docId not found", FirebaseFirestoreException.Code.NOT_FOUND)

