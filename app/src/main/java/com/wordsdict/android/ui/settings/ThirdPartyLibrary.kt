package com.wordsdict.android.ui.settings

/**
 * An object that represents all data needed to display a 3p library
 */
data class ThirdPartyLibrary(
        val name: String,
        val url: String
)

/**
 * A helper variable to hold a list of all 3p libraries which need to be shown, as specified in
 * their licenses, in the app
 */
val allThirdPartyLibraries = listOf(
        ThirdPartyLibrary(
                "Material Design Components",
                "https://github.com/material-components"
        ),
        ThirdPartyLibrary(
                "Android Jetpack",
                "https://developer.android.com/jetpack/"
        ),
        ThirdPartyLibrary(
                "ThreeTen Backport",
                "https://github.com/ThreeTen/threetenbp"
        ),
        ThirdPartyLibrary(
                "Gson",
                "https://github.com/google/gson"
        ),
        ThirdPartyLibrary(
                "Firebase",
                "https://firebase.google.com/"
        ),
        ThirdPartyLibrary(
                "Dagger 2",
                "https://github.com/google/dagger"
        ),
        ThirdPartyLibrary(
                "Retrofit",
                "https://github.com/square/retrofit"
        )
)