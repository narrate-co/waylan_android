package com.words.android.util

import com.google.firebase.auth.FirebaseAuthException

enum class FirebaseAuthErrorType(val errorCode: String, val detailMessage: String) {
    ANON_UNKNOWN("au", "An unknown error occurred during authentication"),
    ANON_SIGN_UP_FAILED("as", "Unable to log in anonymously"),
    LOG_IN_UNKNOWN("lu", "An unknown error occurred during log in"),
    LOG_IN_FAILED("lf", "Failed to log in"),
    SIGN_UP_UNKNOWN("su", "An unknown error occurred during sign up"),
    SIGN_UP_FAILED("sf", "Failed to sign up"),
    SIGN_UP_PASSWORDS_DONT_MATCH("sm", "Passwords don't match"),
    SIGN_UP_NO_CURRNET_USER("sc", "No current user is available to create account with")
}


class FirebaseAuthWordException(originalException: Exception?, errorType: FirebaseAuthErrorType)
    : FirebaseAuthException(errorType.errorCode, errorType.detailMessage) {
    init {
        originalException?.printStackTrace()
        //TODO send log report
    }
}
