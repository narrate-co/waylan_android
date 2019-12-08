package space.narrate.waylan.android.ui.auth

import space.narrate.waylan.core.data.auth.FirebaseAuthWordsException

sealed class ShowErrorModel {

    data class Error(
        val messageRes: Int? = null,
        val message: String = FirebaseAuthWordsException.LoginException.errorMessage
    ) : ShowErrorModel()

    object None : ShowErrorModel()
}

