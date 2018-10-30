package com.words.android.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.words.android.data.firestore.users
import com.words.android.data.firestore.users.User
import com.words.android.util.FirebaseAuthWordErrorType
import javax.inject.Inject
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.suspendCoroutine

class AuthViewModel @Inject constructor(): ViewModel() {

    var processText: CharSequence? = null

    private val isLoading: MutableLiveData<Boolean> by lazy {
        val ld = MutableLiveData<Boolean>()
        ld.value = false
        ld
    }

    fun getIsLoading(): LiveData<Boolean> = isLoading

    suspend fun getCurrentAuth(firebaseUser: FirebaseUser): Auth = suspendCoroutine { cont ->
        attachUser(cont, firebaseUser)
    }

    suspend fun signUp(email: String, password: String, confirmPassword: String): Auth = suspendCoroutine { cont ->
        val auth = FirebaseAuth.getInstance()
        isLoading.value = true
        when {
            password != confirmPassword -> {
                cont.resumeWithFirebaseAuthException(FirebaseAuthWordErrorType.SIGN_UP_PASSWORDS_DONT_MATCH)
            }
            auth.currentUser?.isAnonymous == true -> signUpByLinkingCredentials(cont, email, password)
            else -> signUpByCreatingEmptyAccount(cont, email, password)
        }
    }

    private fun signUpByLinkingCredentials(cont: Continuation<Auth>, email: String, password: String) {
        val credentials = EmailAuthProvider.getCredential(email, password)
        val auth = FirebaseAuth.getInstance()
        auth.currentUser?.linkWithCredential(credentials)?.addOnCompleteListener {
            if (it.isSuccessful) {
                val firebaseUser = it.result?.user
                if (firebaseUser != null) {
                    attachUser(cont, firebaseUser)
                } else {
                    cont.resumeWithFirebaseAuthException(FirebaseAuthWordErrorType.SIGN_UP_FAILED, it)
                }
            } else {
                cont.resumeWithFirebaseAuthException(FirebaseAuthWordErrorType.SIGN_UP_FAILED, it)
            }
        } ?: cont.resumeWithFirebaseAuthException(FirebaseAuthWordErrorType.SIGN_UP_NO_CURRENT_USER)
    }

    private fun signUpByCreatingEmptyAccount(cont: Continuation<Auth>, email: String, password: String) {
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

    suspend fun signUpAnonymously(): Auth = suspendCoroutine { cont ->
        val auth = FirebaseAuth.getInstance()
        isLoading.value = true
        auth.signInAnonymously().addOnCompleteListener {
            if (it.isSuccessful) {
                val firebaseUser = auth.currentUser
                if (firebaseUser != null) {
                    attachUser(cont, firebaseUser)
                } else {
                    cont.resumeWithFirebaseAuthException(FirebaseAuthWordErrorType.ANON_UNKNOWN, it)
                }
            } else {
                cont.resumeWithFirebaseAuthException(FirebaseAuthWordErrorType.ANON_SIGN_UP_FAILED, it)
            }
        }
    }

    suspend fun logIn(email: String, password: String): Auth = suspendCoroutine {cont ->
        val auth = FirebaseAuth.getInstance()
        isLoading.value = true
        if (email.isNotBlank() && password.isNotBlank()) {
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            var firebaseUser = auth.currentUser
                            if (firebaseUser == null) {
                                cont.resumeWithFirebaseAuthException(FirebaseAuthWordErrorType.LOG_IN_UNKNOWN, it)
                            } else {
                                attachUser(cont, firebaseUser)
                            }
                        } else {
                            cont.resumeWithFirebaseAuthException(FirebaseAuthWordErrorType.LOG_IN_FAILED, it)
                        }
                    }
        } else {
            cont.resumeWithFirebaseAuthException(FirebaseAuthWordErrorType.LOG_IN_FAILED)
        }
    }

    private fun attachUser(cont: Continuation<Auth>, firebaseUser: FirebaseUser) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.users.document(firebaseUser.uid).get()
                .addOnFailureListener {
                    when ((it as FirebaseFirestoreException).code) {
                        FirebaseFirestoreException.Code.UNAVAILABLE -> newUser(firestore, cont, firebaseUser)
                        else -> {
                            cont.resumeWithException(it)
                            isLoading.value = false
                        }
                    }
                }
                .addOnSuccessListener {
                    if (it.exists()) {
                        val user = it.toObject(User::class.java)
                        cont.resume(Auth(firebaseUser, user!!))
                        isLoading.value = false
                    } else {
                        newUser(firestore, cont, firebaseUser)
                    }
                }
    }

    private fun newUser(firestore: FirebaseFirestore, cont: Continuation<Auth>, firebaseUser: FirebaseUser) {
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

    private fun <T> Continuation<T>.resumeWithFirebaseAuthException(type: FirebaseAuthWordErrorType, task: Task<AuthResult>? = null) {
        isLoading.value = false
        resumeWithException(task?.exception ?: type.exception)
    }

}

