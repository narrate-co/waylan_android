package space.narrate.words.android.data.auth

import com.google.firebase.auth.FirebaseUser
import space.narrate.words.android.data.firestore.users.User

/**
 * An object to hold all the necessary properties in order to create an authorized user. Objects
 * in [UserScope] need these properties before they can be provided.
 *
 * Once valid, this class is used to call [App.setUser], satisfying all dependencies needed for
 * [UserScope]d objects.
 *
 * TODO Move this back into ui.auth and use as a model class, refactoring out it's usage in
 * AuthStore
 */
data class Auth(val firebaseUser: FirebaseUser, val user: User)

