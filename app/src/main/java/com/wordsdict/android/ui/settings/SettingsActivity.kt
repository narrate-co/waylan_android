package com.wordsdict.android.ui.settings

import android.os.Bundle
import com.wordsdict.android.App
import com.wordsdict.android.Navigator
import com.wordsdict.android.R
import com.wordsdict.android.ui.common.BaseUserActivity

/**
 * A host Activity for all child settings Fragments including [SettingsFragment],
 * [AboutFragment], [DeveloperSettingsFragment] and [ThirdPartyLibrariesFragment]
 */
class SettingsActivity : BaseUserActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        if (savedInstanceState == null) {
            showSettings()
        }
    }

    private fun showSettings() = Navigator.showSettings(this)

    fun showAbout() = Navigator.showAbout(this)

    fun showDeveloperSettings() = Navigator.showDeveloperSettings(this)
}
