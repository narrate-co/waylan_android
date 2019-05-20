package space.narrate.words.android.ui.auth

import space.narrate.words.android.data.auth.Auth

data class LaunchMainModel(
    val auth: Auth?,
    val clearStack: Boolean = true
)