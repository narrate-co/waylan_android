package space.narrate.words.android.ui.auth

import space.narrate.words.android.data.auth.FirebaseAuthWordsException

sealed class ShowErrorModel {

    data class Error(
        val messageRes: Int? = null,
        val message: String = FirebaseAuthWordsException.LoginException.errorMessage
    ) : ShowErrorModel()

    object None : ShowErrorModel()
}

