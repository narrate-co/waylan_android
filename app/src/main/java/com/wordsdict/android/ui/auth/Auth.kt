package com.wordsdict.android.ui.auth

import com.google.firebase.auth.FirebaseUser
import com.wordsdict.android.data.firestore.users.User

data class Auth(val firebaseUser: FirebaseUser, val user: User)

