package com.words.android.ui.settings

import android.os.Bundle
import com.words.android.Navigator
import com.words.android.R
import com.words.android.ui.common.BaseUserActivity

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

}
