package com.words.android.ui.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.words.android.util.FirebaseAuthErrorType
import com.words.android.util.FirebaseAuthWordException
import javax.inject.Inject
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.suspendCoroutine

class AuthViewModel @Inject constructor(): ViewModel() {


    suspend fun signUp(email: String, password: String, confirmPassword: String): FirebaseUser = suspendCoroutine { cont ->
        //TODO password and confirm password check, etc.
        //TODO use showErrorMessage() to show any validation errors

        val auth = FirebaseAuth.getInstance()
        when {
            password != confirmPassword -> cont.resumeWithException(FirebaseAuthWordException(null, FirebaseAuthErrorType.SIGN_UP_PASSWORDS_DONT_MATCH))
            auth.currentUser?.isAnonymous == true -> signUpByLinkingCredentials(cont, email, password)
            else -> signUpByCreatingEmptyAccount(cont, email, password)
        }
    }

    private fun signUpByLinkingCredentials(cont: Continuation<FirebaseUser>, email: String, password: String) {
        val credentials = EmailAuthProvider.getCredential(email, password)
        val auth = FirebaseAuth.getInstance()
        auth.currentUser?.linkWithCredential(credentials)?.addOnCompleteListener {
            if (it.isSuccessful) {
                val user = it.result?.user
                if (user != null) {
                    cont.resume(user)
                } else {
                    cont.resumeWithException(FirebaseAuthWordException(null, FirebaseAuthErrorType.SIGN_UP_FAILED))
                }
            } else {
                cont.resumeWithException(FirebaseAuthWordException(it.exception, FirebaseAuthErrorType.SIGN_UP_FAILED))
            }
        } ?: cont.resumeWithException(FirebaseAuthWordException(null, FirebaseAuthErrorType.SIGN_UP_NO_CURRNET_USER))
    }

    private fun signUpByCreatingEmptyAccount(cont: Continuation<FirebaseUser>, email: String, password: String) {
        val auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                val user = it.result?.user
                if (user != null) {
                    cont.resume(user)
                } else {
                    cont.resumeWithException(FirebaseAuthWordException(null, FirebaseAuthErrorType.SIGN_UP_FAILED))
                }
            } else {
                cont.resumeWithException(FirebaseAuthWordException(it.exception, FirebaseAuthErrorType.SIGN_UP_FAILED))
            }
        }
    }

    suspend fun signUpAnonymously(): FirebaseUser = suspendCoroutine { cont ->
        val auth = FirebaseAuth.getInstance()
        auth.signInAnonymously().addOnCompleteListener {
            if (it.isSuccessful) {
                val user = auth.currentUser
                if (user != null) {
                    cont.resume(user)
                } else {
                    cont.resumeWithException(FirebaseAuthWordException(it.exception, FirebaseAuthErrorType.ANON_UNKNOWN))
                }
            } else {
                cont.resumeWithException(FirebaseAuthWordException(it.exception, FirebaseAuthErrorType.ANON_SIGN_UP_FAILED))
            }
        }
    }

    suspend fun logIn(email: String, password: String): FirebaseUser = suspendCoroutine {cont ->
        val auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        var user = auth.currentUser
                        if (user == null) {
                            cont.resumeWithException(FirebaseAuthWordException(it.exception, FirebaseAuthErrorType.LOG_IN_UNKNOWN))
                        } else {
                            cont.resume(user)
                        }
                    } else {
                        cont.resumeWithException(FirebaseAuthWordException(it.exception, FirebaseAuthErrorType.LOG_IN_FAILED))
                    }
                }
    }

}

