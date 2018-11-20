package com.wordsdict.android.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.core.app.TaskStackBuilder
import com.wordsdict.android.App
import com.wordsdict.android.MainActivity
import com.wordsdict.android.Navigator
import com.wordsdict.android.R
import com.wordsdict.android.ui.common.BaseUserActivity

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

    fun updateNightMode(mode: Int) {
        delegate.setLocalNightMode(mode)
        (application as App).updateDefaultNightMode()
    }

}
