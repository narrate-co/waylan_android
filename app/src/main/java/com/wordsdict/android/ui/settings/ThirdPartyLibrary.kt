package com.wordsdict.android.ui.settings

data class ThirdPartyLibrary(
        val name: String,
        val url: String
)

val allThirdPartyLibraries = listOf(
        ThirdPartyLibrary("Material Design Components", "https://github.com/material-components"),
        ThirdPartyLibrary("Android Jetpack", "https://developer.android.com/jetpack/"),
        ThirdPartyLibrary("ThreeTen Backport", "https://github.com/ThreeTen/threetenbp"),
        ThirdPartyLibrary("Gson", "https://github.com/google/gson"),
        ThirdPartyLibrary("Firebase", "https://firebase.google.com/"),
        ThirdPartyLibrary("Dagger 2", "https://github.com/google/dagger"),
        ThirdPartyLibrary("Retrofit", "https://github.com/square/retrofit")
)