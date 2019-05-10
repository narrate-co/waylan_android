package space.narrate.words.android.ui.settings

import android.os.Bundle
import space.narrate.words.android.App
import space.narrate.words.android.Navigator
import space.narrate.words.android.R
import space.narrate.words.android.ui.common.BaseUserActivity

/**
 * A host Activity for all child settings Fragments including [SettingsFragment],
 * [AboutFragment], [DeveloperSettingsFragment] and [ThirdPartyLibrariesFragment]
 */
class SettingsActivity : BaseUserActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        if (savedInstanceState == null) {
            Navigator.showSettings(this)
        }
    }

    fun showAbout() = Navigator.showAbout(this)

    fun showDeveloperSettings() = Navigator.showDeveloperSettings(this)
}
