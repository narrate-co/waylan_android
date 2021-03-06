package space.narrate.waylan.core.data.firestore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import space.narrate.waylan.core.data.Result
import space.narrate.waylan.core.data.Result.Error
import space.narrate.waylan.core.data.Result.Success
import space.narrate.waylan.core.data.firestore.users.User
import space.narrate.waylan.core.data.prefs.PreferenceStore
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * A store which knows about the credentials which back a [User] - the global account object.
 *
 * This class can [authenticate] a [User] or help create a new [User] by using [logIn], [signUp]
 * or [signUpAnonymously].
 */
class AuthenticationStore(
    private val firebaseAuth: FirebaseAuth,
    private val firestoreStore: FirestoreStore,
    private val preferenceStore: PreferenceStore
) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    /**
     * Whether or not the credentials necessary to authenticate a [User] are available.
     *
     * If they are, the client can call [authenticate] and refresh the [user] LiveData. Otherwise,
     * the client must use [logIn], [signUp] or [signUpAnonymously] to get/create the necessary
     * credentials and refresh [user].
     */
    val hasCredentials: Boolean
        get() = firebaseAuth.currentUser != null

    val hasUser: Boolean
        get() = _uid.value != null && _uid.value?.isNotEmpty() == true

    private val _uid: MutableLiveData<String> = MutableLiveData()
    val uid: LiveData<String>
        get() = _uid

    init {
        // If the user's uid is stored in preferences, retrieve it so auth can be skipped and done
        // in the background, avoiding the spash screen when recreating the app.
        _uid.value = preferenceStore.uid.getValue()
    }

    suspend fun authenticate(): Result<User> {
        val firebaseUser = firebaseAuth.currentUser
            ?: return Error(FirebaseAuthWordsException.LoginException)

        val result = getOrCreateUser(firebaseUser)

        if (result is Success) {
            // Launch and forget an update. This shouldn't block the authentication process
            _uid.value = result.data.uid
            // Save the uid to preferences for faster startup times.
            preferenceStore.uid.setValue(result.data.uid)

            GlobalScope.launch {
                firestoreStore.updateUser(result.data.uid) {
                    isAnonymous = firebaseUser.isAnonymous
                    name = firebaseUser.displayName ?: name
                    email = firebaseUser.email ?: email
                }
            }
        }

        return result
    }

    suspend fun logIn(email: String, password: String): Result<User> {
        try {
            suspendCoroutine<FirebaseUser> { cont ->
                val auth = FirebaseAuth.getInstance()
                if (email.isNotBlank() && password.isNotBlank()) {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener {
                            val user = it.user
                            if (user != null) {
                                cont.resume(user)
                            } else {
                                cont.resumeWithFirebaseAuthException(
                                    FirebaseAuthWordsException.LoginException
                                )
                            }
                        }
                        .addOnFailureListener {
                            cont.resumeWithFirebaseAuthException(
                                FirebaseAuthWordsException.LoginException,
                                it
                            )
                        }
                } else {
                    cont.resumeWithFirebaseAuthException(
                        FirebaseAuthWordsException.LoginEmptyFieldsException
                    )
                }
            }
            return authenticate()
        } catch (e: Exception) {
            return Error(e)
        }
    }

    suspend fun signUp(
        email: String,
        password: String,
        confirmPassword: String
    ): Result<User> {
        try {
            suspendCancellableCoroutine<FirebaseUser> { cont ->
                if (password != confirmPassword) {
                    cont.resumeWithFirebaseAuthException(
                        FirebaseAuthWordsException.SignUpPasswordMissmatchException
                    )
                } else if (firebaseAuth.currentUser?.isAnonymous == true) {
                    signUpByLinkingCredentials(cont, email, password)
                } else {
                    signUpByCreatingEmptyAccount(cont, email, password)
                }
            }
            return authenticate()
        } catch (e: Exception) {
            return Error(e)
        }
    }

    suspend fun signUpAnonymously(): Result<User> {
        try {
            suspendCancellableCoroutine<FirebaseUser> { cont ->
                firebaseAuth.signInAnonymously()
                    .addOnSuccessListener {
                        val user = it.user
                        if (user != null) {
                            cont.resume(user)
                        } else {
                            cont.resumeWithFirebaseAuthException(
                                FirebaseAuthWordsException.AnonSignUpFailedException
                            )
                        }
                    }
                    .addOnFailureListener {
                        cont.resumeWithFirebaseAuthException(
                            FirebaseAuthWordsException.AnonSignUpFailedException,
                            it
                        )
                    }
            }
            return authenticate()
        } catch (e: Exception) {
            return Error(e)
        }
    }

    private fun signUpByLinkingCredentials(
        cont: CancellableContinuation<FirebaseUser>,
        email: String,
        password: String
    ) {
        val credentials = EmailAuthProvider.getCredential(email, password)
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            currentUser.linkWithCredential(credentials)
                .addOnSuccessListener {
                    val user = it.user
                    if (user != null) {
                        cont.resume(user)
                    } else {
                        cont.resumeWithFirebaseAuthException(
                            FirebaseAuthWordsException.AnonSignUpFailedException
                        )
                    }
                }
                .addOnFailureListener {
                    cont.resumeWithFirebaseAuthException(
                        FirebaseAuthWordsException.AnonSignUpFailedException,
                        it
                    )
                }
        } else {
            cont.resumeWithFirebaseAuthException(
                FirebaseAuthWordsException.SignUpNoExistingUserException
            )
        }
    }

    private fun signUpByCreatingEmptyAccount(
        cont: CancellableContinuation<FirebaseUser>,
        email: String,
        password: String
    ) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val user = it.user
                if (user != null) {
                    cont.resume(user)
                } else {
                    cont.resumeWithFirebaseAuthException(
                        FirebaseAuthWordsException.SignUpFailedException
                    )
                }
            }
            .addOnFailureListener {
                cont.resumeWithFirebaseAuthException(
                    FirebaseAuthWordsException.SignUpFailedException,
                    it
                )
            }
    }

    private suspend fun getOrCreateUser(firebaseUser: FirebaseUser): Result<User> {
        val result = firestoreStore.getUser(firebaseUser.uid)

        if (result is Success) {
            return result
        } else {
            return firestoreStore.newUser(firebaseUser.uid) {
                isAnonymous = firebaseUser.isAnonymous
                name = firebaseUser.displayName ?: ""
                email = firebaseUser.email ?: ""
            }
        }
    }

    // Helper function to resume a continuation with a firebase firebaseAuth exception
    private fun <T> Continuation<T>.resumeWithFirebaseAuthException(
        exception: FirebaseAuthWordsException,
        callbackException: Exception? = null
    ) {
        resumeWithException(callbackException ?: exception)
    }
}