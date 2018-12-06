package com.wordsdict.android.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.wordsdict.android.data.firestore.util.users
import com.wordsdict.android.data.firestore.users.User
import com.wordsdict.android.util.FirebaseAuthWordErrorType
import javax.inject.Inject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * A ViewModel that handles the manipulation of [FirebaseUser] and [User] to create
 * valid [Auth] objects.
 *
 * //TODO possibly move FirebaseAuth calls into a data store
 */
class AuthViewModel @Inject constructor(): ViewModel() {

    //TODO use Kotlin backing field style guide underscore
    private val isLoading: MutableLiveData<Boolean> by lazy {
        val ld = MutableLiveData<Boolean>()
        ld.value = false
        ld
    }

    /**
     * An observable LiveData boolean to indicate whether async authorization is happening. Observed
     * by [AuthActivity] to reflect current work state.
     */
    fun getIsLoading(): LiveData<Boolean> = isLoading

    /**
     * Takes a valid [FirebaseUser] and creates an [Auth] obj. by attaching a [User].
     *
     * There should never be a firebase user without a corresponding User Firestore document,
     * making this possible.
     *
     * @return A valid Auth object containing the passed in [firebaseUser] and it's matching [User]
    */
    suspend fun getCurrentAuth(firebaseUser: FirebaseUser): Auth = suspendCoroutine { cont ->
        attachUser(cont, firebaseUser)
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
                isLoading.value = true
                when {
                    password != confirmPassword -> {
                        cont.resumeWithFirebaseAuthException(
                                FirebaseAuthWordErrorType.SIGN_UP_PASSWORDS_DONT_MATCH
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
                            FirebaseAuthWordErrorType.SIGN_UP_FAILED, it
                    )
                }
            } else {
                cont.resumeWithFirebaseAuthException(
                        FirebaseAuthWordErrorType.SIGN_UP_FAILED, it
                )
            }
        } ?: cont.resumeWithFirebaseAuthException(FirebaseAuthWordErrorType.SIGN_UP_NO_CURRENT_USER)
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
                    cont.resumeWithFirebaseAuthException(FirebaseAuthWordErrorType.SIGN_UP_FAILED)
                }
            } else {
                cont.resumeWithFirebaseAuthException(FirebaseAuthWordErrorType.SIGN_UP_FAILED, it)
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
        isLoading.value = true
        auth.signInAnonymously().addOnCompleteListener {
            if (it.isSuccessful) {
                val firebaseUser = auth.currentUser
                if (firebaseUser != null) {
                    attachUser(cont, firebaseUser)
                } else {
                    cont.resumeWithFirebaseAuthException(
                            FirebaseAuthWordErrorType.ANON_UNKNOWN, it
                    )
                }
            } else {
                cont.resumeWithFirebaseAuthException(
                        FirebaseAuthWordErrorType.ANON_SIGN_UP_FAILED, it
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
    suspend fun logIn(email: String, password: String): Auth = suspendCoroutine {cont ->
        val auth = FirebaseAuth.getInstance()
        isLoading.value = true
        if (email.isNotBlank() && password.isNotBlank()) {
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            var firebaseUser = auth.currentUser
                            if (firebaseUser == null) {
                                cont.resumeWithFirebaseAuthException(
                                        FirebaseAuthWordErrorType.LOG_IN_UNKNOWN, it
                                )
                            } else {
                                attachUser(cont, firebaseUser)
                            }
                        } else {
                            cont.resumeWithFirebaseAuthException(
                                    FirebaseAuthWordErrorType.LOG_IN_FAILED, it
                            )
                        }
                    }
        } else {
            cont.resumeWithFirebaseAuthException(FirebaseAuthWordErrorType.LOG_IN_FAILED)
        }
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
                            isLoading.value = false
                        }
                    }
                }
                .addOnSuccessListener {
                    if (it.exists()) {
                        val user = it.toObject(User::class.java)
                        if (user != null) {
                            updateUser(firestore, firebaseUser, user, cont)
                        } else {
                            cont.resumeWithFirebaseAuthException(
                                    FirebaseAuthWordErrorType.FIRESTORE_USER_UNKNOWN
                            )
                        }
                    } else {
                        newUser(firestore, cont, firebaseUser)
                    }
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
                    cont.resume(Auth(firebaseUser, newUser))
                    isLoading.value = false
                }
                .addOnFailureListener {
                    cont.resumeWithException(it)
                    isLoading.value = false
                }
    }

    private fun updateUser(
            firestore: FirebaseFirestore,
            firebaseUser: FirebaseUser,
            user: User,
            cont: Continuation<Auth>
    ) {
        user.isAnonymous = firebaseUser.isAnonymous
        user.name = firebaseUser.displayName ?: user.name
        user.email = firebaseUser.email ?: user.email

        firestore.users.document(user.uid).set(user)
                .addOnSuccessListener {
                    cont.resume(Auth(firebaseUser, user))
                    isLoading.value = false
                }
                .addOnFailureListener {
                    cont.resumeWithException(it)
                    isLoading.value = false
                }
    }

    // Helper function to resume a continuation with a firebase auth exception
    private fun <T> Continuation<T>.resumeWithFirebaseAuthException(
            type: FirebaseAuthWordErrorType,
            task: Task<AuthResult>? = null
    ) {
        isLoading.value = false
        resumeWithException(task?.exception ?: type.exception)
    }

}

