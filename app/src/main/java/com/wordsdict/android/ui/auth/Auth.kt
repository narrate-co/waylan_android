package com.wordsdict.android.ui.auth

import com.google.firebase.auth.FirebaseUser
import com.wordsdict.android.data.firestore.users.User

/**
 * An object to hold all the necessary properties in order to create an authorized user. Objects
 * in [UserScope] need these properties before they can be provided.
 *
 * Once valid, this class is used to call [App.setUser], satisfying all dependencies needed for
 * [UserScope]d objects.
 */
data class Auth(val firebaseUser: FirebaseUser, val user: User)

