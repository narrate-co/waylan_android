package space.narrate.waylan.core.data.firestore

import com.google.firebase.auth.FirebaseAuthException
import space.narrate.waylan.core.R

sealed class FirebaseAuthWordsException(
    errorCode: String,
    val errorMessage: String,
    private val errorMessageRes: Int
) : FirebaseAuthException(errorCode, errorMessage) {

    val localizedMessageRes: Int = errorMessageRes

    object NoCurrentUserException : FirebaseAuthWordsException(
        "ncu",
        "No user is currently signed in",
        R.string.auth_error_message_no_current_user
    )

    object AnonException : FirebaseAuthWordsException(
        "au",
        "An unknown authentication error occurred",
        R.string.auth_error_message_unknown
    )

    object AnonSignUpFailedException : FirebaseAuthWordsException(
        "as",
        "Unable to log in anonymously",
        R.string.auth_error_message_anon_login_unknown
    )

    object LoginException : FirebaseAuthWordsException(
        "lu",
        "An unknown log in error occurred",
        R.string.auth_error_message_login_unknown
    )

    object LoginFailedException : FirebaseAuthWordsException(
        "lf",
        "Unable to log in",
        R.string.auth_error_message_login_failed
    )

    object LoginEmptyFieldsException : FirebaseAuthWordsException(
        "le",
        "Empty fields",
        R.string.auth_error_message_empty_fields
    )

    object SignUpException : FirebaseAuthWordsException(
        "su",
        "An unknown sign up error occurred",
        R.string.auth_error_message_signup_unknown
    )

    object SignUpFailedException : FirebaseAuthWordsException(
        "sf",
        "Unable to sign up",
        R.string.auth_error_message_signup_failed
    )

    object SignUpPasswordMissmatchException : FirebaseAuthWordsException(
        "sm",
        "Unable to use credentials",
        R.string.auth_error_message_passwords_mismatch
    )

    object SignUpNoExistingUserException : FirebaseAuthWordsException(
        "sc",
        "Unable to link null user",
        R.string.auth_error_message_no_current_user_to_link
    )

    object FirestoreUserException : FirebaseAuthWordsException(
        "fu",
        "Unable to create a new user",
        R.string.auth_error_message_firestore_user_unknown_error
    )

}
