package com.words.android.util

import com.google.firebase.auth.FirebaseAuthException
import com.words.android.R

enum class FirebaseAuthWordErrorType(val errorCode: String, val message: String, val detailMessageRes: Int) {
    ANON_UNKNOWN("au","An unknown authentication error occurred", R.string.auth_error_message_unknown),
    ANON_SIGN_UP_FAILED("as", "Unable to log in anonymously", R.string.auth_error_message_anon_login_unknown),
    LOG_IN_UNKNOWN("lu","An unknown log in error occurred", R.string.auth_error_message_login_unknown),
    LOG_IN_FAILED("lf", "Unable to log in", R.string.auth_error_message_login_failed),
    LOG_IN_EMPTY_FIELDS("le", "Empty fields", R.string.auth_error_message_empty_fields),
    SIGN_UP_UNKNOWN("su", "An unknown sign up error occurred", R.string.auth_error_message_signup_unknown),
    SIGN_UP_FAILED("sf", "Unable to sign up", R.string.auth_error_message_signup_failed),
    SIGN_UP_PASSWORDS_DONT_MATCH("sm","Unable to use credentials", R.string.auth_error_message_passwords_mismatch),
    SIGN_UP_NO_CURRENT_USER("sc", "Unable to link null user", R.string.auth_error_message_no_current_user_to_link);

    val exception: FirebaseAuthWordException
        get() = FirebaseAuthWordException(this)
}


class FirebaseAuthWordException(errorType: FirebaseAuthWordErrorType)
    : FirebaseAuthException(errorType.errorCode, errorType.message) {
    val localizedMessageRes: Int = errorType.detailMessageRes
}
