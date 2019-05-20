package space.narrate.words.android.data.auth

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import space.narrate.words.android.data.firestore.users.User
import space.narrate.words.android.data.firestore.util.users
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

// TODO: Refactor/Clean up.
class AuthenticationStore {

    // TODO: Move into constructor and inject.
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val hasFirebaseUser: Boolean
        get() = auth.currentUser != null

    /**
     * Takes a valid [FirebaseUser] and creates an [Auth] obj. by attaching a [User].
     *
     * There should never be a firebase user without a corresponding User Firestore document,
     * making this possible.
     *
     * @return A valid Auth object containing the passed in [firebaseUser] and it's matching [User]
    */
    suspend fun getCurrentAuth(): Auth = suspendCoroutine { cont ->
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            attachUser(cont, firebaseUser)
        } else {
            cont.resumeWithFirebaseAuthException(FirebaseAuthWordsException.LoginException)
        }
    }

    /**
     * Given an [email] and [password], link the current anonymous [FirebaseUser]. If an anonymous
     * [FirebaseUser] is not present, create a new one using the provided credentials.
     *
     * @return A valid [Auth] object containing the either no-longer-anonymous [FirebaeUser] and
     *  updated [User] or the newly created [FirebaseUser] and [User]
     */
    suspend fun signUp(email: String, password: String, confirmPassword: String): Auth =
            suspendCoroutine { cont ->
                val auth = FirebaseAuth.getInstance()
                when {
                    password != confirmPassword -> {
                        cont.resumeWithFirebaseAuthException(
                            FirebaseAuthWordsException.SignUpPasswordMissmatchException
                        )
                    }
                    auth.currentUser?.isAnonymous == true -> {
                        signUpByLinkingCredentials(cont, email, password)
                    }
                    else -> signUpByCreatingEmptyAccount(cont, email, password)
                }
            }

    private fun signUpByLinkingCredentials(
        cont: Continuation<Auth>,
        email: String,
        password: String
    ) {
        val credentials = EmailAuthProvider.getCredential(email, password)
        val auth = FirebaseAuth.getInstance()
        auth.currentUser?.linkWithCredential(credentials)?.addOnCompleteListener {
            if (it.isSuccessful) {
                val firebaseUser = it.result?.user
                if (firebaseUser != null) {
                    attachUser(cont, firebaseUser)
                } else {
                    cont.resumeWithFirebaseAuthException(
                        FirebaseAuthWordsException.SignUpFailedException, it
                    )
                }
            } else {
                cont.resumeWithFirebaseAuthException(
                    FirebaseAuthWordsException.SignUpFailedException, it
                )
            }
        } ?: cont.resumeWithFirebaseAuthException(
            FirebaseAuthWordsException.SignUpNoExistingUserException
        )
    }

    private fun signUpByCreatingEmptyAccount(
        cont: Continuation<Auth>,
        email: String,
        password: String
    ) {
        val auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                val firebaseUser = it.result?.user
                if (firebaseUser != null) {
                    attachUser(cont, firebaseUser)
                } else {
                    cont.resumeWithFirebaseAuthException(
                        FirebaseAuthWordsException.SignUpFailedException
                    )
                }
            } else {
                cont.resumeWithFirebaseAuthException(
                    FirebaseAuthWordsException.SignUpFailedException, it
                )
            }
        }
    }

    /**
     * Authenticate by creating a new, anonymous [FirebaseUser].
     *
     * @return An [Auth] object containing a new, anonymous [FirebaseUser] and a new [User]
     */
    suspend fun signUpAnonymously(): Auth = suspendCoroutine { cont ->
        val auth = FirebaseAuth.getInstance()
        auth.signInAnonymously().addOnCompleteListener {
            if (it.isSuccessful) {
                val firebaseUser = auth.currentUser
                if (firebaseUser != null) {
                    attachUser(cont, firebaseUser)
                } else {
                    cont.resumeWithFirebaseAuthException(
                        FirebaseAuthWordsException.AnonException, it
                    )
                }
            } else {
                cont.resumeWithFirebaseAuthException(
                    FirebaseAuthWordsException.AnonSignUpFailedException, it
                )
            }
        }
    }

    /**
     * Log in with an [email] and [password].
     *
     * @return An [Auth] object with the matching [FirebaseUser] and [User] for the given
     *  credentials
     */
    suspend fun logIn(email: String, password: String): Auth = suspendCoroutine { cont ->
        val auth = FirebaseAuth.getInstance()
        if (email.isNotBlank() && password.isNotBlank()) {
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            var firebaseUser = auth.currentUser
                            if (firebaseUser == null) {
                                cont.resumeWithFirebaseAuthException(
                                    FirebaseAuthWordsException.LoginException, it
                                )
                            } else {
                                attachUser(cont, firebaseUser)
                            }
                        } else {
                            cont.resumeWithFirebaseAuthException(
                                FirebaseAuthWordsException.LoginFailedException, it
                            )
                        }
                    }
        } else {
            cont.resumeWithFirebaseAuthException(
                FirebaseAuthWordsException.LoginEmptyFieldsException
            )
        }
    }



    private fun newUser(
        firestore: FirebaseFirestore,
        cont: Continuation<Auth>,
        firebaseUser: FirebaseUser
    ) {
        val newUser = User(
                firebaseUser.uid,
                firebaseUser.isAnonymous,
                firebaseUser.displayName ?: "",
                firebaseUser.email ?: ""
        )

        firestore.users.document(newUser.uid).set(newUser)
                .addOnSuccessListener {
                    cont.resumeWithValidUser(firebaseUser, newUser)
                }
                .addOnFailureListener {
                    cont.resumeWithException(it)
                }
    }

    /**
     * Launch and forget an update to the Firestore [User] document.
     */
    private fun updateUser(
            firestore: FirebaseFirestore,
            firebaseUser: FirebaseUser,
            user: User
    ) {
        user.isAnonymous = firebaseUser.isAnonymous
        user.name = firebaseUser.displayName ?: user.name
        user.email = firebaseUser.email ?: user.email

        firestore.users.document(user.uid).set(user)
    }

    private fun Continuation<Auth>.resumeWithValidUser(
            firebaseUser: FirebaseUser,
            user: User
    ) {
        resume(Auth(firebaseUser, user))
    }

    /**
     * Use a valid [FirebaseUser] to either get or create the corresponding [User] document.
     */
    private fun attachUser(cont: Continuation<Auth>, firebaseUser: FirebaseUser) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.users.document(firebaseUser.uid).get()
                .addOnFailureListener {
                    when ((it as FirebaseFirestoreException).code) {
                        FirebaseFirestoreException.Code.UNAVAILABLE -> {
                            // create a new user
                            newUser(firestore, cont, firebaseUser)
                        }
                        else -> {
                            cont.resumeWithException(it)
                        }
                    }
                }
                .addOnSuccessListener {
                    if (it.exists()) {
                        val user = it.toObject(User::class.java)
                        if (user != null) {
                            updateUser(firestore, firebaseUser, user)
                            cont.resumeWithValidUser(firebaseUser, user)
                        } else {
                            cont.resumeWithFirebaseAuthException(
                                FirebaseAuthWordsException.FirestoreUserException
                            )
                        }
                    } else {
                        newUser(firestore, cont, firebaseUser)
                    }
                }
    }

    // Helper function to resume a continuation with a firebase auth exception
    private fun <T> Continuation<T>.resumeWithFirebaseAuthException(
        exception: FirebaseAuthWordsException,
        task: Task<AuthResult>? = null
    ) {
        resumeWithException(task?.exception ?: exception)
    }
}